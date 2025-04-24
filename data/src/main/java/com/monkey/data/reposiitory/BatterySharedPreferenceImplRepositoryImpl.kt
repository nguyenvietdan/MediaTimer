package com.monkey.data.reposiitory

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.monkey.domain.repository.BatterySharedPreferenceRepository
import com.monkey.domain.repository.BatterySharedPreferenceRepository.Companion.BATTERY_PREFERENCES_NAME
import com.monkey.domain.repository.BatterySharedPreferenceRepository.Companion.KEY_ENABLE_OPTIMIZATIONS
import com.monkey.domain.repository.BatterySharedPreferenceRepository.Companion.KEY_LIMIT_TIMER_DURATION
import com.monkey.domain.repository.BatterySharedPreferenceRepository.Companion.KEY_LOW_BATTERY_BRIGHTNESS
import com.monkey.domain.repository.BatterySharedPreferenceRepository.Companion.KEY_LOW_BATTERY_MAX_TIMER
import com.monkey.domain.repository.BatterySharedPreferenceRepository.Companion.KEY_LOW_BATTERY_MAX_TIMER_DEFAULT
import com.monkey.domain.repository.BatterySharedPreferenceRepository.Companion.KEY_ORIGINAL_BRIGHTNESS
import com.monkey.domain.repository.BatterySharedPreferenceRepository.Companion.KEY_REDUCE_BRIGHTNESS
import com.monkey.domain.repository.BatterySharedPreferenceRepository.Companion.KEY_SCHEDULED_TIMERS
import com.monkey.domain.repository.DefaultSharedPreferenceValue
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class BatterySharedPreferenceImplRepositoryImpl @Inject constructor(
    override val context: Context,
    defaultValue: DefaultSharedPreferenceValue
) : BaseSharedPreferenceImpl(BATTERY_PREFERENCES_NAME, context), BatterySharedPreferenceRepository {

    override val TAG: String = "BatterySharedPreferenceImplRepositoryImpl"

    // battery
    private val _enableOptimizations = ENABLE_OPTIMIZATIONS.createFlow(true)
    override val enableOptimizations: StateFlow<Boolean> = _enableOptimizations.asStateFlow()
    private val _reduceBrightness = REDUCE_BRIGHTNESS.createFlow(true)
    override val reduceBrightness: StateFlow<Boolean> = _reduceBrightness.asStateFlow()
    private val _lowBatteryBrightness = LOW_BATTERY_BRIGHTNESS.createFlow(0.3f)
    override val lowBatteryBrightness: StateFlow<Float> = _lowBatteryBrightness.asStateFlow()
    private val _limitTimerDuration = LIMIT_TIMER_DURATION.createFlow(true)
    override val limitTimerDuration: StateFlow<Boolean> = _limitTimerDuration.asStateFlow()
    private val _lowBatteryMaxTimerDefault = LOW_BATTERY_MAX_TIMER_DEFAULT.createFlow(30)
    override val lowBatteryMaxTimerDefault: StateFlow<Int> =
        _lowBatteryMaxTimerDefault.asStateFlow()
    private val _originalBrightness = ORIGINAL_BRIGHTNESS.createFlow(-1)
    override val originalBrightness: StateFlow<Int> = _originalBrightness.asStateFlow()
    private val _lowBatteryMaxTimer = LOW_BATTERY_MAX_TIMER.createFlow(30)
    override val lowBatteryMaxTimer: StateFlow<Int> = _lowBatteryMaxTimer.asStateFlow()

    private val _scheduledTimer = SCHEDULED_TIMERS.createFlow("[]")
    override val scheduledTimer: StateFlow<String> = _scheduledTimer.asStateFlow()

    override suspend fun save(key: String, value: Any) {
        Log.i(TAG, "save: $key = $value")
        context.dataStore.edit { preferences ->
            when (key) {
                // battery
                KEY_ENABLE_OPTIMIZATIONS -> {
                    preferences[ENABLE_OPTIMIZATIONS] = value as Boolean
                    _enableOptimizations.value = value as Boolean
                }

                KEY_REDUCE_BRIGHTNESS -> {
                    preferences[REDUCE_BRIGHTNESS] = value as Boolean
                    _reduceBrightness.value = value as Boolean
                }

                KEY_LOW_BATTERY_BRIGHTNESS -> {
                    preferences[LOW_BATTERY_BRIGHTNESS] = value as Float
                    _lowBatteryBrightness.value = value as Float
                }

                KEY_LIMIT_TIMER_DURATION -> {
                    preferences[LIMIT_TIMER_DURATION] = value as Boolean
                    _limitTimerDuration.value = value as Boolean
                }

                KEY_LOW_BATTERY_MAX_TIMER_DEFAULT -> {
                    preferences[LOW_BATTERY_MAX_TIMER_DEFAULT] = value as Int
                    _lowBatteryMaxTimerDefault.value = value as Int
                }

                KEY_ORIGINAL_BRIGHTNESS -> {
                    preferences[ORIGINAL_BRIGHTNESS] = value as Int
                    _originalBrightness.value = value as Int
                }

                KEY_LOW_BATTERY_MAX_TIMER -> {
                    preferences[LOW_BATTERY_MAX_TIMER] = value as Int
                    _lowBatteryMaxTimer.value = value as Int
                }

                KEY_SCHEDULED_TIMERS -> {
                    preferences[SCHEDULED_TIMERS] = value as String
                    _scheduledTimer.value = value
                }

                else -> Log.e(TAG, "unknown key $key")
            }
        }
    }

    companion object {
        // Battery
        private val ENABLE_OPTIMIZATIONS = booleanPreferencesKey(KEY_ENABLE_OPTIMIZATIONS)
        private val REDUCE_BRIGHTNESS = booleanPreferencesKey(KEY_REDUCE_BRIGHTNESS)
        private val LOW_BATTERY_BRIGHTNESS = floatPreferencesKey(KEY_LOW_BATTERY_BRIGHTNESS)
        private val LIMIT_TIMER_DURATION = booleanPreferencesKey(KEY_LIMIT_TIMER_DURATION)
        private val LOW_BATTERY_MAX_TIMER_DEFAULT =
            intPreferencesKey(KEY_LOW_BATTERY_MAX_TIMER_DEFAULT)
        private val ORIGINAL_BRIGHTNESS = intPreferencesKey(KEY_ORIGINAL_BRIGHTNESS)
        private val LOW_BATTERY_MAX_TIMER = intPreferencesKey(KEY_LOW_BATTERY_MAX_TIMER)

        private val SCHEDULED_TIMERS = stringPreferencesKey(KEY_SCHEDULED_TIMERS)
    }
}