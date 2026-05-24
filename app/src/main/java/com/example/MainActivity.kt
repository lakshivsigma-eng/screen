package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.DashboardScreen
import com.example.ui.FocusScreen
import com.example.ui.GamificationScreen
import com.example.ui.LimitsScreen
import com.example.ui.ScreenTimeViewModel
import com.example.ui.SimulatorScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ScreenTimeViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val usageToday by viewModel.usageToday.collectAsState()
                val appLimits by viewModel.appLimits.collectAsState()
                val categoryLimits by viewModel.categoryLimits.collectAsState()
                
                // New flows
                val challenges by viewModel.challenges.collectAsState()
                val rewards by viewModel.rewards.collectAsState()
                val parentalSettings by viewModel.parentalSettings.collectAsState()
                val isParentViewUnlocked by viewModel.isParentViewUnlocked.collectAsState()
                val userXp by viewModel.userXp.collectAsState()
                val streakDays by viewModel.streakDays.collectAsState()
                
                // Focus flows
                val isFocusActive by viewModel.isFocusActive.collectAsState()
                val focusTotalSeconds by viewModel.focusTotalSeconds.collectAsState()
                val focusRemainingSeconds by viewModel.focusRemainingSeconds.collectAsState()
                val focusCompletedSuccessfully by viewModel.focusCompletedSuccessfully.collectAsState()

                val simAppName by viewModel.simulatingAppName.collectAsState()
                val simPackageName by viewModel.simulatingPackageName.collectAsState()
                val simCategory by viewModel.simulatingCategory.collectAsState()
                val simulatedSeconds by viewModel.simulatedSeconds.collectAsState()
                val showBlocker by viewModel.showLimitBlocker.collectAsState()
                val blockerReason by viewModel.blockerReason.collectAsState()

                var currentTab by remember { mutableIntStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = when (currentTab) {
                                        0 -> "Usage Report"
                                        1 -> "Budget Limits"
                                        2 -> "Focus Workspace"
                                        3 -> "Habits & Rewards"
                                        else -> "Enforcement Simulator"
                                    },
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp
                                )
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.testTag("app_top_bar")
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.testTag("app_bottom_nav")
                        ) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 },
                                icon = {
                                    Icon(Icons.Default.Leaderboard, contentDescription = "Dashboard Reports")
                                },
                                label = { Text("Dashboard") },
                                modifier = Modifier.testTag("tab_dashboard")
                            )

                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                icon = {
                                    Icon(Icons.Default.HourglassTop, contentDescription = "Limits Targets")
                                },
                                label = { Text("Limits") },
                                modifier = Modifier.testTag("tab_limits")
                            )

                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                icon = {
                                    Icon(Icons.Default.Spa, contentDescription = "Focus Mode")
                                },
                                label = { Text("Focus") },
                                modifier = Modifier.testTag("tab_focus")
                            )

                            NavigationBarItem(
                                selected = currentTab == 3,
                                onClick = { currentTab = 3 },
                                icon = {
                                    Icon(Icons.Default.EmojiEvents, contentDescription = "Rewards cabinet")
                                },
                                label = { Text("Badges") },
                                modifier = Modifier.testTag("tab_badges")
                            )

                            NavigationBarItem(
                                selected = currentTab == 4,
                                onClick = { currentTab = 4 },
                                icon = {
                                    Icon(Icons.Default.Timer, contentDescription = "Enforcement Simulator")
                                },
                                label = { Text("Simulator") },
                                modifier = Modifier.testTag("tab_simulator")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            0 -> DashboardScreen(
                                usageToday = usageToday,
                                appLimits = appLimits,
                                categoryLimits = categoryLimits,
                                onAddUsage = { name, pkg, cat, mins ->
                                    viewModel.addManualUsage(name, pkg, cat, mins)
                                }
                            )
                            1 -> LimitsScreen(
                                appLimits = appLimits,
                                categoryLimits = categoryLimits,
                                parentalSettings = parentalSettings,
                                isParentViewUnlocked = isParentViewUnlocked,
                                onTryUnlockParent = { pin ->
                                    viewModel.tryUnlockParentMode(pin)
                                },
                                onLockParentView = {
                                    viewModel.lockParentView()
                                },
                                onToggleParentProtection = { enabled ->
                                    viewModel.toggleParentProtection(enabled)
                                },
                                onSaveParentPasscode = { pin ->
                                    viewModel.saveParentPasscode(pin)
                                },
                                onUpdateParentBlockedPackages = { list ->
                                    viewModel.updateParentBlockedPackages(list)
                                },
                                onToggleParentBlockingActive = { active ->
                                    viewModel.toggleParentBlockingActive(active)
                                },
                                onUpdateCategoryLimit = { cat, mins, enabled ->
                                    viewModel.updateCategoryLimit(cat, mins, enabled)
                                },
                                onUpdateAppLimit = { pkg, name, cat, mins, enabled ->
                                    viewModel.updateAppLimit(pkg, name, cat, mins, enabled)
                                },
                                onDeleteAppLimit = { pkg ->
                                    viewModel.deleteAppLimit(pkg)
                                }
                            )
                            2 -> FocusScreen(
                                isFocusActive = isFocusActive,
                                focusTotalSeconds = focusTotalSeconds,
                                focusRemainingSeconds = focusRemainingSeconds,
                                focusCompletedSuccessfully = focusCompletedSuccessfully,
                                onStartFocus = { minutes ->
                                    viewModel.startFocusMode(minutes)
                                },
                                onStopFocus = {
                                    viewModel.stopFocusMode()
                                },
                                onDismissSuccessAnnouncement = {
                                    viewModel.dismissFocusSuccessAnnouncement()
                                }
                            )
                            3 -> GamificationScreen(
                                userXp = userXp,
                                streakDays = streakDays,
                                challenges = challenges,
                                rewards = rewards,
                                onProgressChallenge = { chalId, increment ->
                                    viewModel.progressChallenge(chalId, increment)
                                }
                            )
                            else -> SimulatorScreen(
                                simAppName = simAppName,
                                simPackageName = simPackageName,
                                simCategory = simCategory,
                                simulatedSeconds = simulatedSeconds,
                                showBlocker = showBlocker,
                                blockerReason = blockerReason,
                                onStartSimulation = { name, pkg, cat ->
                                    viewModel.startSimulation(name, pkg, cat)
                                },
                                onStopSimulation = {
                                    viewModel.stopSimulation()
                                },
                                onOverrideLimit = { packageName, addMinutes ->
                                    val currentLimit = appLimits.find { it.packageName == packageName }
                                    if (currentLimit != null) {
                                        viewModel.updateAppLimit(
                                            packageName = currentLimit.packageName,
                                            appName = currentLimit.appName,
                                            category = currentLimit.category,
                                            limitMins = currentLimit.limitMinutes + addMinutes,
                                            isEnabled = true
                                        )
                                    } else {
                                        simCategory?.let { activeCategory ->
                                            val currentCatLimit = categoryLimits.find { it.categoryName == activeCategory }
                                            if (currentCatLimit != null) {
                                                viewModel.updateCategoryLimit(
                                                    category = activeCategory,
                                                    limitMins = currentCatLimit.limitMinutes + addMinutes,
                                                    isEnabled = true
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
