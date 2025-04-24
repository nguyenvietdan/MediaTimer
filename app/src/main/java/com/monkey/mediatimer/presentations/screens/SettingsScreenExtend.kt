package com.monkey.mediatimer.presentations.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.monkey.mediatimer.common.SettingItemData
import com.monkey.mediatimer.common.SettingSwitchItemData
import com.monkey.mediatimer.common.SettingValueItemData
import com.monkey.mediatimer.common.SettingsActionItem
import com.monkey.mediatimer.presentations.components.DefaultWheelMinutesTimePicker
import com.monkey.mediatimer.presentations.viewmodel.DurationType
import com.monkey.mediatimer.presentations.viewmodel.MediaViewModel
import com.monkey.mediatimer.presentations.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenExtend(
    navController: NavHostController,
    drawerState: DrawerState,
    mediaViewModel: MediaViewModel,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu"
                        )
                    }
                }
            )
        }
    ) { padding ->
        SettingsScreenContentExtend(padding, settingsViewModel, mediaViewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContentExtend(
    paddingValues: PaddingValues,
    settingsViewModel: SettingsViewModel,
    mediaViewModel: MediaViewModel
) {
    val maxTimerDuration by settingsViewModel.sharedPrefs.maxTimerDuration.collectAsState()
    val defaultTimeDuration by settingsViewModel.sharedPrefs.defaultTimerDuration.collectAsState()
    val vibrateOnCompletion by settingsViewModel.sharedPrefs.vibrateOnCompletion.collectAsState()
    val startOnBoot by settingsViewModel.sharedPrefs.startOnBoot.collectAsState()
    val autoUpUpdate by settingsViewModel.sharedPrefs.autoCheckForUpdates.collectAsState()
    val darkMode by settingsViewModel.sharedPrefs.darkMode.collectAsState()
    val useSystemTheme by settingsViewModel.sharedPrefs.useSystemTheme.collectAsState()
    val sleepModeEnabled by settingsViewModel.sharedPrefs.sleepModeEnabled.collectAsState()
    val gradualVolumeReductionEnabled by settingsViewModel.sharedPrefs.gradualVolumeReductionEnabled.collectAsState()
    val screenDimmingEnabled by settingsViewModel.sharedPrefs.screenDimmingEnabled.collectAsState()

    val writeSettingsPermission by mediaViewModel.hasWriteSettingsPermission.collectAsState()
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val editingType by settingsViewModel.editingType.collectAsState()
    //val max by settingsViewModel.maxDuration.collectAsState()
    //val default by settingsViewModel.defaultDuration.collectAsState()
    val showSheet = editingType != null

    if (showSheet) {
        val selectedValue = when (editingType) {
            DurationType.MAX -> maxTimerDuration
            DurationType.DEFAULT -> defaultTimeDuration
            else -> 0
        }
        val maxValue = when (editingType) {
            DurationType.MAX -> 120
            DurationType.DEFAULT -> maxTimerDuration
            else -> 0
        }

        DurationWheelPickerDialog(
            selected = selectedValue.toInt(),
            maxValue = maxValue.toInt(),
            onDismiss = { settingsViewModel.closeSheet() },
            onConfirm = { minutes ->
                when (editingType) {
                    DurationType.MAX -> settingsViewModel.updateMaxTimer(minutes.toLong())
                    DurationType.DEFAULT -> settingsViewModel.updateDefaultTimeDuration(minutes.toLong())
                    else -> {}
                }
                settingsViewModel.closeSheet()
            }
        )

        /*ModalBottomSheet(
            onDismissRequest = { settingsViewModel.closeSheet() },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            DurationBottomSheetContent(
                selectedValue = selectedValue.toInt(),
                onSelect = { settingsViewModel.onDurationSelected(it) },
                onCancel = { settingsViewModel.closeSheet() }
            )
        }*/
    }

    LazyColumn(
        modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            SettingGroup(
                title = "Timer Settings",
                items = listOf(
                    SettingValueItemData("Maximum Timer Duration", "$maxTimerDuration phút") {
                        settingsViewModel.openDurationSheet(DurationType.MAX)
                    },
                    SettingValueItemData("Default Timer Duration", "$defaultTimeDuration phút") {
                        settingsViewModel.openDurationSheet(DurationType.DEFAULT)
                    },
                    SettingSwitchItemData(
                        title = "Vibrate on Completion",
                        subTitle = "Vibrate when timer completes",
                        checked = vibrateOnCompletion,
                        onToggle = { settingsViewModel.updateVibrateOnCompletion(it) }
                    )
                )
            )
        }
        item {
            SettingGroup(
                title = "App Settings",
                items = listOf(
                    SettingSwitchItemData(
                        title = "Start on Boot",
                        subTitle = "Automatically start the app when device boots up",
                        checked = startOnBoot,
                        onToggle = { settingsViewModel.updateStartOnBoot(it) }
                    ),
                    SettingSwitchItemData(
                        title = "Auto check for update",
                        subTitle = "Automatically check for app updates",
                        checked = autoUpUpdate,
                        onToggle = { settingsViewModel.updateAutoUpdate(it) }
                    )

                )
            )
        }

        item {
            SettingGroup(
                title = "UI Settings",
                items = listOf(
                    SettingSwitchItemData(
                        title = "Dark mode",
                        subTitle = "Use dark mode theme for the app",
                        checked = darkMode,
                        onToggle = { settingsViewModel.updateDarkMode(it) }
                    ),
                    SettingSwitchItemData(
                        title = "System theme",
                        subTitle = "Follow system dark/light theme settings",
                        checked = useSystemTheme,
                        onToggle = { settingsViewModel.updateUseSystemTheme(it) }
                    )

                )
            )
        }

        item {
            val items = mutableListOf<SettingItemData>(
                SettingSwitchItemData(
                    title = "Sleep mode",
                    subTitle = "Enabled sleep mode",
                    checked = sleepModeEnabled,
                    onToggle = { settingsViewModel.updateSleepMode(it) }
                ),
                SettingSwitchItemData(
                    title = "Gradual volume reduction",
                    subTitle = "Gradual volume reduction when the timer completes",
                    checked = gradualVolumeReductionEnabled,
                    enabled = sleepModeEnabled,
                    onToggle = { settingsViewModel.updateGradualVolumeEnabled(it) }
                ),
                SettingSwitchItemData(
                    title = "Screen dimming reduction",
                    subTitle = "Reduce screen dimming when timer is running",
                    checked = screenDimmingEnabled,
                    enabled = sleepModeEnabled,
                    onToggle = { settingsViewModel.updateScreenDimmingEnabled(it) }
                )
            )
            if (!writeSettingsPermission) {
                items.add(
                    SettingsActionItem(
                        title = "Register system permission",
                        onClick = {
                            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                                data = "package:${context.packageName}".toUri()
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    )
                )
            }
            SettingGroup(
                title = "Sleep mode settings",
                items = items
            )
        }

        // Add more SettingGroups here for AppSettings, UiSettings...
    }
}


@Composable
fun SettingGroup(title: String, items: List<SettingItemData>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp))
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        items.forEach { item ->
            when (item) {
                is SettingSwitchItemData -> SwitchRow(
                    title = item.title,
                    subTitle = item.subTitle,
                    checked = item.checked,
                    enabled = item.enabled,
                    onToggle = item.onToggle
                )

                is SettingValueItemData -> ValueRow(item.title, item.value, item.onClick)
                is SettingsActionItem -> ActionRow(item.title, item.onClick)
            }
        }
    }
}

@Composable
fun SwitchRow(
    title: String,
    subTitle: String = "",
    checked: Boolean,
    enabled: Boolean = true,
    onToggle: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.titleMedium) },
        supportingContent = { Text(subTitle, style = MaterialTheme.typography.bodyMedium) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onToggle, enabled = enabled)
        },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun ValueRow(title: String, value: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.titleMedium) },
        supportingContent = { Text(value, style = MaterialTheme.typography.bodyMedium) },
        trailingContent = {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        },
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 0.dp)
            .clickable { onClick() },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun ActionRow(title: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.titleMedium) },
        trailingContent = {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        },
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 0.dp)
            .clickable { onClick() },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun DurationBottomSheetContent(
    onSelect: (Int) -> Unit,
    onCancel: () -> Unit,
    selectedValue: Int
) {
    val durations = (5..120 step 5).toList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        Text(
            "Chọn thời lượng",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn {
            items(durations) { duration ->
                val isSelected = duration == selectedValue
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(duration) }
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                            else Color.Transparent
                        )
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$duration phút",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("Hủy")
        }
    }
}


@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun SettingsSwitchItem(
    title: String,
    subTitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {

    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = { Text(subTitle) },
            trailingContent = {
                Switch(checked = checked, onCheckedChange = onToggle)
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun SettingsListActionItem(
    title: String,
    subTitle: String,
    icon: ImageVector,
    trailingIcon: ImageVector,
    trailingTint: Color = LocalContentColor.current,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = { Text(subTitle) },
            leadingContent = {
                Icon(icon, contentDescription = null)
            },
            trailingContent = {
                Icon(trailingIcon, contentDescription = null, tint = trailingTint)
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DurationWheelPickerDialog(
    selected: Int,
    maxValue: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(selected) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(currentIndex) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = {
            DefaultWheelMinutesTimePicker(
                startTime = currentIndex,
                maxTime = maxValue,
                onSelectedMinutes = {
                    currentIndex = it
                }
            )
        }
    )
}