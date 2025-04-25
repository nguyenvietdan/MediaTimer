package com.monkey.mediatimer.presentations.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.monkey.mediatimer.R
import com.monkey.mediatimer.common.MediaInfo
import com.monkey.mediatimer.utils.formatTimeDisplay
import kotlinx.coroutines.delay

@Composable
fun MediaCard(
    mediaInfo: MediaInfo,
    onPlayPauseClick: () -> Unit,
    onStopClick: () -> Unit,
    onVolumeClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentPositionState by remember { mutableLongStateOf(mediaInfo.position) }
    var currentProgress by remember {
        mutableFloatStateOf(
            getProgress(
                currentPositionState,
                mediaInfo.duration
            )
        )
    }

    LaunchedEffect(mediaInfo.position) {
        currentPositionState = mediaInfo.position
        currentProgress = getProgress(currentPositionState, mediaInfo.duration)
    }

    LaunchedEffect(mediaInfo.isPlaying()) {
        while (mediaInfo.isPlaying() && currentPositionState < mediaInfo.duration) {
            delay(1000)
            if (mediaInfo.duration > 0) {
                currentPositionState += 1000
                if (currentPositionState > mediaInfo.duration) {
                    currentPositionState = mediaInfo.duration
                }
                currentProgress = getProgress(currentPositionState, mediaInfo.duration)
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (mediaInfo.artIcon != null || mediaInfo.appIcon != null) {
                    if (mediaInfo.artIcon != null) {
                        Image(
                            mediaInfo.artIcon.asImageBitmap(),
                            contentDescription = "artIcon",
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(mediaInfo.appIcon!!),
                            contentDescription = "App icon",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = mediaInfo.appName, style = MaterialTheme.typography.titleMedium)
                    Text(text = mediaInfo.title, style = MaterialTheme.typography.bodyMedium)
                    if (mediaInfo.artist.isNotEmpty()) {
                        Text(text = mediaInfo.artist, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (mediaInfo.duration > 0) {
                LinearProgressIndicator(
                    progress = { currentProgress },
                    modifier = Modifier.fillMaxWidth()
                )
                val timeRemaining = (mediaInfo.duration - currentPositionState).coerceAtLeast(0L)
                val formatedTimeRemaining = formatTimeDisplay(timeRemaining)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatedTimeRemaining,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = formatTimeDisplay(mediaInfo.duration),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = {
                    onVolumeClicked()
                }) {
                    Icon(
                        painterResource(R.drawable.baseline_volume_up_24),
                        contentDescription = "volume"
                    )
                }

                IconButton(onClick = {
                    onPlayPauseClick()
                }) {
                    Icon(
                        painterResource(if (mediaInfo.isPlaying()) R.drawable.baseline_pause_circle_outline_24 else R.drawable.baseline_play_circle_outline_24),
                        contentDescription = if (mediaInfo.isPlaying()) "Pause" else "Play"
                    )
                }

                IconButton(onClick = {
                    onStopClick()
                }) {
                    Icon(
                        painterResource(R.drawable.baseline_stop_circle_24),
                        contentDescription = "stop"
                    )
                }
            }
        }
    }
}

private fun getProgress(currentPosition: Long, duration: Long): Float {
    return if (duration > 0) {
        currentPosition.toFloat() / duration.toFloat()
    } else 0f
}