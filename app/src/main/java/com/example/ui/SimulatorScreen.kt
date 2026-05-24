package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.NoAccounts
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueryBuilder
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SimulatibleApp(
    val name: String,
    val packageName: String,
    val category: String,
    val tagline: String,
    val gradientColors: List<Color>
)

@Composable
fun SimulatorScreen(
    simAppName: String?,
    simPackageName: String?,
    simCategory: String?,
    simulatedSeconds: Int,
    showBlocker: Boolean,
    blockerReason: String,
    onStartSimulation: (appName: String, packageName: String, category: String) -> Unit,
    onStopSimulation: () -> Unit,
    onOverrideLimit: (packageName: String, addMinutes: Int) -> Unit
) {
    val appsList = remember {
        listOf(
            SimulatibleApp("Instagram", "com.instagram.android", "Social Media", "Scrolling home feed and reels", listOf(Color(0xFFF1515E), Color(0xFFC13584))),
            SimulatibleApp("TikTok", "com.zhiliaoapp.musically", "Social Media", "Looping short entertainment videos", listOf(Color(0xFF000000), Color(0xFF00f5d4))),
            SimulatibleApp("YouTube", "com.google.android.youtube", "Entertainment", "Streaming HD tutorials and vloggers", listOf(Color(0xFFFF0000), Color(0xFFB31010))),
            SimulatibleApp("Clash of Clans", "com.supercell.clashofclans", "Games", "Coordinating clan raids and base upgrades", listOf(Color(0xFFEE9B00), Color(0xFFCA6702))),
            SimulatibleApp("Chess.com", "com.chess", "Games", "Pondering the Sicilian Defense response", listOf(Color(0xFF7FA650), Color(0xFF4C6A2F))),
            SimulatibleApp("Slack", "com.slack", "Productivity", "Responding to active workplace huddles", listOf(Color(0xFF4A154B), Color(0xFF36C5F0)))
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (simAppName == null) {
            // Pick App Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    Text(
                        text = "App Simulator",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Simulate launching social media or mobile games to witness your screening limits action reactively in real-time!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("simulator_grid")
                ) {
                    items(appsList) { app ->
                        AppSimCard(app = app, onLaunch = {
                            onStartSimulation(app.name, app.packageName, app.category)
                        })
                    }
                }
            }
        } else {
            // Under simulation view
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Info Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "LIVE SESSION MONITOR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val activeApp = appsList.find { it.packageName == simPackageName }
                    val brush = if (activeApp != null) {
                        Brush.linearGradient(colors = activeApp.gradientColors)
                    } else {
                        Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                    }

                    // Simulated Screen Container
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("simulated_screen"),
                        shape = RoundedCornerShape(32.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(4.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(32.dp))
                                .padding(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Dummy Phone Status bar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("9:41", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.Timer, contentDescription = "Battery", tint = Color.White, modifier = Modifier.size(14.dp))
                                        Text("100%", color = Color.White, fontSize = 12.sp)
                                    }
                                }

                                // Interactive Stopwatch Ticker
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    val pulseTransition = rememberInfiniteTransition(label = "pulse")
                                    val opacity by pulseTransition.animateFloat(
                                        initialValue = 0.4f,
                                        targetValue = 1f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1000),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "opacity"
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(Color.White.copy(alpha = 0.1f))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .alpha(opacity)
                                                .background(Color.Green)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "RUNNING $simAppName IN FOREGROUND",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Time Elapsed in Simulation
                                    Text(
                                        text = formatTime(simulatedSeconds),
                                        color = Color.White,
                                        fontSize = 54.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 2.sp
                                    )
                                    Text(
                                        text = "active time this session",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 12.sp
                                    )
                                }

                                // Interactive App Feed Placeholder
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.QueryBuilder,
                                        contentDescription = "Sim logo",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = activeApp?.tagline ?: "Displaying app session graphics",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Screentime accumulates here. If a limit config is reached, the device locks automatically.",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action controls
                Button(
                    onClick = onStopSimulation,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("exit_sim_button")
                ) {
                    Text("Terminate Session", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Limit Blocker Overlay
        if (showBlocker && simAppName != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .padding(24.dp)
                    .clickable(enabled = false) {}, // absorb clicks
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = "Blocked Symbol",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "DAILY BUDGET EXCEEDED!",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = blockerReason,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "The system has restricted accessibility for $simAppName under the Screen Time enforcement rule. Take a break!",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onStopSimulation,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("blocker_close_button")
                        ) {
                            Text("Exit & Lock Application", fontWeight = FontWeight.Black)
                        }

                        Button(
                            onClick = {
                                onOverrideLimit(simPackageName ?: "", 5) // Add 5 minutes override!
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(50))
                                .testTag("blocker_extend_button")
                        ) {
                            Icon(Icons.Default.LockReset, contentDescription = "Extend limit")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Emergency Extend (+5 MinsLimit)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppSimCard(app: SimulatibleApp, onLaunch: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onLaunch() }
            .testTag("launch_sim_card_${app.packageName}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(colors = app.gradientColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.name.firstOrNull()?.toString() ?: "A",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Icon(
                    Icons.Default.Launch,
                    contentDescription = "Launch",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = app.category,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}
