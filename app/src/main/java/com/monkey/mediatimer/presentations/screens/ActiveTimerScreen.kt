package com.monkey.mediatimer.presentations.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.monkey.mediatimer.common.TimerState
import com.monkey.mediatimer.presentations.components.CircularTimerDisplay
import com.monkey.mediatimer.presentations.viewmodel.TimerViewModel

@Composable
fun ActiveTimerScreen(
    timerViewModel: TimerViewModel,
    circleSize: Dp,
    bottomSpace: Dp = 0.dp, // for devices with a navigation bar at the bottom (e.g. tablets)
) {
    val timeRemaining by timerViewModel.remainTime.collectAsState()
    val timerState by timerViewModel.sharedViewModel.timerState.collectAsState()
    if (timerState is TimerState.Active) {
        CircularTimerDisplay(
            timeRemaining = timeRemaining,
            totalTime = (timerState as TimerState.Active).totalDurationMillis,
            isRunning = timerState is TimerState.Running,
            animationDurationMillis = 1000,
            circleSize = circleSize,
            bottomSpace = bottomSpace,
            onPauseResume = { timerViewModel.pauseOrResumeTimer() },
            onStop = { timerViewModel.cancelTimer() }
        )
    }
}