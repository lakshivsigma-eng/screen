package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Challenge
import com.example.data.Reward

@Composable
fun GamificationScreen(
    userXp: Int,
    streakDays: Int,
    challenges: List<Challenge>,
    rewards: List<Reward>,
    onProgressChallenge: (id: String, inc: Int) -> Unit
) {
    var selectedBadgeForDetail by remember { mutableStateOf<Reward?>(null) }

    // Constants for XP Level
    val xpPerLevel = 500
    val currentLevel = (userXp / xpPerLevel) + 1
    val currentLevelXp = userXp % xpPerLevel
    val xpProgress = currentLevelXp.toFloat() / xpPerLevel.toFloat()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. HERO PROFILE & XP SUMMARY
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("xp_profile_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LEVEL $currentLevel ARCHMAGE",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Keep up the great habits which guard your productivity",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Stars,
                                contentDescription = "Rank Icon",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "$currentLevelXp / $xpPerLevel XP",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Next Level: ${xpPerLevel - currentLevelXp} XP to go",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { xpProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                    )
                }
            }
        }

        // 2. STREAKS RADAR WIDGET
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("streak_widget"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.errorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak Fire",
                            tint = Color(0xFFFF5722),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$streakDays Day Habit Streak",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "Keep daily social limits checked and execute focus loops to protect your streak!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        // 3. DAILY CHALLENGES SECTION
        item {
            Text(
                text = "Active Screen Challenges",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Accomplish these action items today to level up quickly",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        if (challenges.isEmpty()) {
            item {
                Text(
                    text = "No current active challenges.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(challenges) { challenge ->
                ChallengeRow(
                    challenge = challenge,
                    onSolveTrigger = {
                        // Increase simulator progress key inside Room
                        onProgressChallenge(challenge.id, 10)
                    }
                )
            }
        }

        // 4. VIRTUAL REWARD BADGES GRID
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Virtual Rewards & Badges",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Tap on any unlocked badge to examine details",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // We can display badges in standard Row containers
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Break rewards into chunks of 2 for grid layout style
                val chunks = rewards.chunked(2)
                chunks.forEach { rowRewards ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowRewards.forEach { reward ->
                            BadgeCard(
                                reward = reward,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedBadgeForDetail = reward
                                }
                            )
                        }
                        if (rowRewards.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(96.dp))
        }
    }

    // Detail Dialog
    selectedBadgeForDetail?.let { badge ->
        BadgeDetailDialog(
            reward = badge,
            onDismiss = { selectedBadgeForDetail = null }
        )
    }
}

@Composable
fun ChallengeRow(
    challenge: Challenge,
    onSolveTrigger: () -> Unit
) {
    val progressFraction = if (challenge.targetValue > 0) {
        challenge.currentValue.toFloat() / challenge.targetValue.toFloat()
    } else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("challenge_row_${challenge.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (challenge.isCompleted) {
                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = challenge.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (challenge.isCompleted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "+${challenge.rewardedXp} XP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Text(
                    text = challenge.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { progressFraction.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (challenge.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "${challenge.currentValue}/${challenge.targetValue}m",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (challenge.isCompleted) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(32.dp).testTag("challenge_done_${challenge.id}")
                )
            } else {
                // Mock Progress Trigger Button so kids can interactively complete challenges for evaluation!
                IconButton(
                    onClick = onSolveTrigger,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .testTag("progress_challenge_btn_${challenge.id}")
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Simulate progress",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun BadgeCard(
    reward: Reward,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val vectorIcon = when (reward.iconName) {
        "ic_shield" -> Icons.Default.Shield
        "ic_spa" -> Icons.Default.Spa
        "ic_military_tech" -> Icons.Default.MilitaryTech
        "ic_emoji_events" -> Icons.Default.EmojiEvents
        else -> Icons.Default.WorkspacePremium
    }

    Card(
        modifier = modifier
            .testTag("badge_card_${reward.id}")
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (reward.isUnlocked) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        if (reward.isUnlocked) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = vectorIcon,
                    contentDescription = reward.title,
                    tint = if (reward.isUnlocked) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    },
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = reward.title,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = if (reward.isUnlocked) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )

            Text(
                text = if (reward.isUnlocked) "UNLOCKED" else "LOCKED",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (reward.isUnlocked) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun BadgeDetailDialog(
    reward: Reward,
    onDismiss: () -> Unit
) {
    val vectorIcon = when (reward.iconName) {
        "ic_shield" -> Icons.Default.Shield
        "ic_spa" -> Icons.Default.Spa
        "ic_military_tech" -> Icons.Default.MilitaryTech
        "ic_emoji_events" -> Icons.Default.EmojiEvents
        else -> Icons.Default.WorkspacePremium
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                vectorIcon,
                contentDescription = reward.title,
                tint = if (reward.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(48.dp)
            )
        },
        title = { Text(text = reward.title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = reward.description,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Requires: At least ${reward.requiredXp} XP milestone",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (reward.isUnlocked) "Status: Claimed Flawlessly (Earned)" else "Status: Locked",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = if (reward.isUnlocked) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Great!")
            }
        }
    )
}
