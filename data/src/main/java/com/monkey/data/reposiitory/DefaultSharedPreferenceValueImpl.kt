package com.monkey.data.reposiitory

import com.monkey.data.local.DefaultValue
import com.monkey.domain.repository.DefaultSharedPreferenceValue

class DefaultSharedPreferenceValueImpl : DefaultSharedPreferenceValue {
    override val maxTimerDurationMinutes: Long = DefaultValue.DEFAULT_MAX_TIMER_DURATION
    override val defaultTimerDurationMinutes: Long = DefaultValue.DEFAULT_TIMER_DURATION
    override val vibrateOnCompletion: Boolean = DefaultValue.DEFAULT_VIBRATE_ON_COMPLETION
    override val timerCompletionSound: String = DefaultValue.DEFAULT_TIMER_COMPLETION_SOUND
    override val autoStartTimerOnDetection: Boolean =
        DefaultValue.DEFAULT_AUTO_START_TIMER_ON_DETECTION
/*    override val timerSettings: TimerSettings = TimerSettings(
        maxTimerDurationMinutes,
        defaultTimerDurationMinutes,
        vibrateOnCompletion,
        timerCompletionSound,
        autoStartTimerOnDetection
    )*/

    override val darkMode: Boolean = DefaultValue.DEFAULT_DARK_THEME
    override val useSystemTheme: Boolean = DefaultValue.DEFAULT_USE_SYSTEM_THEME
    override val accentColor: String = DefaultValue.DEFAULT_ACCENT_COLOR
    override val showNotifications: Boolean = DefaultValue.DEFAULT_SHOW_NOTIFICATIONS
/*    override val uiSettings: UISettings =
        UISettings(darkMode, useSystemTheme, accentColor, showNotifications)*/

    override val startOnBoot: Boolean = DefaultValue.DEFAULT_START_ON_BOOT
    override val autoCheckForUpdates: Boolean = DefaultValue.DEFAULT_AUTO_CHECK_FOR_UPDATES
    override val analyticsEnabled: Boolean = DefaultValue.DEFAULT_ANALYTICS_ENABLED
    override val useExactlyAlarms: Boolean = DefaultValue.DEFAULT_USE_EXACTLY_ALARMS
    /*override val appSettings: AppSettings =
        AppSettings(startOnBoot, autoCheckForUpdates, analyticsEnabled, useExactlyAlarms)*/
}