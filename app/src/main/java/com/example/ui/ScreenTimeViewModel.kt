package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppLimit
import com.example.data.AppUsageRecord
import com.example.data.CategoryLimit
import com.example.data.Challenge
import com.example.data.ParentalSettings
import com.example.data.Reward
import com.example.data.ScreenTimeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScreenTimeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ScreenTimeRepository

    val usageToday: StateFlow<List<AppUsageRecord>>
    val appLimits: StateFlow<List<AppLimit>>
    val categoryLimits: StateFlow<List<CategoryLimit>>
    
    // Gamification Flows
    val challenges: StateFlow<List<Challenge>>
    val rewards: StateFlow<List<Reward>>
    val parentalSettings: StateFlow<ParentalSettings?>

    // Gamification Memory States
    private val _userXp = MutableStateFlow(320) // Seeded starting XP
    val userXp: StateFlow<Int> = _userXp.asStateFlow()

    private val _streakDays = MutableStateFlow(6) // Seeded starting daily streak
    val streakDays: StateFlow<Int> = _streakDays.asStateFlow()

    // Focus Mode Active states
    private val _isFocusActive = MutableStateFlow(false)
    val isFocusActive: StateFlow<Boolean> = _isFocusActive.asStateFlow()

    private val _focusTotalSeconds = MutableStateFlow(0)
    val focusTotalSeconds: StateFlow<Int> = _focusTotalSeconds.asStateFlow()

    private val _focusRemainingSeconds = MutableStateFlow(0)
    val focusRemainingSeconds: StateFlow<Int> = _focusRemainingSeconds.asStateFlow()

    private val _focusCompletedSuccessfully = MutableStateFlow(false)
    val focusCompletedSuccessfully: StateFlow<Boolean> = _focusCompletedSuccessfully.asStateFlow()

    private var focusJob: Job? = null

    // parental Authorization Lock Screen state
    private val _isParentViewUnlocked = MutableStateFlow(false)
    val isParentViewUnlocked: StateFlow<Boolean> = _isParentViewUnlocked.asStateFlow()

    // Live Simulator States
    private val _simulatingAppName = MutableStateFlow<String?>(null)
    val simulatingAppName: StateFlow<String?> = _simulatingAppName.asStateFlow()

    private val _simulatingPackageName = MutableStateFlow<String?>(null)
    val simulatingPackageName: StateFlow<String?> = _simulatingPackageName.asStateFlow()

    private val _simulatingCategory = MutableStateFlow<String?>(null)
    val simulatingCategory: StateFlow<String?> = _simulatingCategory.asStateFlow()

    private val _simulatedSeconds = MutableStateFlow(0)
    val simulatedSeconds: StateFlow<Int> = _simulatedSeconds.asStateFlow()

    private val _showLimitBlocker = MutableStateFlow(false)
    val showLimitBlocker: StateFlow<Boolean> = _showLimitBlocker.asStateFlow()

    private val _blockerReason = MutableStateFlow("")
    val blockerReason: StateFlow<String> = _blockerReason.asStateFlow()

    private var simulationJob: Job? = null
    private var accumulatedSecondsToLog = 0L

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ScreenTimeRepository(db.screenTimeDao())

        // Seed default parameters on launch
        viewModelScope.launch {
            repository.prePopulateIfEmpty()
        }

        usageToday = repository.getUsageTodayFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        appLimits = repository.getAppLimitsFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        categoryLimits = repository.getCategoryLimitsFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        challenges = repository.getChallengesFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        rewards = repository.getRewardsFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        parentalSettings = repository.getParentalSettingsFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    // --- Core Simulator Logic ---
    private fun flushSimulationUsage() {
        val app = _simulatingAppName.value ?: return
        val pkg = _simulatingPackageName.value ?: return
        val cat = _simulatingCategory.value ?: return
        val seconds = accumulatedSecondsToLog
        if (seconds > 0) {
            accumulatedSecondsToLog = 0L
            viewModelScope.launch {
                repository.logUsage(app, pkg, cat, seconds)
            }
        }
    }

    fun startSimulation(appName: String, packageName: String, category: String) {
        stopSimulation()
        _simulatingAppName.value = appName
        _simulatingPackageName.value = packageName
        _simulatingCategory.value = category
        _simulatedSeconds.value = 0
        _showLimitBlocker.value = false
        accumulatedSecondsToLog = 0L

        // Check blocking triggers immediately on launch
        if (checkImmediateBlocks(appName, packageName, category)) {
            return
        }

        simulationJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _simulatedSeconds.value += 1
                accumulatedSecondsToLog += 1

                // Every 5 seconds, flush accumulated progress to Room
                if (accumulatedSecondsToLog >= 5) {
                    flushSimulationUsage()
                }

                // Verify limits periodically
                if (checkImmediateBlocks(appName, packageName, category)) {
                    flushSimulationUsage()
                    break
                } else {
                    checkLimitsForActiveApp(packageName, category)
                    if (_showLimitBlocker.value) {
                        flushSimulationUsage()
                        break
                    }
                }
            }
        }
    }

    fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
        flushSimulationUsage()
        _simulatingAppName.value = null
        _simulatingPackageName.value = null
        _simulatingCategory.value = null
        _simulatedSeconds.value = 0
        _showLimitBlocker.value = false
    }

    private fun checkImmediateBlocks(appName: String, packageName: String, category: String): Boolean {
        // 1. Parental blockade check (Absolute priority override)
        val pSettings = parentalSettings.value
        if (pSettings != null && pSettings.isEnabled) {
            val blockedAppsList = pSettings.blockedPackagesCommaSeparated
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            
            if (blockedAppsList.contains(packageName) && pSettings.isAppBlockingActive) {
                _showLimitBlocker.value = true
                _blockerReason.value = "LOCKDOWN ACTIVE: This app has been locked on this child device by parental authority. Seek permission for unlock."
                return true
            }
        }

        // 2. Focus mode blockade check (Blocks social, games, entertainment)
        if (isFocusActive.value) {
            val restrictedCategories = listOf("Social Media", "Games", "Entertainment")
            if (restrictedCategories.contains(category)) {
                _showLimitBlocker.value = true
                val focusMins = focusRemainingSeconds.value / 60
                val focusSecs = focusRemainingSeconds.value % 60
                _blockerReason.value = "FOCUS WORKSPACE ACTIVE: You cannot open $appName right now. Your focus timer is ticking! Remaining: ${String.format("%02d:%02d", focusMins, focusSecs)}."
                return true
            }
        }
        return false
    }

    private fun checkLimitsForActiveApp(packageName: String, category: String) {
        val currentRecords = usageToday.value
        val limits = appLimits.value
        val catLimits = categoryLimits.value

        val appRecord = currentRecords.find { it.packageName == packageName } ?: return
        val currentSecondsToday = appRecord.usedSeconds

        // Check App specific limit
        val appLimit = limits.find { it.packageName == packageName && it.isEnabled }
        if (appLimit != null) {
            val limitSecs = appLimit.limitMinutes * 60L
            if (currentSecondsToday >= limitSecs) {
                _showLimitBlocker.value = true
                _blockerReason.value = "DAILY BUDGET DEPLETED: You have fully spent your daily budget of ${appLimit.limitMinutes} minute(s) setup for $packageName (${appRecord.appName})!"
                return
            }
        }

        // Check Category limits
        val catLimit = catLimits.find { it.categoryName == category && it.isEnabled }
        if (catLimit != null) {
            val totalSecsInCategory = currentRecords
                .filter { it.category == category }
                .sumOf { it.usedSeconds }

            val limitSecs = catLimit.limitMinutes * 60L
            if (totalSecsInCategory >= limitSecs) {
                _showLimitBlocker.value = true
                _blockerReason.value = "CATEGORY OVER-BUDGET: You have reached your category budget of ${catLimit.limitMinutes} minute(s) for $category folders today!"
            }
        }
    }

    // --- Focus Mode Routines ---
    fun startFocusMode(minutes: Int) {
        stopFocusMode()
        _focusCompletedSuccessfully.value = false
        _isFocusActive.value = true
        _focusTotalSeconds.value = minutes * 60
        _focusRemainingSeconds.value = minutes * 60

        // If simulating app is blocked, stop it immediately
        val curPkg = simulatingPackageName.value
        val curCat = simulatingCategory.value
        if (curPkg != null && curCat != null) {
            val restrictedCategories = listOf("Social Media", "Games", "Entertainment")
            if (restrictedCategories.contains(curCat)) {
                stopSimulation()
            }
        }

        focusJob = viewModelScope.launch {
            var lastRecordedMinutes = 0
            while (_focusRemainingSeconds.value > 0) {
                delay(1000)
                _focusRemainingSeconds.value -= 1
                
                // Track focus progress in challenges - only database-write when minutes tick up
                val currentMins = (_focusTotalSeconds.value - _focusRemainingSeconds.value) / 60
                if (currentMins > lastRecordedMinutes) {
                    lastRecordedMinutes = currentMins
                    updateChallengeActiveProgress("focus_sprint", currentMins)
                }
            }
            
            // Reached zero: Focus completed successfully!
            _isFocusActive.value = false
            _focusCompletedSuccessfully.value = true
            
            // Add substantial XP
            addExperiencePoints(150)
            _streakDays.value += 1 // Increment streak as a reward!

            // Unlock focus badge
            unlockReward("badge_focus_lord")
            completeChallengeDirectly("focus_sprint")
        }
    }

    fun stopFocusMode() {
        focusJob?.cancel()
        focusJob = null
        _isFocusActive.value = false
        _focusRemainingSeconds.value = 0
        _focusTotalSeconds.value = 0
    }

    fun dismissFocusSuccessAnnouncement() {
        _focusCompletedSuccessfully.value = false
    }

    // --- Parental Controls Routines ---
    fun tryUnlockParentMode(pin: String): Boolean {
        val currentSettings = parentalSettings.value ?: return false
        val isMatch = currentSettings.passcode == pin
        if (isMatch) {
            _isParentViewUnlocked.value = true
        }
        return isMatch
    }

    fun lockParentView() {
        _isParentViewUnlocked.value = false
    }

    fun toggleParentProtection(isEnabled: Boolean) {
        viewModelScope.launch {
            val current = parentalSettings.value ?: ParentalSettings()
            repository.saveParentalSettings(current.copy(isEnabled = isEnabled))
        }
    }

    fun saveParentPasscode(newPin: String) {
        viewModelScope.launch {
            val current = parentalSettings.value ?: ParentalSettings()
            repository.saveParentalSettings(current.copy(passcode = newPin))
        }
    }

    fun updateParentBlockedPackages(commaSeparatedPackages: String) {
        viewModelScope.launch {
            val current = parentalSettings.value ?: ParentalSettings()
            repository.saveParentalSettings(current.copy(
                blockedPackagesCommaSeparated = commaSeparatedPackages,
                isEnabled = true,
                isAppBlockingActive = true
            ))
        }
    }

    fun toggleParentBlockingActive(isActive: Boolean) {
        viewModelScope.launch {
            val current = parentalSettings.value ?: ParentalSettings()
            repository.saveParentalSettings(current.copy(isAppBlockingActive = isActive))
        }
    }

    // --- Gamification Systems ---
    private fun addExperiencePoints(xp: Int) {
        _userXp.value += xp
        // Auto unlock milestones at specific XP values!
        checkXpMilestones(_userXp.value)
    }

    private fun checkXpMilestones(currentXp: Int) {
        if (currentXp >= 1000) {
            unlockReward("tier_gold")
        }
        if (currentXp >= 500) {
            unlockReward("badge_focus_lord")
        }
        if (currentXp >= 300) {
            unlockReward("badge_zen")
        }
        if (currentXp >= 100) {
            unlockReward("badge_warrior")
        }
    }

    fun progressChallenge(id: String, increment: Int) {
        viewModelScope.launch {
            val list = challenges.value
            val chal = list.find { it.id == id } ?: return@launch
            if (chal.isCompleted) return@launch

            val nextValue = (chal.currentValue + increment).coerceAtMost(chal.targetValue)
            val isNowDone = nextValue >= chal.targetValue

            val updated = chal.copy(currentValue = nextValue, isCompleted = isNowDone)
            repository.saveChallenge(updated)

            if (isNowDone) {
                addExperiencePoints(chal.rewardedXp)
                // Unlock reward according to challenge
                when (id) {
                    "detox_social" -> unlockReward("badge_warrior")
                    "gaming_diet" -> unlockReward("badge_warrior")
                    "focus_sprint" -> unlockReward("badge_focus_lord")
                    "screen_diet" -> unlockReward("badge_diet_king")
                }
            }
        }
    }

    private fun updateChallengeActiveProgress(id: String, curMins: Int) {
        viewModelScope.launch {
            val list = challenges.value
            val chal = list.find { it.id == id } ?: return@launch
            if (chal.isCompleted) return@launch

            val nextValue = curMins.coerceAtMost(chal.targetValue)
            val isNowDone = nextValue >= chal.targetValue

            val updated = chal.copy(currentValue = nextValue, isCompleted = isNowDone)
            repository.saveChallenge(updated)

            if (isNowDone) {
                addExperiencePoints(chal.rewardedXp)
            }
        }
    }

    private fun completeChallengeDirectly(id: String) {
        viewModelScope.launch {
            val list = challenges.value
            val chal = list.find { it.id == id } ?: return@launch
            if (chal.isCompleted) return@launch

            val updated = chal.copy(currentValue = chal.targetValue, isCompleted = true)
            repository.saveChallenge(updated)
        }
    }

    fun unlockReward(id: String) {
        viewModelScope.launch {
            val rewardList = rewards.value
            val rew = rewardList.find { it.id == id } ?: return@launch
            if (rew.isUnlocked) return@launch

            repository.saveReward(rew.copy(isUnlocked = true))
        }
    }

    // --- Legacy settings ---
    fun updateCategoryLimit(category: String, limitMins: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.saveCategoryLimit(CategoryLimit(category, limitMins, isEnabled))
        }
    }

    fun updateAppLimit(packageName: String, appName: String, category: String, limitMins: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.saveAppLimit(AppLimit(packageName, appName, category, limitMins, isEnabled))
        }
    }

    fun deleteAppLimit(packageName: String) {
        viewModelScope.launch {
            repository.deleteAppLimit(packageName)
        }
    }

    fun addManualUsage(appName: String, packageName: String, category: String, minutes: Int) {
        viewModelScope.launch {
            repository.logUsage(appName, packageName, category, minutes * 60L)
            
            // Evaluate challenge progress on manual logs!
            if (category == "Social Media") {
                // Social Detox allows up to 45 mins. If manual addition logs time, let's update progress!
                // Social is a "Diet" challenge (lower is better, or tracking overall adherence progress)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopSimulation()
        stopFocusMode()
    }
}
