package com.monkey.mediatimer.presentations.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.monkey.mediatimer.R
import com.monkey.mediatimer.common.TimerState
import com.monkey.mediatimer.presentations.components.CircularTimerDisplay
import com.monkey.mediatimer.presentations.components.MediaCard
import com.monkey.mediatimer.presentations.navigation.Screen
import com.monkey.mediatimer.presentations.viewmodel.HomeViewModel
import com.monkey.mediatimer.presentations.viewmodel.TimerViewModel
import com.monkey.mediatimer.utils.navigateWithPopupTo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Homescreen(
    nav: NavHostController,
    drawerState: DrawerState,
    homeViewModel: HomeViewModel = hiltViewModel(),
    timerViewModel: TimerViewModel = hiltViewModel() // this is just to make sure the view model is initialized when the screen is created. It's not used anywhere in this composable.
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    //val remainTimer by homeViewModel.remainTimer.collectAsState()
    val mediaSessions by homeViewModel.mediaSessions.collectAsState()

    Log.e("dan.nv", "Homescreen: checking for mediaSession ${mediaSessions}")

    /*LaunchedEffect(remainTimer) {
        while (remainTimer > 0) {
            delay(1000)
            homeViewModel.updateRemainTimer()
        }
    }*/

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            drawerState.apply {
                                if (isOpen) close() else open()
                            }
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    nav.navigateWithPopupTo(Screen.TimerScreen.route)
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_access_time_24),
                    contentDescription = "Add Timer"
                )
            }
        }
    ) { paddingValue ->
        HomeContent(
            paddingValues = paddingValue,
            homeViewModel = homeViewModel,
            timerViewModel = timerViewModel/*,
            onPlayPause = { packageName -> homeViewModel.togglePlayPauseState(packageName) },
            onStop = { packageName -> homeViewModel.stopMedia(packageName) },
            onVolumeChanged = { packageName -> homeViewModel.changeVolume(packageName) }*/
        )

    }
}

@Composable
fun HomeContent(
    paddingValues: PaddingValues,
    homeViewModel: HomeViewModel,
    timerViewModel: TimerViewModel/*,
    onPlayPause: (String) -> Unit,
    onStop: (String) -> Unit,
    onVolumeChanged: (String) -> Unit*/
) {
    val mediaInfos by homeViewModel.mediaSessions.collectAsState()
    val timerState by timerViewModel.timerState.collectAsState()
    val remainingTime by timerViewModel.remainTime.collectAsState()
    Log.e("dan.nv", "HomeContent: ${mediaInfos.size}")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ActiveTimerScreen(timerViewModel, 200.dp)
        /*if (timerState is TimerState.Active) {
            ActiveTimerInfo(
                timerState as TimerState.Active,
                remainingTime,
                onPauseResume = { timerViewModel.pauseOrResumeTimer() },
                onStop = { timerViewModel.cancelTimer() })
        }*/
        if (mediaInfos.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No active media found",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Start playing media on your device and it will appear here",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(mediaInfos) { mediaInfo ->
                    MediaCard(
                        mediaInfo,
                        onPlayPauseClick = {
                            homeViewModel.togglePlayPauseState(mediaInfo.packageName)
                        },
                        onStopClick = {
                            homeViewModel.stopMedia(mediaInfo.packageName)
                        },
                        onVolumeClicked = {
                            homeViewModel.changeVolume(mediaInfo.packageName)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveTimerInfo(
    timerState: TimerState.Active,
    timeRemaining: Long,
    onPauseResume: () -> Unit,
    onStop: () -> Unit
) {

    CircularTimerDisplay(
        timeRemaining = timeRemaining,
        totalTime = timerState.totalDurationMillis,
        isRunning = timerState is TimerState.Running,
        animationDurationMillis = 1000,
        200.dp,
        onPauseResume = onPauseResume,
        onStop = onStop
    )
    /*

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Timer Active",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Media will stop in ${formatTimeDisplay(activeTimer)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }*/
}