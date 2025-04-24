package com.monkey.mediatimer.presentations.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.monkey.mediatimer.R
import com.monkey.mediatimer.common.TimerState
import com.monkey.mediatimer.presentations.components.CircularTimerSelector
import com.monkey.mediatimer.presentations.viewmodel.TimerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    nav: NavHostController,
    drawerState: DrawerState,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val selectedMinutes by viewModel.selectedMinutes.collectAsState()
    val maxTimerDurations by viewModel.maxTimeDuration.collectAsState()
    val timerState by viewModel.timerState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Set Timer") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "go back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            drawerState.apply {
                                if (isOpen) close() else open()
                            }
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "drawer menu")
                    }
                }
            )
        }
    ) { paddingValues ->
        TimerContent(
            paddings = paddingValues,
            selectedMinutes = selectedMinutes.toInt(),
            viewModel,
            maxTimerDuration = maxTimerDurations.toInt(),
            timerState = timerState,
            onMinutesSelected = { viewModel.updateSelectedMinutes(it.toLong()) },
            onStartTimer = {
                viewModel.startTimer()
            }
        )
    }
}

@Composable
fun TimerContent(
    paddings: PaddingValues,
    selectedMinutes: Int,
    viewModel: TimerViewModel,
    maxTimerDuration: Int,
    timerState: TimerState,
    onMinutesSelected: (Int) -> Unit,
    onStartTimer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddings)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (timerState !is TimerState.Inactive) {
            Text(
                text = stringResource(R.string.timer_active),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            ActiveTimerScreen(viewModel, 300.dp)
        } else {
            Text(
                text = stringResource(R.string.set_timer_to_stop_all_media),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularTimerSelector(
                selectedMinutes = selectedMinutes,
                maxMinutes = maxTimerDuration,
                circleSize = 300.dp,
                onValueChange = onMinutesSelected
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "$selectedMinutes minutes",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onStartTimer,
                modifier = Modifier.fillMaxWidth(0.7f),
                enabled = selectedMinutes > 0
            ) {
                Text(text = stringResource(R.string.start_timer))
            }
        }
    }
}