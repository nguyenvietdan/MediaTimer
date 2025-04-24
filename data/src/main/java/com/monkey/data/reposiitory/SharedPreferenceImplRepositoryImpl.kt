package com.monkey.data.reposiitory

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.monkey.data.local.DefaultValue
import com.monkey.domain.model.AppSettings
import com.monkey.domain.model.TimerSettings
import com.monkey.domain.model.UISettings
import com.monkey.domain.repository.DefaultSharedPreferenceValue
import com.monkey.domain.repository.SharedPreferenceRepository
import com.monkey.domain.repository.SharedPreferenceRepository.Companion.BASE_SHARE_PREFS
import com.monkey.domain.repository.SharedPreferenceRepository.Companion.KEY_ACCENT_COLOR
import com.monkey.domain.repository.SharedPreferenceRepository.Companion.KEY_ANALYTICS_ENABLED
import com.monkey.domain.repository.SharedPreferenceRepository.Companion.KEY_APP_SETTINGS_SETUP
import com.monkey.domain.repository.SharedPreferenceRepository.Companion.KEY_AUTO_START_TIMER
import com.monkey.domain.repository.SharedPreferenceRepository.Companion.KEY_AUTO_UPDATE
import com.monkey.domain.repository.SharedPreferenceRepository.Companion.KEY_DARK_MODE
import com.monkey.domain.repository.SharedPreferenceRepository.Companion.KEY_DEFAULT_TIMER_DURATION
import com.monkey.domain.repository.SharedPreferenceRepository.Companion.KEY_END_TIME_MILLISECONDS
import com.monkey.domain.repository.SharedPreferenceRepository.Companion.KEY_GRADUAL_VOLUME_REDUCTION_ENABLED
import com.monkey.domain.repository.SharedPreferenceRepository.Companion.KEY_MAX_TIMER_DURATION
import com.monkey.domain.repository.SharedPreferenceRepository.Companion.KEY_SCREEN_DIMMING_ENABLED
import com.monkey.domain.repository.SharedPreferenceRepository.Companion.KEY_SHOW_NOTIFICATIONS
import com.monkey.domain.repository.SharedPreferenceRepository.Companion.KEY_SLEEP_MODE_ENABLED
import com.monkey.domain.repository.SharedPreferenceRepository.Companion.KEY_START_ON_BOOT
import com.monkey.domain.repository.SharedPreferenceRepository.Companion.KEY_STOP_TIMER
import com.monkey.domain.repository.SharedPreferenceRepository.Companion.KEY_TIMER_COMPLETION_SOUND
import com.monkey.domain.repository.SharedPreferenceRepository.Companion.KEY_TIMER_SETTINGS_SETUP
import com.monkey.domain.repository.SharedPreferenceRepository.Companion.KEY_UI_SETTINGS_SETUP
import com.monkey.domain.repository.SharedPreferenceRepository.Companion.KEY_USE_EXACT_ALARMS
import com.monkey.domain.repository.SharedPreferenceRepository.Companion.KEY_USE_SYSTEM_THEME
import com.monkey.domain.repository.SharedPreferenceRepository.Companion.KEY_VIBRATE_ON_COMPLETION
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferenceImplRepositoryImpl @Inject constructor(
    override val context: Context,
    private val defaultValue: DefaultSharedPreferenceValue
) : BaseSharedPreferenceImpl(BASE_SHARE_PREFS, context), SharedPreferenceRepository {

    override val TAG = "SharedPreferenceRepositoryImpl"

    private val _maxTimerDuration =
        MAX_TIMER_DURATION.createFlow(defaultValue.maxTimerDurationMinutes)
    override val maxTimerDuration: StateFlow<Long> = _maxTimerDuration.asStateFlow()
    private val _defaultTimerDuration =
        DEFAULT_TIMER_DURATION.createFlow(defaultValue.defaultTimerDurationMinutes)
    override val defaultTimerDuration: StateFlow<Long> = _defaultTimerDuration.asStateFlow()
    private val _vibrateOnCompletion =
        VIBRATE_ON_COMPLETION.createFlow(defaultValue.vibrateOnCompletion)
    override val vibrateOnCompletion: StateFlow<Boolean> = _vibrateOnCompletion.asStateFlow()
    private val _timerCompletionSound =
        TIMER_COMPLETION_SOUND.createFlow(defaultValue.timerCompletionSound)
    private val _autoStartTimer =
        AUTO_START_TIMER.createFlow(defaultValue.autoStartTimerOnDetection)
    private val _timerSettings = MutableStateFlow<TimerSettings>(
        TimerSettings(
            _maxTimerDuration.value,
            _defaultTimerDuration.value,
            _vibrateOnCompletion.value,
            _timerCompletionSound.value,
            _autoStartTimer.value
        )
    )
    override val timerSettings: StateFlow<TimerSettings> = _timerSettings.asStateFlow()

    private val _darkMode = DARK_MODE.createFlow(defaultValue.darkMode)
    override val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()
    private val _useSystemTheme = USE_SYSTEM_THEME.createFlow(defaultValue.useSystemTheme)
    override val useSystemTheme: StateFlow<Boolean> = _useSystemTheme.asStateFlow()
    private val _accentColor = ACCENT_COLOR.createFlow(defaultValue.accentColor)
    private val _showNotifications = SHOW_NOTIFICATIONS.createFlow(defaultValue.showNotifications)
    private val _uiSettings = MutableStateFlow<UISettings>(
        UISettings(
            _darkMode.value,
            _useSystemTheme.value,
            _accentColor.value,
            _showNotifications.value
        )
    )
    override val uiSettings: StateFlow<UISettings> = _uiSettings.asStateFlow()


    private val _startOnBoot = START_ON_BOOT.createFlow(defaultValue.startOnBoot)
    override val startOnBoot: StateFlow<Boolean> = _startOnBoot.asStateFlow()
    private val _autoUpdate = AUTO_UPDATE.createFlow(defaultValue.autoCheckForUpdates)
    override val autoCheckForUpdates: StateFlow<Boolean> = _autoUpdate.asStateFlow()
    private val _analyticsEnabled = ANALYTICS_ENABLED.createFlow(defaultValue.analyticsEnabled)
    private val _useExactAlarms = USE_EXACT_ALARMS.createFlow(defaultValue.useExactlyAlarms)
    private val _appSettings = MutableStateFlow<AppSettings>(
        AppSettings(
            _startOnBoot.value,
            _autoUpdate.value,
            _analyticsEnabled.value,
            _useExactAlarms.value
        )
    )
    override val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    private val _stopTimer =
        STOP_TIMER.createFlow(TimeUnit.MINUTES.toMillis(defaultValue.maxTimerDurationMinutes))
    override val stopTimer: StateFlow<Long> = _stopTimer.asStateFlow()

    private val _endTimeMilliseconds =
        END_TIME_MILLISECONDS.createFlow(DefaultValue.DEFAULT_SELECTED_TIMER_MILLISECONDS)
    override val endTimeMilliseconds: StateFlow<Long> = _endTimeMilliseconds.asStateFlow()

    // sleep mode
    private val _sleepModeEnabled = SLEEP_MODE_ENABLED.createFlow(false)
    override val sleepModeEnabled: StateFlow<Boolean> = _sleepModeEnabled.asStateFlow()

    private val _gradualVolumeReductionEnabled = GRADUAL_VOLUME_REDUCTION.createFlow(false)
    override val gradualVolumeReductionEnabled: StateFlow<Boolean> =
        _gradualVolumeReductionEnabled.asStateFlow()

    private val _screenDimmingEnabled = SCREEN_DIMMING_ENABLED.createFlow(false)
    override val screenDimmingEnabled: StateFlow<Boolean> = _screenDimmingEnabled.asStateFlow()

    override suspend fun save(key: String, value: Any) {
        Log.i(TAG, "save: $key = $value")
        context.dataStore.edit { preferences ->
            when (key) {
                KEY_TIMER_SETTINGS_SETUP -> updateTimerSettings(value as TimerSettings, preferences)
                KEY_APP_SETTINGS_SETUP -> updateAppSettings(value as AppSettings, preferences)
                KEY_UI_SETTINGS_SETUP -> updateUISettings(value as UISettings, preferences)
                KEY_MAX_TIMER_DURATION -> {
                    preferences[MAX_TIMER_DURATION] = value as Long
                    _maxTimerDuration.value = value
                    // update default timer duration if it is greater than max timer duration
                    if (value < _defaultTimerDuration.value) {
                        preferences[DEFAULT_TIMER_DURATION] = value
                        _defaultTimerDuration.value = value
                    }
                }

                KEY_DEFAULT_TIMER_DURATION -> {
                    preferences[DEFAULT_TIMER_DURATION] = value as Long
                    _defaultTimerDuration.value = value
                }

                KEY_VIBRATE_ON_COMPLETION -> {
                    preferences[VIBRATE_ON_COMPLETION] = value as Boolean
                    _vibrateOnCompletion.value = value
                }

                KEY_START_ON_BOOT -> {
                    preferences[START_ON_BOOT] = value as Boolean
                    _startOnBoot.value = value
                }

                KEY_AUTO_UPDATE -> {
                    preferences[AUTO_UPDATE] = value as Boolean
                    _autoUpdate.value = value
                }

                KEY_DARK_MODE -> {
                    preferences[DARK_MODE] = value as Boolean
                    _darkMode.value = value
                }

                KEY_USE_SYSTEM_THEME -> {
                    preferences[USE_SYSTEM_THEME] = value as Boolean
                    _useSystemTheme.value = value
                }

                KEY_STOP_TIMER -> {
                    preferences[STOP_TIMER] = value as Long
                    _stopTimer.value = value
                }

                KEY_END_TIME_MILLISECONDS -> {
                    preferences[END_TIME_MILLISECONDS] = value as Long
                    _endTimeMilliseconds.value = value
                }

                KEY_SLEEP_MODE_ENABLED -> {
                    preferences[SLEEP_MODE_ENABLED] = value as Boolean
                    _sleepModeEnabled.value = value
                }

                KEY_GRADUAL_VOLUME_REDUCTION_ENABLED -> {
                    preferences[GRADUAL_VOLUME_REDUCTION] = value as Boolean
                    _gradualVolumeReductionEnabled.value = value
                }

                KEY_SCREEN_DIMMING_ENABLED -> {
                    preferences[SCREEN_DIMMING_ENABLED] = value as Boolean
                    _screenDimmingEnabled.value = value
                }

                else -> Log.e(TAG, "unknown key $key")
            }
        }
    }

    private fun updateTimerSettings(
        settings: TimerSettings,
        preferences: MutablePreferences
    ) {
        preferences[MAX_TIMER_DURATION] = settings.maxTimerDurationMinutes
        preferences[DEFAULT_TIMER_DURATION] = settings.defaultTimerDurationMinutes
        preferences[VIBRATE_ON_COMPLETION] = settings.vibrateOnCompletion
        preferences[TIMER_COMPLETION_SOUND] = settings.timerCompletionSound
        preferences[AUTO_START_TIMER] = settings.autoStartTimerOnDetection
        _timerSettings.value = settings
    }

    private fun updateAppSettings(
        appSettings: AppSettings,
        preferences: MutablePreferences
    ) {
        preferences[START_ON_BOOT] = appSettings.startOnBoot
        preferences[AUTO_UPDATE] = appSettings.autoCheckForUpdates
        preferences[ANALYTICS_ENABLED] = appSettings.analyticsEnabled
        preferences[USE_EXACT_ALARMS] = appSettings.useExactAlarms
        _appSettings.value = appSettings
    }

    private fun updateUISettings(
        uiSettings: UISettings,
        preferences: MutablePreferences
    ) {
        preferences[DARK_MODE] = uiSettings.darkMode
        preferences[USE_SYSTEM_THEME] = uiSettings.useSystemTheme
        preferences[ACCENT_COLOR] = uiSettings.accentColor
        preferences[SHOW_NOTIFICATIONS] = uiSettings.showNotifications
        _uiSettings.value = uiSettings
    }

    companion object {
        // for create data store file name
        private val MAX_TIMER_DURATION = longPreferencesKey(KEY_MAX_TIMER_DURATION)
        private val DEFAULT_TIMER_DURATION = longPreferencesKey(KEY_DEFAULT_TIMER_DURATION)
        private val VIBRATE_ON_COMPLETION = booleanPreferencesKey(KEY_VIBRATE_ON_COMPLETION)
        private val TIMER_COMPLETION_SOUND = stringPreferencesKey(KEY_TIMER_COMPLETION_SOUND)
        private val AUTO_START_TIMER = booleanPreferencesKey(KEY_AUTO_START_TIMER)

        private val DARK_MODE = booleanPreferencesKey(KEY_DARK_MODE)
        private val USE_SYSTEM_THEME = booleanPreferencesKey(KEY_USE_SYSTEM_THEME)
        private val ACCENT_COLOR = stringPreferencesKey(KEY_ACCENT_COLOR)
        private val SHOW_NOTIFICATIONS = booleanPreferencesKey(KEY_SHOW_NOTIFICATIONS)

        private val START_ON_BOOT = booleanPreferencesKey(KEY_START_ON_BOOT)
        private val AUTO_UPDATE = booleanPreferencesKey(KEY_AUTO_UPDATE)
        private val ANALYTICS_ENABLED = booleanPreferencesKey(KEY_ANALYTICS_ENABLED)
        private val USE_EXACT_ALARMS = booleanPreferencesKey(KEY_USE_EXACT_ALARMS)

        private val STOP_TIMER = longPreferencesKey(KEY_STOP_TIMER)
        private val END_TIME_MILLISECONDS = longPreferencesKey(KEY_END_TIME_MILLISECONDS)

        // Sleep mode
        private val SLEEP_MODE_ENABLED = booleanPreferencesKey(KEY_SLEEP_MODE_ENABLED)
        private val GRADUAL_VOLUME_REDUCTION = booleanPreferencesKey(
            KEY_GRADUAL_VOLUME_REDUCTION_ENABLED
        )
        private val SCREEN_DIMMING_ENABLED = booleanPreferencesKey(KEY_SCREEN_DIMMING_ENABLED)
    }
}