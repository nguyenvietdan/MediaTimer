package com.monkey.domain.repository

import com.monkey.domain.model.AppSettings
import com.monkey.domain.model.TimerSettings
import com.monkey.domain.model.UISettings

interface DefaultSharedPreferenceValue {

    // for timer settings
    //val timerSettings: TimerSettings
    val maxTimerDurationMinutes: Long
    val defaultTimerDurationMinutes: Long
    val vibrateOnCompletion: Boolean
    val timerCompletionSound: String
    val autoStartTimerOnDetection: Boolean

    // for ui settings
    //val appSettings: AppSettings
    val darkMode: Boolean
    val useSystemTheme: Boolean
    val accentColor: String
    val showNotifications: Boolean

    // for app settings
    //val uiSettings: UISettings
    val startOnBoot: Boolean
    val autoCheckForUpdates: Boolean
    val analyticsEnabled: Boolean
    val useExactlyAlarms: Boolean

}