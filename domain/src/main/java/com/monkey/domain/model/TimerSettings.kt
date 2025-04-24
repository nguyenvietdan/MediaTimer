package com.monkey.domain.model

/**
 * Regarding timer setup
 */
data class TimerSettings(
    val maxTimerDurationMinutes: Long = 120L,
    val defaultTimerDurationMinutes: Long = 30L,
    val vibrateOnCompletion: Boolean = true,
    val timerCompletionSound: String = "default",
    val autoStartTimerOnDetection: Boolean = false
)
