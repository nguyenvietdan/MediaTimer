package com.monkey.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface BatterySharedPreferenceRepository {
    // Battery
    val enableOptimizations: StateFlow<Boolean>
    val reduceBrightness: StateFlow<Boolean>
    val lowBatteryBrightness: StateFlow<Float>
    val limitTimerDuration: StateFlow<Boolean>
    val lowBatteryMaxTimerDefault: StateFlow<Int>
    val originalBrightness: StateFlow<Int>
    val lowBatteryMaxTimer: StateFlow<Int>

    val scheduledTimer: StateFlow<String>

    suspend fun save(key: String, value: Any)

    companion object {
        const val BATTERY_PREFERENCES_NAME = "battery_optimization_preferences"
        // Battery
        const val KEY_ENABLE_OPTIMIZATIONS = "enable_battery_optimizations"
        const val KEY_REDUCE_BRIGHTNESS = "reduce_brightness_on_low_battery"
        const val KEY_LOW_BATTERY_BRIGHTNESS = "low_battery_brightness_level"
        const val KEY_LIMIT_TIMER_DURATION = "limit_timer_duration_on_low_battery"
        const val KEY_LOW_BATTERY_MAX_TIMER_DEFAULT = "low_battery_max_timer_duration"
        const val KEY_ORIGINAL_BRIGHTNESS = "original_brightness"
        const val KEY_LOW_BATTERY_MAX_TIMER = "current_low_battery_max_timer"

        const val KEY_SCHEDULED_TIMERS = "scheduled_timers"
    }
}