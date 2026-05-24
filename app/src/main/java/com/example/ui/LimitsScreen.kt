package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLimit
import com.example.data.CategoryLimit
import com.example.data.ParentalSettings

@Composable
fun LimitsScreen(
    appLimits: List<AppLimit>,
    categoryLimits: List<CategoryLimit>,
    parentalSettings: ParentalSettings?,
    isParentViewUnlocked: Boolean,
    onTryUnlockParent: (pin: String) -> Boolean,
    onLockParentView: () -> Unit,
    onToggleParentProtection: (Boolean) -> Unit,
    onSaveParentPasscode: (String) -> Unit,
    onUpdateParentBlockedPackages: (String) -> Unit,
    onToggleParentBlockingActive: (Boolean) -> Unit,
    onUpdateCategoryLimit: (category: String, limitMins: Int, isEnabled: Boolean) -> Unit,
    onUpdateAppLimit: (packageName: String, appName: String, category: String, limitMins: Int, isEnabled: Boolean) -> Unit,
    onDeleteAppLimit: (packageName: String) -> Unit
) {
    var showAddAppLimitDialog by remember { mutableStateOf(false) }
    var showUnlockDialog by remember { mutableStateOf(false) }
    var showBlockedListSettingsDialog by remember { mutableStateOf(false) }
    var showPinChangeDialog by remember { mutableStateOf(false) }

    // State for temporary password attempt message
    var unlockErrorMessage by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. GUARDIAN ACCESS GATEWAY HERO CARD
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("parent_gate_hero"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isParentViewUnlocked) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isParentViewUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = "Lock indicator",
                                tint = if (isParentViewUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isParentViewUnlocked) "Guardian Console Active" else "Protected Child Mode",
                                    fontWeight = FontWeight.Black,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = if (isParentViewUnlocked) "You have root admin override" else "PIN required to edit daily budgets",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        if (isParentViewUnlocked) {
                            Button(
                                onClick = onLockParentView,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("button_lock_parentivew")
                            ) {
                                Text("Lock Console", fontSize = 12.sp)
                            }
                        } else {
                            Button(
                                onClick = {
                                    unlockErrorMessage = ""
                                    showUnlockDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.testTag("button_unlock_parentivew")
                            ) {
                                Text("Parent Entry", fontSize = 12.sp)
                            }
                        }
                    }

                    // Show Parent Panel if fully authorized
                    AnimatedVisibility(visible = isParentViewUnlocked) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Parent Overrides & Configurations",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Switch 1: Toggle complete parental limits validation on/off
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Activate Parental Guard", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Enforces all budget limits & restrictions on this device.", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                }
                                Switch(
                                    checked = parentalSettings?.isEnabled ?: false,
                                    onCheckedChange = { onToggleParentProtection(it) },
                                    thumbContent = {
                                        Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(12.dp))
                                    },
                                    modifier = Modifier.testTag("toggle_parent_guard")
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Switch 2: Blackout / Complete app blocking toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Absolute Package Blackout", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Blocks apps on blocklist completely regardless of time.", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                }
                                Switch(
                                    checked = parentalSettings?.isAppBlockingActive ?: false,
                                    onCheckedChange = { onToggleParentBlockingActive(it) },
                                    modifier = Modifier.testTag("toggle_parent_blackout")
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Secondary Admin Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showBlockedListSettingsDialog = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("button_manage_blocklist"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                                ) {
                                    Text("Edit Blocklist", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { showPinChangeDialog = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("button_change_pin"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer)
                                ) {
                                    Text("Modify Passcode", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. CATEGORY LIMITS BLOCK
        item {
            Spacer(modifier = Modifier.height(4.dp))
            SectionHeader(
                title = "Category Limits",
                subtitle = if (isParentViewUnlocked) "Adjust group folder constraints" else "Read-only: Locked under Child Mode"
            )
        }

        val allCategories = listOf("Social Media", "Games", "Entertainment", "Productivity", "Utilities")
        items(allCategories) { category ->
            val existing = categoryLimits.find { it.categoryName == category }
            val isEnabled = existing?.isEnabled ?: false
            val limitMins = existing?.limitMinutes ?: 45
            
            CategoryLimitRuleCard(
                categoryName = category,
                isEnabled = isEnabled,
                limitMinutes = limitMins,
                isInteractive = isParentViewUnlocked,
                onUpdate = { mins, enabled ->
                    if (isParentViewUnlocked) {
                        onUpdateCategoryLimit(category, mins, enabled)
                    } else {
                        showUnlockDialog = true
                    }
                }
            )
        }

        // 3. APP LIMITS BLOCK
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    title = "App-specific Rules",
                    subtitle = if (isParentViewUnlocked) "Admin configurations" else "Budget list overrides"
                )
                Button(
                    onClick = {
                        if (isParentViewUnlocked) {
                            showAddAppLimitDialog = true
                        } else {
                            unlockErrorMessage = "You must enter Guardian Passcode to append app limits."
                            showUnlockDialog = true
                        }
                    },
                    modifier = Modifier.testTag("add_app_limit_trigger_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Rule")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add App")
                }
            }
        }

        if (appLimits.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.HourglassTop, contentDescription = "Empty", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Narrow App Rules Preset",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap 'Add App' above to set screen time caps on narrow selected packages.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp)
                        )
                    }
                }
            }
        } else {
            items(appLimits, key = { it.packageName }) { appLimit ->
                AppLimitRuleCard(
                    rule = appLimit,
                    isInteractive = isParentViewUnlocked,
                    onUpdate = { pkg, name, cat, mins, enabled ->
                        if (isParentViewUnlocked) {
                            onUpdateAppLimit(pkg, name, cat, mins, enabled)
                        } else {
                            showUnlockDialog = true
                        }
                    },
                    onDelete = { pkg ->
                        if (isParentViewUnlocked) {
                            onDeleteAppLimit(pkg)
                        } else {
                            showUnlockDialog = true
                        }
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(84.dp))
        }
    }

    // --- DIALOGS CONTROLLING PARRENT MODE ---

    // 1. PIN Authentication Dialog
    if (showUnlockDialog) {
        var pinEntered by remember { mutableStateOf("") }
        var tempError by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { 
                showUnlockDialog = false 
                tempError = ""
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardian Passcode Entry")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Authentication required. Enter your 4-digit Parent PIN to access administrator controls. (Default: 1234)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = pinEntered,
                        onValueChange = { 
                            if (it.length <= 4) {
                                pinEntered = it
                            }
                        },
                        label = { Text("Parent PIN") },
                        placeholder = { Text("••••") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("parent_pin_input")
                    )
                    if (tempError.isNotEmpty()) {
                        Text(
                            text = tempError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = onTryUnlockParent(pinEntered)
                        if (success) {
                            showUnlockDialog = false
                            tempError = ""
                        } else {
                            tempError = "Incorrect PIN code. Please try again."
                        }
                    },
                    modifier = Modifier.testTag("parent_pin_submit")
                ) {
                    Text("Authorize")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showUnlockDialog = false
                        tempError = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // 2. Add App Limit Dialog (Standard)
    if (showAddAppLimitDialog) {
        AddAppLimitDialog(
            onDismiss = { showAddAppLimitDialog = false },
            onSave = { pkg, name, cat, mins ->
                onUpdateAppLimit(pkg, name, cat, mins, true)
                showAddAppLimitDialog = false
            }
        )
    }

    // 3. Modify PIN Dialog
    if (showPinChangeDialog) {
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPinChangeDialog = false },
            title = { Text("Modify Parent PIN") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 4) newPin = it },
                        label = { Text("New 4-digit PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_pin_input")
                    )

                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { if (it.length <= 4) confirmPin = it },
                        label = { Text("Confirm New PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("confirm_pin_input")
                    )

                    if (pinError.isNotEmpty()) {
                        Text(pinError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.length != 4) {
                            pinError = "PIN code must be exactly 4 digits."
                        } else if (newPin != confirmPin) {
                            pinError = "PIN inputs do not match."
                        } else {
                            onSaveParentPasscode(newPin)
                            showPinChangeDialog = false
                            pinError = ""
                        }
                    },
                    modifier = Modifier.testTag("pin_change_save")
                ) {
                    Text("Update PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinChangeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 4. Manage Blocked List Packages Comma Separated Dialog
    if (showBlockedListSettingsDialog) {
        var blockedListInput by remember { mutableStateOf(parentalSettings?.blockedPackagesCommaSeparated ?: "") }

        AlertDialog(
            onDismissRequest = { showBlockedListSettingsDialog = false },
            title = { Text("Completely Blocked Packages") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Specify absolute package identifiers to blockade completely. Any attempt to run these in the Simulator will hit zero tolerance locks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = blockedListInput,
                        onValueChange = { blockedListInput = it },
                        label = { Text("Comma-separated package names") },
                        placeholder = { Text("com.instagram.android, com.tiktok.android") },
                        modifier = Modifier.fillMaxWidth().testTag("blocklist_textfield")
                    )
                    Text(
                        "Sample: com.instagram.android, com.zhiliaoapp.musically",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateParentBlockedPackages(blockedListInput)
                        showBlockedListSettingsDialog = false
                    },
                    modifier = Modifier.testTag("blocklist_save")
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockedListSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CategoryLimitRuleCard(
    categoryName: String,
    isEnabled: Boolean,
    limitMinutes: Int,
    isInteractive: Boolean,
    onUpdate: (limitMinutes: Int, isEnabled: Boolean) -> Unit
) {
    var sliderValue by remember(limitMinutes) { mutableStateOf(limitMinutes.toFloat()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("category_card_$categoryName"),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Category,
                            contentDescription = categoryName,
                            tint = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEnabled) "${sliderValue.toInt()}m daily budget active" else "Uncapped folder",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { onUpdate(limitMinutes, it) },
                    colors = SwitchDefaults.colors(),
                    modifier = Modifier.testTag("category_switch_$categoryName")
                )
            }

            AnimatedVisibility(visible = isEnabled) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Set Budget Limit:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${sliderValue.toInt()} mins",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = sliderValue,
                        onValueChange = { if (isInteractive) sliderValue = it },
                        onValueChangeFinished = {
                            if (isInteractive) {
                                onUpdate(sliderValue.toInt(), isEnabled)
                            }
                        },
                        valueRange = 5f..240f,
                        steps = 46, // 5 min increments
                        enabled = isInteractive,
                        modifier = Modifier.testTag("category_slider_$categoryName")
                    )
                }
            }
        }
    }
}

@Composable
fun AppLimitRuleCard(
    rule: AppLimit,
    isInteractive: Boolean,
    onUpdate: (packageName: String, appName: String, category: String, limitMins: Int, isEnabled: Boolean) -> Unit,
    onDelete: (packageName: String) -> Unit
) {
    var sliderValue by remember(rule.limitMinutes) { mutableStateOf(rule.limitMinutes.toFloat()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_limit_card_${rule.packageName}"),
        colors = CardDefaults.cardColors(
            containerColor = if (rule.isEnabled) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rule.appName.firstOrNull()?.toString() ?: "A",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = rule.appName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = rule.category,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = rule.packageName,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = rule.isEnabled,
                        onCheckedChange = { onUpdate(rule.packageName, rule.appName, rule.category, rule.limitMinutes, it) },
                        modifier = Modifier.testTag("app_limit_switch_${rule.packageName}")
                    )
                    IconButton(
                        onClick = { onDelete(rule.packageName) },
                        modifier = Modifier.testTag("app_limit_delete_${rule.packageName}")
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Rule", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            AnimatedVisibility(visible = rule.isEnabled) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Budget Limit:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${sliderValue.toInt()} mins",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = sliderValue,
                        onValueChange = { if (isInteractive) sliderValue = it },
                        onValueChangeFinished = {
                            if (isInteractive) {
                                onUpdate(rule.packageName, rule.appName, rule.category, sliderValue.toInt(), rule.isEnabled)
                            }
                        },
                        valueRange = 5f..180f,
                        enabled = isInteractive,
                        steps = 34,
                        modifier = Modifier.testTag("app_limit_slider_${rule.packageName}")
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppLimitDialog(
    onDismiss: () -> Unit,
    onSave: (packageName: String, appName: String, category: String, limitMinutes: Int) -> Unit
) {
    var appName by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Social Media") }
    var limitMinsStr by remember { mutableStateOf("30") }

    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Social Media", "Games", "Entertainment", "Productivity", "Utilities")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restrict Specific App") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    label = { Text("App Name (e.g. Instagram)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("Package (e.g. com.instagram.android)") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = category,
                        onValueChange = {},
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    category = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = limitMinsStr,
                    onValueChange = { limitMinsStr = it },
                    label = { Text("Daily Budget (Minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalAppName = appName.ifBlank { "Unspecified App" }
                    val finalPkg = packageName.ifBlank { "com." + finalAppName.lowercase().replace(" ", "") }
                    val mins = limitMinsStr.toIntOrNull() ?: 30
                    onSave(finalPkg, finalAppName, category, mins)
                },
                enabled = appName.isNotEmpty() && limitMinsStr.isNotEmpty()
            ) {
                Text("Enforce")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}
