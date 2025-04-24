package com.monkey.mediatimer.presentations.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.monkey.mediatimer.presentations.theme.MediaTimerTheme
import com.monkey.mediatimer.presentations.theme.TimerGreen
import com.monkey.mediatimer.presentations.theme.TimerRed
import com.monkey.mediatimer.presentations.theme.TimerYellow
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CircularTimerSelector(
    selectedMinutes: Int = 0,
    maxMinutes: Int = 120,
    circleSize: Dp = 300.dp,
    onValueChange: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    //val circleSize = 300.dp
    val strokeWidth = 30.dp
    var center by remember { mutableStateOf(Offset.Zero) }

    val radius = circleSize.value

    // Calculate the color based on the selected time
    val timerColor = when {
        selectedMinutes < maxMinutes / 3 -> TimerGreen
        selectedMinutes < maxMinutes * 2 / 3 -> TimerYellow
        else -> TimerRed
    }

    Box(
        modifier = modifier
            .size(circleSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(circleSize)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val touchPoint = change.position
                        // Calculate angle from center to touch point
                        val angle = getAngle(center, touchPoint)
                        // Convert angle to minutes
                        val newMinutes = (angle / (2 * PI) * maxMinutes).toInt()
                        // Ensure minutes are within valid range newMinutes.coerceIn(0, maxMinutes)
                        val clampedMinutes =
                            if (newMinutes < 0) 0 else if (newMinutes > maxMinutes) maxMinutes else newMinutes
                        onValueChange(clampedMinutes)
                    }
                }
        ) {
            center = this.center

            // Draw background circle
            drawArc(
                color = Color.Gray.copy(alpha = 0.2f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(strokeWidth.value / 2, strokeWidth.value / 2),
                size = Size(size.width - strokeWidth.value, size.height - strokeWidth.value),
                style = Stroke(width = strokeWidth.value, cap = StrokeCap.Round)
            )

            // Draw timer progress
            val sweepAngle = (selectedMinutes.toFloat() / maxMinutes.toFloat()) * 360f
            if (sweepAngle > 0) {
                drawArc(
                    color = timerColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(strokeWidth.value / 2, strokeWidth.value / 2),
                    size = Size(size.width - strokeWidth.value, size.height - strokeWidth.value),
                    style = Stroke(width = strokeWidth.value, cap = StrokeCap.Round)
                )
            }

            // Draw the knob at the end of the progress
            if (selectedMinutes >= 0) {
                val angle =
                    (selectedMinutes.toFloat() / maxMinutes.toFloat()) * 2 * PI.toFloat() - PI.toFloat() / 2
                val knobRadius = 15.dp.toPx()
                val knobX = center.x + cos(angle) * (size.width - strokeWidth.value) / 2
                val knobY = center.y + sin(angle) * (size.height - strokeWidth.value ) / 2

                drawCircle(
                    color = timerColor,
                    radius = knobRadius,
                    center = Offset(knobX, knobY)
                )
            }
        }

        Text(
            text = "$selectedMinutes",
            style = MaterialTheme.typography.headlineMedium,
            color = timerColor
        )
    }
}

private fun getAngle(center: Offset, point: Offset): Double {
    val angle = atan2(point.y - center.y, point.x - center.x)

    // Convert to 0-2PI range
    val normalizedAngle = if (angle < 0) angle + 2 * PI else angle

    // Adjust to start from top (-PI/2) going clockwise
    val adjustedAngle = (normalizedAngle.toFloat() + PI / 2) % (2 * PI)

    return adjustedAngle
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CircularTimerSelectorPreview() {
    MediaTimerTheme {
        CircularTimerSelector(1, 120, 300.dp, {})
    }
}
