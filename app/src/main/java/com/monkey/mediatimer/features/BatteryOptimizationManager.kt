package com.monkey.mediatimer.features

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import com.monkey.domain.repository.BatterySharedPreferenceRepository
import com.monkey.domain.repository.BatterySharedPreferenceRepository.Companion.KEY_ENABLE_OPTIMIZATIONS
import com.monkey.domain.repository.BatterySharedPreferenceRepository.Companion.KEY_LIMIT_TIMER_DURATION
import com.monkey.domain.repository.BatterySharedPreferenceRepository.Companion.KEY_LOW_BATTERY_BRIGHTNESS
import com.monkey.domain.repository.BatterySharedPreferenceRepository.Companion.KEY_LOW_BATTERY_MAX_TIMER
import com.monkey.domain.repository.BatterySharedPreferenceRepository.Companion.KEY_LOW_BATTERY_MAX_TIMER_DEFAULT
import com.monkey.domain.repository.BatterySharedPreferenceRepository.Companion.KEY_ORIGINAL_BRIGHTNESS
import com.monkey.domain.repository.BatterySharedPreferenceRepository.Companion.KEY_REDUCE_BRIGHTNESS
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryOptimizationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPrefs: BatterySharedPreferenceRepository
) {

    private val _batteryOptimizationSettings = MutableStateFlow(BatteryOptimizationSettings())
    val batteryOptimizationSettings: StateFlow<BatteryOptimizationSettings> =
        _batteryOptimizationSettings.asStateFlow()

    private val _batteryStatus = MutableStateFlow<BatteryStatus>(BatteryStatus.Unknown)
    val batteryStatus: StateFlow<BatteryStatus> = _batteryStatus.asStateFlow()

    init {
        loadSettings()
        updateBatteryStatus()
    }

    fun updateBatteryStatus() {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val isCharging = batteryManager.isCharging
        _batteryStatus.value = when {
            batteryLevel < 0 -> BatteryStatus.Unknown
            isCharging -> BatteryStatus.Charging(batteryLevel)
            batteryLevel <= LOW_BATTERY_THRESHOLD -> BatteryStatus.Low(batteryLevel)
            else -> BatteryStatus.Normal(batteryLevel)
        }
    }

    /**
     * Cập nhật cài đặt tối ưu hóa pin
     */
    fun updateSettings(settings: BatteryOptimizationSettings) {
        _batteryOptimizationSettings.value = settings
        saveSettings(settings)

        // Áp dụng các tối ưu mới nếu đã kích hoạt
        if (settings.enableBatteryOptimizations) {
            applyOptimizationsBasedOnStatus(_batteryStatus.value)
        }
    }

    private fun saveSettings(settings: BatteryOptimizationSettings) {
        CoroutineScope(Dispatchers.IO).launch {
            sharedPrefs.save(KEY_ENABLE_OPTIMIZATIONS, settings.enableBatteryOptimizations)
            sharedPrefs.save(KEY_REDUCE_BRIGHTNESS, settings.reduceBrightnessOnLowBattery)
            sharedPrefs.save(KEY_LOW_BATTERY_BRIGHTNESS, settings.lowBatteryBrightnessLevel)
            sharedPrefs.save(KEY_LIMIT_TIMER_DURATION, settings.limitTimerDurationOnLowBattery)
            sharedPrefs.save(KEY_LOW_BATTERY_MAX_TIMER_DEFAULT, settings.lowBatteryMaxTimerDuration)
        }
    }

    /**
     * Kiểm tra xem ứng dụng có được miễn trừ khỏi tối ưu hóa pin không
     */
    fun isIgnoringBatteryOptimizations(): Boolean {
        val packageName = context.packageName
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(packageName)
        } else {
            true // Phiên bản cũ không có tính năng này
        }
    }

    private fun applyOptimizationsBasedOnStatus(status: BatteryStatus) {
        val settings = _batteryOptimizationSettings.value
        if (!settings.enableBatteryOptimizations) return

        when (status) {
            is BatteryStatus.Low -> {
                if (settings.reduceBrightnessOnLowBattery) {
                    reduceBrightness(settings.lowBatteryBrightnessLevel)
                }
                if (settings.limitTimerDurationOnLowBattery) {
                    limitTimerDuration(settings.lowBatteryMaxTimerDuration)
                }
            }
            is BatteryStatus.Normal -> {
                // Pin bình thường, có thể áp dụng các tối ưu hóa nhẹ
                // Khôi phục độ sáng nếu trước đó đã giảm
                if (settings.reduceBrightnessOnLowBattery) {
                    restoreBrightness()
                }

                // Khôi phục giới hạn thời gian hẹn giờ
                if (settings.limitTimerDurationOnLowBattery) {
                    restoreTimerDuration()
                }
            }
            is BatteryStatus.Charging -> {
                // Đang sạc, không cần tối ưu
                restoreBrightness()
                restoreTimerDuration()
            }
            BatteryStatus.Unknown -> {
                // Không biết trạng thái, không làm gì
            }
        }
    }

    /**
     * Giảm độ sáng màn hình
     */
    private fun reduceBrightness(level: Float) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(context)) {
                    return // Không có quyền
                }

                // Lưu độ sáng hiện tại nếu chưa lưu
                val currentBrightness = Settings.System.getInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS
                )
                CoroutineScope(Dispatchers.IO).launch {
                    sharedPrefs.save(KEY_ORIGINAL_BRIGHTNESS, currentBrightness)
                }

                // Đặt độ sáng mới
                val newBrightnessValue = (level * 255).toInt()
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    newBrightnessValue
                )
            }
        } catch (e: Exception) {
            // Xử lý nếu không thể thay đổi độ sáng
        }
    }

    /**
     * Khôi phục độ sáng ban đầu
     */
    private fun restoreBrightness() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(context)) {
                    return // Không có quyền
                }

                // Khôi phục nếu đã lưu trước đó
                val originalBrightness = sharedPrefs.originalBrightness.value
                if (originalBrightness != -1) {
                    Settings.System.putInt(
                        context.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS,
                        originalBrightness
                    )

                    // Xóa giá trị đã lưu
                    CoroutineScope(Dispatchers.IO).launch {
                        sharedPrefs.save(KEY_ORIGINAL_BRIGHTNESS, -1)
                    }
                }
            }
        } catch (e: Exception) {
            // Xử lý ngoại lệ
        }
    }

    /**
     * Khôi phục giới hạn thời gian hẹn giờ
     */
    private fun restoreTimerDuration() {
        // Xóa giá trị đã lưu
        CoroutineScope(Dispatchers.IO).launch {

            sharedPrefs.save(KEY_LOW_BATTERY_MAX_TIMER, 30)
        }
    }


    /**
     * Giới hạn thời gian tối đa cho hẹn giờ khi pin yếu
     */
    private fun limitTimerDuration(maxMinutes: Int) {
        // Lưu giá trị thực trong trạng thái tạm thời
        CoroutineScope(Dispatchers.IO).launch {
            sharedPrefs.save(KEY_LOW_BATTERY_MAX_TIMER, maxMinutes)
        }
    }

    private fun loadSettings() {
        _batteryOptimizationSettings.value = BatteryOptimizationSettings(
            sharedPrefs.enableOptimizations.value,
            sharedPrefs.reduceBrightness.value,
            sharedPrefs.lowBatteryBrightness.value,
            sharedPrefs.limitTimerDuration.value,
            sharedPrefs.lowBatteryMaxTimerDefault.value
        )
    }


    /**
     * Lớp đại diện cho cài đặt tối ưu hóa pin
     */
    data class BatteryOptimizationSettings(
        val enableBatteryOptimizations: Boolean = true,
        val reduceBrightnessOnLowBattery: Boolean = true,
        val lowBatteryBrightnessLevel: Float = 0.3f, // 30% độ sáng
        val limitTimerDurationOnLowBattery: Boolean = true,
        val lowBatteryMaxTimerDuration: Int = 30 // 30 phút
    )

    /**
     * Lớp đại diện cho trạng thái pin
     */
    sealed class BatteryStatus {
        object Unknown : BatteryStatus()
        data class Normal(val level: Int) : BatteryStatus()
        data class Low(val level: Int) : BatteryStatus()
        data class Charging(val level: Int) : BatteryStatus()
    }

    companion object {
        /*        private const val PREFERENCES_NAME = "battery_optimization_preferences"
                private const val KEY_ENABLE_OPTIMIZATIONS = "enable_battery_optimizations"
                private const val KEY_REDUCE_BRIGHTNESS = "reduce_brightness_on_low_battery"
                private const val KEY_LOW_BATTERY_BRIGHTNESS = "low_battery_brightness_level"
                private const val KEY_LIMIT_TIMER_DURATION = "limit_timer_duration_on_low_battery"
                private const val KEY_LOW_BATTERY_MAX_TIMER_DEFAULT = "low_battery_max_timer_duration"
                private const val KEY_ORIGINAL_BRIGHTNESS = "original_brightness"
                private const val KEY_LOW_BATTERY_MAX_TIMER = "current_low_battery_max_timer"*/

        private const val LOW_BATTERY_THRESHOLD = 20 // Pin dưới 20% được coi là yếu
    }
}