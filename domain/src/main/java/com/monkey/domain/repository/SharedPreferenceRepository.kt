package com.monkey.domain.repository

import com.monkey.domain.model.AppSettings
import com.monkey.domain.model.TimerSettings
import com.monkey.domain.model.UISettings
import kotlinx.coroutines.flow.StateFlow

interface SharedPreferenceRepository {

    val maxTimerDuration: StateFlow<Long> // this is minutes
    val defaultTimerDuration: StateFlow<Long> // this is minutes
    val timerSettings: StateFlow<TimerSettings>
    val uiSettings: StateFlow<UISettings>
    val appSettings: StateFlow<AppSettings>
    val endTimeMilliseconds: StateFlow<Long> // this is milliseconds, this is current time
    val vibrateOnCompletion: StateFlow<Boolean>
    val startOnBoot: StateFlow<Boolean>
    val autoCheckForUpdates: StateFlow<Boolean>
    val darkMode: StateFlow<Boolean>
    val useSystemTheme: StateFlow<Boolean>
    val sleepModeEnabled: StateFlow<Boolean>
    val gradualVolumeReductionEnabled: StateFlow<Boolean>
    val screenDimmingEnabled: StateFlow<Boolean>



    /**
     * this is milliseconds
     */
    val stopTimer: StateFlow<Long>

    suspend fun save(key: String, value: Any)

    companion object {
        const val BASE_SHARE_PREFS = "media_timer_preferences"

        // Keys cho các cài đặt hẹn giờ
        const val KEY_TIMER_SETTINGS_SETUP = "app_settings_setup"
        const val KEY_MAX_TIMER_DURATION = "max_timer_duration"
        const val KEY_DEFAULT_TIMER_DURATION = "default_timer_duration"
        const val KEY_VIBRATE_ON_COMPLETION = "vibrate_on_completion"
        const val KEY_TIMER_COMPLETION_SOUND = "timer_completion_sound"
        const val KEY_AUTO_START_TIMER = "auto_start_timer"

        // Keys cho các cài đặt giao diện
        const val KEY_UI_SETTINGS_SETUP = "ui_settings_setup"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_USE_SYSTEM_THEME = "use_system_theme"
        const val KEY_ACCENT_COLOR = "accent_color"
        const val KEY_SHOW_NOTIFICATIONS = "show_notifications"

        // Keys cho các cài đặt ứng dụng
        const val KEY_APP_SETTINGS_SETUP = "timer_settings_setup"
        const val KEY_START_ON_BOOT = "start_on_boot"
        const val KEY_AUTO_UPDATE = "auto_check_for_updates"
        const val KEY_ANALYTICS_ENABLED = "analytics_enabled"
        const val KEY_USE_EXACT_ALARMS = "use_exact_alarms"

        // keys for sleep mode setup
        const val KEY_SLEEP_MODE_ENABLED = "sleep_mode_enabled"
        const val KEY_GRADUAL_VOLUME_REDUCTION_ENABLED = "gradual_volume_reduction_enabled"
        const val KEY_SCREEN_DIMMING_ENABLED = "screen_dimming_enabled"

        // Keys for common
        const val KEY_STOP_TIMER = "stop_timer"
        const val KEY_END_TIME_MILLISECONDS = "selected_time_minutes" // this is minutes


    }
}