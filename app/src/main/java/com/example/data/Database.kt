package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "app_usage_records")
data class AppUsageRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val appName: String,
    val packageName: String,
    val category: String,
    val usedSeconds: Long,
    val dateStr: String // YYYY-MM-DD
)

@Entity(tableName = "app_limits")
data class AppLimit(
    @PrimaryKey val packageName: String,
    val appName: String,
    val category: String,
    val limitMinutes: Int,
    val isEnabled: Boolean
)

@Entity(tableName = "category_limits")
data class CategoryLimit(
    @PrimaryKey val categoryName: String,
    val limitMinutes: Int,
    val isEnabled: Boolean
)

@Entity(tableName = "gamification_challenges")
data class Challenge(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val targetValue: Int, // e.g. Max minutes allowed or required focus session minutes
    val currentValue: Int, // Active tracking progress
    val isCompleted: Boolean,
    val rewardedXp: Int,
    val category: String // "social", "focus", "games", "all"
)

@Entity(tableName = "gamification_rewards")
data class Reward(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val type: String, // "badge", "streak", "rank"
    val isUnlocked: Boolean,
    val requiredXp: Int,
    val iconName: String
)

@Entity(tableName = "parental_settings")
data class ParentalSettings(
    @PrimaryKey val id: Int = 1,
    val isEnabled: Boolean = false,
    val passcode: String = "", // 4 digit passcode
    val isAppBlockingActive: Boolean = false,
    val blockedPackagesCommaSeparated: String = "" // List of package ids blocked
)

@Dao
interface ScreenTimeDao {
    @Query("SELECT * FROM app_usage_records WHERE dateStr = :date ORDER BY usedSeconds DESC")
    fun getUsageRecordsForDate(date: String): Flow<List<AppUsageRecord>>

    @Query("SELECT * FROM app_usage_records ORDER BY dateStr DESC")
    fun getAllUsageRecords(): Flow<List<AppUsageRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUsageRecord(record: AppUsageRecord)

    @Query("SELECT * FROM app_usage_records WHERE packageName = :packageName AND dateStr = :date LIMIT 1")
    suspend fun getRecord(packageName: String, date: String): AppUsageRecord?

    // App limits queries
    @Query("SELECT * FROM app_limits")
    fun getAllAppLimits(): Flow<List<AppLimit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAppLimit(limit: AppLimit)

    @Query("DELETE FROM app_limits WHERE packageName = :packageName")
    suspend fun deleteAppLimit(packageName: String)

    // Category limits queries
    @Query("SELECT * FROM category_limits")
    fun getAllCategoryLimits(): Flow<List<CategoryLimit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCategoryLimit(limit: CategoryLimit)

    @Query("SELECT COUNT(*) FROM category_limits")
    suspend fun getCategoryLimitsCount(): Int

    @Query("SELECT COUNT(*) FROM app_usage_records")
    suspend fun getUsageRecordsCount(): Int

    // Gamification Challenges
    @Query("SELECT * FROM gamification_challenges")
    fun getAllChallenges(): Flow<List<Challenge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveChallenge(challenge: Challenge)

    @Query("SELECT COUNT(*) FROM gamification_challenges")
    suspend fun getChallengesCount(): Int

    // Gamification Rewards
    @Query("SELECT * FROM gamification_rewards")
    fun getAllRewards(): Flow<List<Reward>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReward(reward: Reward)

    @Query("SELECT COUNT(*) FROM gamification_rewards")
    suspend fun getRewardsCount(): Int

    // Parental Control Settings
    @Query("SELECT * FROM parental_settings WHERE id = 1 LIMIT 1")
    fun getParentalSettings(): Flow<ParentalSettings?>

    @Query("SELECT * FROM parental_settings WHERE id = 1 LIMIT 1")
    suspend fun getParentalSettingsDirect(): ParentalSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveParentalSettings(settings: ParentalSettings)
}

@Database(
    entities = [
        AppUsageRecord::class, 
        AppLimit::class, 
        CategoryLimit::class, 
        Challenge::class, 
        Reward::class, 
        ParentalSettings::class
    ], 
    version = 2, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun screenTimeDao(): ScreenTimeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "screen_time_db"
                )
                .fallbackToDestructiveMigration() // Handle upgrades elegantly for developer updates
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class ScreenTimeRepository(private val dao: ScreenTimeDao) {
    fun getUsageTodayFlow(): Flow<List<AppUsageRecord>> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return dao.getUsageRecordsForDate(today)
    }

    fun getAllUsageFlow(): Flow<List<AppUsageRecord>> = dao.getAllUsageRecords()
    fun getAppLimitsFlow(): Flow<List<AppLimit>> = dao.getAllAppLimits()
    fun getCategoryLimitsFlow(): Flow<List<CategoryLimit>> = dao.getAllCategoryLimits()
    
    // Gamification & Parental flows
    fun getChallengesFlow(): Flow<List<Challenge>> = dao.getAllChallenges()
    fun getRewardsFlow(): Flow<List<Reward>> = dao.getAllRewards()
    fun getParentalSettingsFlow(): Flow<ParentalSettings?> = dao.getParentalSettings()

    suspend fun logUsage(appName: String, packageName: String, category: String, seconds: Long) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val existing = dao.getRecord(packageName, today)
        if (existing != null) {
            dao.insertOrUpdateUsageRecord(
                existing.copy(usedSeconds = existing.usedSeconds + seconds)
            )
        } else {
            dao.insertOrUpdateUsageRecord(
                AppUsageRecord(
                    appName = appName,
                    packageName = packageName,
                    category = category,
                    usedSeconds = seconds,
                    dateStr = today
                )
            )
        }
    }

    suspend fun saveAppLimit(limit: AppLimit) = dao.saveAppLimit(limit)
    suspend fun deleteAppLimit(packageName: String) = dao.deleteAppLimit(packageName)
    suspend fun saveCategoryLimit(limit: CategoryLimit) = dao.saveCategoryLimit(limit)

    suspend fun saveChallenge(challenge: Challenge) = dao.saveChallenge(challenge)
    suspend fun saveReward(reward: Reward) = dao.saveReward(reward)
    suspend fun saveParentalSettings(settings: ParentalSettings) = dao.saveParentalSettings(settings)
    suspend fun getParentalSettingsDirect(): ParentalSettings? = dao.getParentalSettingsDirect()

    suspend fun prePopulateIfEmpty() {
        // Initialize standard categories
        if (dao.getCategoryLimitsCount() == 0) {
            val defaultCategories = listOf(
                CategoryLimit("Social Media", 60, true),
                CategoryLimit("Games", 45, true),
                CategoryLimit("Entertainment", 90, true),
                CategoryLimit("Productivity", 120, false),
                CategoryLimit("Utilities", 30, false)
            )
            for (category in defaultCategories) {
                dao.saveCategoryLimit(category)
            }
        }

        // Initialize standard usage logs
        if (dao.getUsageRecordsCount() == 0) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val initialUsage = listOf(
                AppUsageRecord(appName = "Instagram", packageName = "com.instagram.android", category = "Social Media", usedSeconds = 2700, dateStr = today), // 45m
                AppUsageRecord(appName = "TikTok", packageName = "com.zhiliaoapp.musically", category = "Social Media", usedSeconds = 1320, dateStr = today), // 22m
                AppUsageRecord(appName = "Clash of Clans", packageName = "com.supercell.clashofclans", category = "Games", usedSeconds = 2100, dateStr = today), // 35m
                AppUsageRecord(appName = "Chess.com", packageName = "com.chess", category = "Games", usedSeconds = 720, dateStr = today), // 12m
                AppUsageRecord(appName = "Slack", packageName = "com.slack", category = "Productivity", usedSeconds = 2400, dateStr = today), // 40m
                AppUsageRecord(appName = "YouTube", packageName = "com.google.android.youtube", category = "Entertainment", usedSeconds = 5100, dateStr = today), // 85m
                AppUsageRecord(appName = "Google Maps", packageName = "com.google.android.apps.maps", category = "Utilities", usedSeconds = 900, dateStr = today) // 15m
            )
            for (rec in initialUsage) {
                dao.insertOrUpdateUsageRecord(rec)
            }

            dao.saveAppLimit(AppLimit("com.instagram.android", "Instagram", "Social Media", 30, true))
            dao.saveAppLimit(AppLimit("com.supercell.clashofclans", "Clash of Clans", "Games", 40, true))
            dao.saveAppLimit(AppLimit("com.slack", "Slack", "Productivity", 120, false))
        }

        // Initialize standard challenges
        if (dao.getChallengesCount() == 0) {
            val initialChallenges = listOf(
                Challenge(
                    id = "detox_social",
                    title = "Social Cleansing",
                    description = "Keep total Social Media screen time under 45 minutes today.",
                    targetValue = 45,
                    currentValue = 0,
                    isCompleted = false,
                    rewardedXp = 150,
                    category = "Social Media"
                ),
                Challenge(
                    id = "gaming_diet",
                    title = "Casual Mindset",
                    description = "Restrict mobile games to 20 minutes max today.",
                    targetValue = 20,
                    currentValue = 0,
                    isCompleted = false,
                    rewardedXp = 200,
                    category = "Games"
                ),
                Challenge(
                    id = "focus_sprint",
                    title = "Focus Masterclass",
                    description = "Achieve 25 consecutive minutes of offline Focus Mode.",
                    targetValue = 25,
                    currentValue = 0,
                    isCompleted = false,
                    rewardedXp = 250,
                    category = "focus"
                ),
                Challenge(
                    id = "screen_diet",
                    title = "Digital Fast",
                    description = "Total daily screen time under 180 minutes.",
                    targetValue = 180,
                    currentValue = 0,
                    isCompleted = false,
                    rewardedXp = 300,
                    category = "all"
                )
            )
            for (chal in initialChallenges) {
                dao.saveChallenge(chal)
            }
        }

        // Initialize badges and levels
        if (dao.getRewardsCount() == 0) {
            val initialRewards = listOf(
                Reward("badge_warrior", "Digital Warrior", "Completed first screen limit restriction flawlessly.", "badge", false, 100, "ic_shield"),
                Reward("badge_zen", "Zen Master", "Achieved a 5-day screen limitation streak.", "badge", false, 300, "ic_spa"),
                Reward("badge_focus_lord", "Monastic Focus", "Survived 3 consecutive Focus Mode sprints.", "badge", false, 500, "ic_military_tech"),
                Reward("badge_diet_king", "Screen Detox King", "Completed the Digital Fast limit challenge.", "badge", false, 750, "ic_emoji_events"),
                Reward("tier_gold", "Platinum Guardian Class", "Earned 1000 accumulated experience points (XP).", "rank", false, 1000, "ic_stars")
            )
            for (rew in initialRewards) {
                dao.saveReward(rew)
            }
        }

        // Initialize Parental Control config
        if (dao.getParentalSettingsDirect() == null) {
            dao.saveParentalSettings(
                ParentalSettings(
                    id = 1,
                    isEnabled = false,
                    passcode = "1234", // Default easy developer parent code
                    isAppBlockingActive = true,
                    blockedPackagesCommaSeparated = "com.instagram.android" // Initially block Instagram as parental rule sample
                )
            )
        }
    }
}
