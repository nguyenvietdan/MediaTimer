package com.monkey.mediatimer.presentations.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.monkey.mediatimer.presentations.viewmodel.MediaViewModel
import com.monkey.mediatimer.presentations.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    drawerState: DrawerState,
    mediaViewModel: MediaViewModel,
    viewModel: SettingsViewModel = hiltViewModel()
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
        SettingsScreenContent(padding, viewModel, mediaViewModel)
    }
}

@Composable
fun SettingsScreenContent(
    paddingValues: PaddingValues,
    viewModel: SettingsViewModel,
    mediaViewModel: MediaViewModel
) {
    val maxTimerDuration by viewModel.sharedPrefs.maxTimerDuration.collectAsState()
    val defaultTimeDuration by viewModel.sharedPrefs.defaultTimerDuration.collectAsState()
    val vibrateOnCompletion by viewModel.sharedPrefs.vibrateOnCompletion.collectAsState()
    val startOnBoot by viewModel.sharedPrefs.startOnBoot.collectAsState()
    val autoUpUpdate by viewModel.sharedPrefs.autoCheckForUpdates.collectAsState()
    val darkMode by viewModel.sharedPrefs.darkMode.collectAsState()
    val useSystemTheme by viewModel.sharedPrefs.useSystemTheme.collectAsState()
    val sleepModeEnabled by viewModel.sharedPrefs.sleepModeEnabled.collectAsState()
    val gradualVolumeReductionEnabled by viewModel.sharedPrefs.gradualVolumeReductionEnabled.collectAsState()

    val writeSettingsPermission by mediaViewModel.hasWriteSettingsPermission.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp, vertical = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        SettingsSection(title = "Timer settings") {
            SettingsSlider(
                title = "Maximum timer duration",
                value = maxTimerDuration.toInt(),
                onValueChange = { viewModel.updateMaxTimer(it.toLong()) },
                valueRange = 5f..120f,
                steps = 23,
                valueDisplay = "$maxTimerDuration minutes",
                description = "Set the maximum duration available when setting a timer"
            )
            Spacer(modifier = Modifier.height(24.dp))
            SettingsSlider(
                title = "Default timer duration",
                value = defaultTimeDuration.toInt(),
                onValueChange = { viewModel.updateDefaultTimeDuration(it.toLong()) },
                valueRange = 1f..60f,
                steps = 59,
                valueDisplay = "$defaultTimeDuration minutes",
                description = "Default duration when starting a new timer"
            )
            Spacer(modifier = Modifier.height(24.dp))
            SettingsSwitch(
                title = "Vibrate on completion",
                isChecked = vibrateOnCompletion,
                onCheckedChange = { viewModel.updateVibrateOnCompletion(it) },
                description = "Vibrate when timer completes"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        SettingsSection(title = "App settings") {
            SettingsSwitch(
                title = "Start on boot",
                isChecked = startOnBoot,
                onCheckedChange = { viewModel.updateStartOnBoot(it) },
                description = "Automatically start the app when device boots up"
            )
            Spacer(modifier = Modifier.height(16.dp))
            SettingsSwitch(
                title = "Auto check for updates",
                isChecked = autoUpUpdate,
                onCheckedChange = { viewModel.updateAutoUpdate(it) },
                description = "Automatically check for app updates"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        SettingsSection(title = "UI Settings") {
            SettingsSwitch(
                title = "Dark mode",
                isChecked = darkMode,
                onCheckedChange = { viewModel.updateDarkMode(it) },
                description = "Use dark mode theme for the app"
            )
            Spacer(modifier = Modifier.height(16.dp))
            SettingsSwitch(
                title = "Use system theme",
                isChecked = useSystemTheme,
                onCheckedChange = { viewModel.updateUseSystemTheme(it) },
                description = "Follow system dark/light theme settings"
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        SettingsSection(title = "Sleep mode settings") {
            SettingsSwitch(
                title = "Sleep mode",
                isChecked = sleepModeEnabled,
                onCheckedChange = { viewModel.updateSleepMode(it) },
                description = "Enabled sleep mode"
            )
            Spacer(modifier = Modifier.height(16.dp))
            SettingsSwitch(
                title = "Gradual volume reduction",
                isChecked = gradualVolumeReductionEnabled,
                onCheckedChange = { viewModel.updateGradualVolumeEnabled(it) },
                description = "Gradual volume reduction when the timer finish",
                enabled = sleepModeEnabled
            )
            if (writeSettingsPermission) {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsButton(
                    title = "Register write system permission",
                    description = "The app need this permission for use reduce volume",
                    textButton = "Register"
                ) {
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                        data = "package:${context.packageName}".toUri()
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
                /*TextButton(onClick = {
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                        data = "package:${context.packageName}".toUri()
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }) {
                    Text(text = "Register write system permission", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "The app need this permission for use reduce volume", style = MaterialTheme.typography.bodySmall)
                }*/
            }

        }

    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Text(text = title, style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(16.dp))
    content()
}

@Composable
fun SettingsSlider(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueDisplay: String,
    description: String
) {
    Text(text = title, style = MaterialTheme.typography.bodyLarge)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = valueDisplay,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(8.dp))
    Slider(
        value = value.toFloat(),
        onValueChange = { onValueChange(it.toInt()) },
        valueRange = valueRange,
        steps = steps,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = description, style = MaterialTheme.typography.bodySmall)
}

@Composable
fun SettingsSwitch(
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String,
    enabled: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = description, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = isChecked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
fun SettingsButton(
    title: String,
    description: String,
    textButton: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = description, style = MaterialTheme.typography.bodySmall)
        }
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(textButton.uppercase(), style = MaterialTheme.typography.headlineLarge)
        }
    }
}