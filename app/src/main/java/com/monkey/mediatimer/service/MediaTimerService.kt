package com.monkey.mediatimer.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.monkey.domain.repository.SharedPreferenceRepository
import com.monkey.mediatimer.common.TimerState
import com.monkey.mediatimer.di.SharedPreferenceRepositoryEntryPoint
import com.monkey.mediatimer.domain.MediaControllerManagerEntryPoint
import com.monkey.mediatimer.domain.MediaControllerMgr
import com.monkey.mediatimer.features.SleepModeManager
import com.monkey.mediatimer.features.SleepModeManagerEntryPoint
import com.monkey.mediatimer.presentations.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MediaTimerService : Service() {
    private val TAG = "MediaTimerService"

    private var timer: CountDownTimer? = null
    private lateinit var mediaController: MediaControllerMgr
    private lateinit var sleepModeManager: SleepModeManager
    private lateinit var sharedPrefs: SharedPreferenceRepository

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Inactive)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate: ")
        MediaTimerService.timerState = _timerState.asStateFlow()

        val mediaControllerManagerEntryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            MediaControllerManagerEntryPoint::class.java
        )
        mediaController = mediaControllerManagerEntryPoint.mediaControllerManager()

        val sleepModeManagerEntryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            SleepModeManagerEntryPoint::class.java
        )
        sleepModeManager = sleepModeManagerEntryPoint.sleepModeManager()
        val sharedPreferenceRepository = EntryPointAccessors.fromApplication(
            applicationContext,
            SharedPreferenceRepositoryEntryPoint::class.java
        )
        sharedPrefs = sharedPreferenceRepository.sharedPreferenceRepository()

        val chanel = NotificationChannel(
            CHANNEL_ID,
            "Media Timer Service",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notification displayed when timer is running"
            setShowBadge(false)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(chanel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand: ${intent?.action}")
        when (intent?.action) {
            ACTION_START_TIMER -> {
                val durationMinutes = intent.getIntExtra(
                    EXTRA_DURATION_MINUTES,
                    DEFAULT_TIMER_DURATION
                )
                val useSleepMode = intent.getBooleanExtra(EXTRA_USE_SLEEP_MODE, false)
                startTimer(durationMinutes, useSleepMode)
            }

            ACTION_PAUSE_TIMER -> pauseTimer()
            ACTION_RESUME_TIMER -> resumeTimer()
            ACTION_STOP_TIMER -> stopTimer()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        timer?.cancel()
        MediaTimerService.timerState = MutableStateFlow<TimerState>(TimerState.Inactive)
        super.onDestroy()
    }

    @SuppressLint("ForegroundServiceType")
    private fun startTimer(durationMinutes: Int, useSleepMode: Boolean) {
        Log.i(TAG, "startTimer: $durationMinutes $useSleepMode")
        timer?.cancel()
        val durationMillis = TimeUnit.MINUTES.toMillis(durationMinutes.toLong())
        val startTimeMillis = System.currentTimeMillis()
        val endTimeMillis = startTimeMillis + durationMillis

        _timerState.value = TimerState.Running(
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
            remainingMillis = durationMillis,
            totalDurationMillis = durationMillis,
            useSleepMode = useSleepMode
        )
        if (useSleepMode) {
            sleepModeManager.activateSleepMode(durationMinutes)
        }
        startForeground(NOTIFICATION_ID, createTimerNotification(remainingMillis = durationMillis))

        executeCountDownTimer(durationMillis)
    }

    /**
     * Tạm dừng hẹn giờ đang chạy
     */
    private fun pauseTimer() {
        Log.i(TAG, "pauseTimer: ")
        timer?.cancel()

        val currentState = _timerState.value
        if (currentState is TimerState.Running) {
            _timerState.value = TimerState.Paused(
                startTimeMillis = currentState.startTimeMillis,
                pausedAtMillis = System.currentTimeMillis(),
                remainingMillis = currentState.remainingMillis,
                totalDurationMillis = currentState.totalDurationMillis,
                useSleepMode = currentState.useSleepMode
            )

            // Cập nhật thông báo
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(
                NOTIFICATION_ID,
                createTimerNotification(currentState.remainingMillis, isPaused = true)
            )
        }
    }

    /**
     * Tiếp tục hẹn giờ đã tạm dừng
     */
    private fun resumeTimer() {
        Log.i(TAG, "resumeTimer: ")
        val currentState = _timerState.value
        if (currentState is TimerState.Paused) {
            val newEndTimeMillis = System.currentTimeMillis() + currentState.remainingMillis

            _timerState.value = TimerState.Running(
                startTimeMillis = currentState.startTimeMillis,
                endTimeMillis = newEndTimeMillis,
                remainingMillis = currentState.remainingMillis,
                totalDurationMillis = currentState.totalDurationMillis,
                useSleepMode = currentState.useSleepMode
            )

            // Khởi tạo bộ đếm thời gian mới
            executeCountDownTimer(currentState.remainingMillis)

            // Cập nhật thông báo
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(
                NOTIFICATION_ID,
                createTimerNotification(currentState.remainingMillis)
            )
        }
    }

    private fun executeCountDownTimer(durationMillis: Long) {
        // Khởi tạo bộ đếm thời gian mới
        timer?.cancel()
        timer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val runningState = _timerState.value
                if (runningState is TimerState.Running) {
                    _timerState.value = runningState.copy(remainingMillis = millisUntilFinished)

                    // Cập nhật thông báo mỗi phút
                    if (millisUntilFinished % 60000 < 1000) {
                        val notificationManager =
                            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(
                            NOTIFICATION_ID,
                            createTimerNotification(millisUntilFinished)
                        )
                    }
                }
            }

            @RequiresPermission(Manifest.permission.VIBRATE)
            override fun onFinish() {
                // Dừng tất cả media
                mediaController.stopAllMedias()

                // Kiểm tra xem có nên rung khi kết thúc không
                val timerSettings = sharedPrefs.timerSettings.value
                if (timerSettings.vibrateOnCompletion) {
                    vibrate()
                }

                // Cập nhật trạng thái
                _timerState.value = TimerState.Inactive

                // Dừng service
                stopSelf()
            }
        }

        timer?.start()
    }

    /**
     * Dừng hẹn giờ thủ công
     */
    private fun stopTimer() {
        Log.i(TAG, "stopTimer: ")
        timer?.cancel()

        if (_timerState.value !is TimerState.Inactive) {
            // Nếu đang sử dụng sleep mode, dừng nó
            if (_timerState.value is TimerState.Running && (_timerState.value as TimerState.Running).useSleepMode ||
                _timerState.value is TimerState.Paused && (_timerState.value as TimerState.Paused).useSleepMode
            ) {
                sleepModeManager.stopSleepMode()
            }

            _timerState.value = TimerState.Inactive
        }

        stopForeground(true)
        stopSelf()
    }

    /**
     * Rung thiết bị khi hẹn giờ kết thúc
     */
    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun vibrate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        500,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        500,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            }
        } catch (e: Exception) {
            // Xử lý ngoại lệ nếu có
        }
    }

    /**
     * Tạo thông báo hiển thị khi hẹn giờ đang chạy
     */
    private fun createTimerNotification(
        remainingMillis: Long,
        isPaused: Boolean = false
    ): Notification {
        val minutes = remainingMillis / 60000
        val seconds = (remainingMillis % 60000) / 1000

        val timeText = if (isPaused) {
            "Pause: $minutes minutes $seconds remain"
        } else {
            "$minutes minutes $seconds remain"
        }

        // Intent để mở ứng dụng khi nhấn vào thông báo
        val contentIntent = Intent(this, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent để dừng hẹn giờ
        val stopIntent = Intent(this, MediaTimerService::class.java).apply {
            action = ACTION_STOP_TIMER
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent để tạm dừng hoặc tiếp tục hẹn giờ
        val pauseResumeIntent = Intent(this, MediaTimerService::class.java).apply {
            action = if (isPaused) ACTION_RESUME_TIMER else ACTION_PAUSE_TIMER
        }
        val pauseResumePendingIntent = PendingIntent.getService(
            this,
            2,
            pauseResumeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Xây dựng thông báo
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_pause)
            .setContentTitle("Media Timer")
            .setContentText(timeText)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentPendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                if (isPaused) "Play" else "Pause",
                pauseResumePendingIntent
            )
            .addAction(
                android.R.drawable.ic_media_pause,
                "Stop",
                stopPendingIntent
            )

        return builder.build()
    }


    companion object {
        private const val CHANNEL_ID = "MediaTimerServiceChannel"
        private const val NOTIFICATION_ID = 1

        private const val ACTION_START_TIMER = "com.mediatimerapp.action.START_TIMER"
        private const val ACTION_PAUSE_TIMER = "com.mediatimerapp.action.PAUSE_TIMER"
        private const val ACTION_RESUME_TIMER = "com.mediatimerapp.action.RESUME_TIMER"
        private const val ACTION_STOP_TIMER = "com.mediatimerapp.action.STOP_TIMER"

        private const val EXTRA_DURATION_MINUTES = "duration_minutes"
        private const val EXTRA_USE_SLEEP_MODE = "use_sleep_mode"

        private const val DEFAULT_TIMER_DURATION = 30 // 30 phút

        /**
         * Helper để bắt đầu hẹn giờ từ bên ngoài service
         */
        fun startTimer(context: Context, durationMinutes: Int, useSleepMode: Boolean = false) {
            val intent = Intent(context, MediaTimerService::class.java).apply {
                action = ACTION_START_TIMER
                putExtra(EXTRA_DURATION_MINUTES, durationMinutes)
                putExtra(EXTRA_USE_SLEEP_MODE, useSleepMode)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        // todo saving temporary state of the timer in a global variable to be used by other components
        var timerState: StateFlow<TimerState> = MutableStateFlow<TimerState>(TimerState.Inactive)

        /**
         * Helper để tạm dừng hẹn giờ từ bên ngoài service
         */
        fun pauseTimer(context: Context) {
            val intent = Intent(context, MediaTimerService::class.java).apply {
                action = ACTION_PAUSE_TIMER
            }
            context.startService(intent)
        }

        /**
         * Helper để tiếp tục hẹn giờ từ bên ngoài service
         */
        fun resumeTimer(context: Context) {
            val intent = Intent(context, MediaTimerService::class.java).apply {
                action = ACTION_RESUME_TIMER
            }
            context.startService(intent)
        }

        /**
         * Helper để dừng hẹn giờ từ bên ngoài service
         */
        fun stopTimer(context: Context) {
            val intent = Intent(context, MediaTimerService::class.java).apply {
                action = ACTION_STOP_TIMER
            }
            context.startService(intent)
        }
    }
}