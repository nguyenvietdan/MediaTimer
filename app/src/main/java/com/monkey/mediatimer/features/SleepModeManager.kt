package com.monkey.mediatimer.features

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import com.monkey.domain.repository.SharedPreferenceRepository
import com.monkey.mediatimer.domain.MediaControllerMgr
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepModeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaControllerMgr: MediaControllerMgr,
    private val sharedPrefs: SharedPreferenceRepository
) {
    private val TAG = "SleepModeManager"
    private val handler = Handler(Looper.getMainLooper())

    // Lưu trạng thái chế độ ngủ hiện tại
    private val _sleepModeState = MutableStateFlow<SleepModeState>(SleepModeState.Inactive)
    val sleepModeState: StateFlow<SleepModeState> = _sleepModeState.asStateFlow()

    // Lưu cài đặt chế độ ngủ
    private val _sleepModeSettings = MutableStateFlow(SleepModeSettings())
    val sleepModeSettings: StateFlow<SleepModeSettings> = _sleepModeSettings.asStateFlow()

    // Giá trị lưu trữ
    private var originalVolume = 0
    private var originalBrightness = 0f
    private var fadeVolumeRunnable: Runnable? = null
    private var dimScreenRunnable: Runnable? = null

    init {
        // Khởi tạo giá trị mặc định từ SettingsRepository
        CoroutineScope(Dispatchers.IO).launch {
            sharedPrefs.gradualVolumeReductionEnabled.collect { enabled ->
                _sleepModeSettings.value.enableGradualVolumeReduction = enabled
            }
        }

        loadSettings()
    }

    /**
     * Kích hoạt chế độ ngủ với các tùy chọn đã cấu hình
     */
    fun activateSleepMode(durationMinutes: Int) {
        // Lưu các giá trị hiện tại để khôi phục sau này
        Log.i(TAG, "activateSleepMode: $durationMinutes")
        saveCurrentState()

        val settings = _sleepModeSettings.value
        val durationMillis = TimeUnit.MINUTES.toMillis(durationMinutes.toLong())
        val endTimeMillis = System.currentTimeMillis() + durationMillis

        _sleepModeState.value = SleepModeState.Active(
            startTime = System.currentTimeMillis(),
            endTime = endTimeMillis,
            settings = settings
        )

        // Áp dụng các tính năng của chế độ ngủ
        if (settings.enableGradualVolumeReduction) {
            startGradualVolumeReduction(durationMinutes)
        }

        if (settings.enableScreenDimming) {
            startScreenDimming(durationMinutes)
        }

        // Đặt hẹn giờ để tắt media
        handler.postDelayed({
            stopSleepMode()
        }, durationMillis)
    }

    /**
     * Dừng chế độ ngủ và khôi phục các cài đặt ban đầu
     */
    fun stopSleepMode() {
        // Dừng tất cả các quá trình đang chạy
        Log.i(TAG, "stopSleepMode: ")
        fadeVolumeRunnable?.let { handler.removeCallbacks(it) }
        dimScreenRunnable?.let { handler.removeCallbacks(it) }

        // Khôi phục các cài đặt ban đầu
        restoreOriginalState()

        // Dừng tất cả media nếu cần
        if (_sleepModeSettings.value.stopMediaAtEnd) {
            mediaControllerMgr.stopAllMedias()
        }

        _sleepModeState.value = SleepModeState.Inactive
    }

    /**
     * Cập nhật cài đặt chế độ ngủ
     */
    fun updateSleepModeSettings(settings: SleepModeSettings) {
        Log.i(TAG, "updateSleepModeSettings: ")
        _sleepModeSettings.value = settings
        // Lưu các cài đặt này vào bộ nhớ cố định
        saveSleepModeSettings(settings)
    }

    /**
     * Lưu trạng thái hiện tại để khôi phục sau này
     */
    private fun saveCurrentState() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        try {
            // Lấy độ sáng hiện tại của màn hình
            originalBrightness = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            ).toFloat() / 255
        } catch (e: Exception) {
            // Xử lý nếu không thể truy cập cài đặt hệ thống
            originalBrightness = 0.5f
        }
    }

    /**
     * Khôi phục trạng thái ban đầu
     */
    private fun restoreOriginalState() {
        // Khôi phục âm lượng
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0)

        // Khôi phục độ sáng màn hình nếu có quyền
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(context)) {
                    Settings.System.putInt(
                        context.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS,
                        (originalBrightness * 255).toInt()
                    )
                }
            }
        } catch (e: Exception) {
            // Xử lý nếu không thể ghi cài đặt hệ thống
        }
    }

    /**
     * Bắt đầu quá trình giảm dần âm lượng
     */
    private fun startGradualVolumeReduction(durationMinutes: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        var startVolume = originalVolume
        val totalTimeMs = TimeUnit.MINUTES.toMillis(durationMinutes.toLong()).coerceAtLeast(1L)
        val updateIntervalMs = TimeUnit.SECONDS.toMillis(30) // 30 giây cập nhật một lần
        var newVolume = originalVolume

        fadeVolumeRunnable = object : Runnable {
            var elapsedTimeMs = 0L

            override fun run() {
                elapsedTimeMs += updateIntervalMs

                if (elapsedTimeMs < totalTimeMs) {
                    // Tính toán âm lượng mới
                    val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    if (newVolume < currentVolume) {
                        elapsedTimeMs = updateIntervalMs
                        startVolume = currentVolume
                    }
                    val volumeReductionRatio = elapsedTimeMs.toFloat() / totalTimeMs

                    newVolume = (startVolume * (1 - volumeReductionRatio)).toInt()
                    // Đặt âm lượng mới
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)

                    // Lên lịch cập nhật tiếp theo
                    handler.postDelayed(this, updateIntervalMs)
                }
            }
        }

        // Bắt đầu quá trình
        handler.postDelayed(fadeVolumeRunnable!!, updateIntervalMs)
    }

    /**
     * Bắt đầu quá trình làm tối dần màn hình
     */
    private fun startScreenDimming(durationMinutes: Int) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(context)) {
                    Log.i(TAG, "startScreenDimming: there is no permission for write system settings.")
                    return // Không có quyền thay đổi cài đặt hệ thống
                }
            }

            val totalTimeMs = TimeUnit.MINUTES.toMillis(durationMinutes.toLong())
            val updateIntervalMs = TimeUnit.SECONDS.toMillis(30) // 30 giây cập nhật một lần

            dimScreenRunnable = object : Runnable {
                var elapsedTimeMs = 0L

                override fun run() {
                    elapsedTimeMs += updateIntervalMs

                    if (elapsedTimeMs < totalTimeMs) {
                        // Tính toán độ sáng mới
                        val brightnessReductionRatio = elapsedTimeMs.toFloat() / totalTimeMs
                        // Giảm xuống tối thiểu 10% độ sáng ban đầu
                        val targetBrightness = originalBrightness * 0.1f
                        val newBrightness = originalBrightness -
                                (originalBrightness - targetBrightness) * brightnessReductionRatio

                        // Đặt độ sáng mới
                        Settings.System.putInt(
                            context.contentResolver,
                            Settings.System.SCREEN_BRIGHTNESS,
                            (newBrightness * 255).toInt()
                        )

                        // Lên lịch cập nhật tiếp theo
                        handler.postDelayed(this, updateIntervalMs)
                    }
                }
            }

            // Bắt đầu quá trình
            handler.postDelayed(dimScreenRunnable!!, updateIntervalMs)
        } catch (e: Exception) {
            // Xử lý nếu không thể thay đổi độ sáng
        }
    }

    /**
     * Tải cài đặt từ bộ nhớ cố định
     */
    private fun loadSettings() {
        // Trong ứng dụng thực, điều này sẽ lấy từ SharedPreferences
        // Hiện tại chúng ta sử dụng các giá trị mặc định
        _sleepModeSettings.value = SleepModeSettings(
            enableGradualVolumeReduction = sharedPrefs.gradualVolumeReductionEnabled.value,
            enableScreenDimming = sharedPrefs.screenDimmingEnabled.value,
            stopMediaAtEnd = true,
            fadeOutDurationSeconds = 30
        )
    }

    /**
     * Lưu cài đặt vào bộ nhớ cố định
     */
    private fun saveSleepModeSettings(settings: SleepModeSettings) {
        // Trong ứng dụng thực, điều này sẽ lưu vào SharedPreferences
    }

    /**
     * Lớp đại diện cho trạng thái chế độ ngủ
     */
    sealed class SleepModeState {
        object Inactive : SleepModeState()
        data class Active(
            val startTime: Long,
            val endTime: Long,
            val settings: SleepModeSettings
        ) : SleepModeState()
    }

    /**
     * Lớp chứa các cài đặt cho chế độ ngủ
     */
    data class SleepModeSettings(
        var enableGradualVolumeReduction: Boolean = false,
        val enableScreenDimming: Boolean = true,
        val stopMediaAtEnd: Boolean = true,
        val fadeOutDurationSeconds: Int = 30
    )
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SleepModeManagerEntryPoint {
    fun sleepModeManager(): SleepModeManager
}