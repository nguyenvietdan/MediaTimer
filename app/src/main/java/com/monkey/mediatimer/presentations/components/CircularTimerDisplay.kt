package com.monkey.mediatimer.presentations.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monkey.mediatimer.R
import com.monkey.mediatimer.utils.formatTimeDisplay
import kotlin.math.min

@Composable
fun CircularTimerDisplay(
    timeRemaining: Long = 0,
    totalTime: Long = 120,
    isRunning: Boolean = false,
    animationDurationMillis: Int = 0,
    circleSize: Dp = 200.dp,
    onPauseResume: () -> Unit = {},
    onStop: () -> Unit = {},
) {
    val animatedProgress = remember { Animatable(1f) }
    val progress = if (totalTime > 0) timeRemaining.toFloat() / totalTime else 0f
    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = animationDurationMillis, easing = LinearEasing)
        )
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(circleSize)) {
            Canvas(modifier = Modifier.size(circleSize)) {
                val strokeWidth = 30.dp.value
                val diameter = min(size.width, size.height) - strokeWidth
                val radius = diameter / 2
                val topLeft = Offset(
                    x = (size.width - diameter) / 2,
                    y = (size.height - diameter) / 2
                )
                val arcSize = Size(width = diameter, height = diameter)
                drawArc(
                    color = Color.Gray/*MaterialTheme.colorScheme.surfaceVariant*/,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = Color.Red/*MaterialTheme.colorScheme.primary*/,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress.value,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Text(
                text = formatTimeDisplay(timeRemaining),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(62.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = onPauseResume) {
                Image(
                    painter = painterResource(if (isRunning) R.drawable.baseline_pause_circle_outline_24 else R.drawable.baseline_play_circle_outline_24),
                    contentDescription = if (isRunning) "Pause" else "Resume"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (isRunning) "Pause" else "Resume")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(onClick = onStop) {
                Image(
                    painter = painterResource(R.drawable.baseline_stop_circle_24),
                    contentDescription = "Stop"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Stop")
            }
        }
    }
}