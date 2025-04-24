package com.monkey.mediatimer.presentations.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monkey.domain.repository.SharedPreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val sharedPrefs: SharedPreferenceRepository
) : ViewModel() {

    private val _editingType = MutableStateFlow<DurationType?>(null)
    val editingType: StateFlow<DurationType?> = _editingType.asStateFlow()

    //val maxDuration = MutableStateFlow(120)
    //val defaultDuration = MutableStateFlow(30)
    //var vibrateOnCompletion by mutableStateOf(true)

    fun openDurationSheet(type: DurationType) {
        _editingType.value = type
    }

    fun closeSheet() {
        _editingType.value = null
    }

    fun onDurationSelected(value: Int) {
        /*when (_editingType.value) {
            DurationType.MAX -> maxDuration.value = value
            DurationType.DEFAULT -> defaultDuration.value = value
            null -> {}
        }*/
        closeSheet()
    }

    fun updateMaxTimer(timer: Long) {
        saveSharedValue(SharedPreferenceRepository.KEY_MAX_TIMER_DURATION, timer)
    }

    fun updateDefaultTimeDuration(timer: Long) {
        saveSharedValue(SharedPreferenceRepository.KEY_DEFAULT_TIMER_DURATION, timer)
    }

    fun updateVibrateOnCompletion(isVibrate: Boolean) {
        saveSharedValue(SharedPreferenceRepository.KEY_VIBRATE_ON_COMPLETION, isVibrate)
    }

    fun updateStartOnBoot(startOnBoot: Boolean) {
        saveSharedValue(SharedPreferenceRepository.KEY_START_ON_BOOT, startOnBoot)

    }

    fun updateAutoUpdate(autoUpdate: Boolean) {
        saveSharedValue(SharedPreferenceRepository.KEY_AUTO_UPDATE, autoUpdate)
    }

    fun updateDarkMode(darkMode: Boolean) {
        saveSharedValue(SharedPreferenceRepository.KEY_DARK_MODE, darkMode)
    }

    fun updateUseSystemTheme(useSystemTheme: Boolean) {
        saveSharedValue(SharedPreferenceRepository.KEY_USE_SYSTEM_THEME, useSystemTheme)

    }

    fun updateSleepMode(sleepMode: Boolean) {
        saveSharedValue(SharedPreferenceRepository.KEY_SLEEP_MODE_ENABLED, sleepMode)
    }

    fun updateGradualVolumeEnabled(gradualVolumeEnabled: Boolean) {
        saveSharedValue(
            SharedPreferenceRepository.KEY_GRADUAL_VOLUME_REDUCTION_ENABLED,
            gradualVolumeEnabled
        )
    }

    fun updateScreenDimmingEnabled(enabled: Boolean) {
        saveSharedValue(SharedPreferenceRepository.KEY_SCREEN_DIMMING_ENABLED, enabled)
    }

    private fun saveSharedValue(key: String, value: Any) = viewModelScope.launch {
        viewModelScope.launch {
            sharedPrefs.save(key, value)
        }
    }
}

enum class DurationType { MAX, DEFAULT }