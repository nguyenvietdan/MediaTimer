package com.monkey.mediatimer.features

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.monkey.mediatimer.service.MediaTimerService
import javax.inject.Inject

class ScheduledTimerReceiver @Inject constructor(
    private val sleepModeManager: SleepModeManager,
    private val scheduledTimerManager: ScheduledTimerManager
) : BroadcastReceiver() {
    private val TAG = "ScheduledTimerReceiver"
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG, "onReceive: action ${intent?.action}")
        if (context == null || intent == null) return
        if (intent.action == ACTION_EXECUTE_SCHEDULED_TIMER) {
            val timerId = intent.getStringExtra(EXTRA_TIMER_ID) ?: return
            val durationMinutes = intent.getIntExtra(EXTRA_TIMER_DURATION, 0)
            val useSleepMode = intent.getBooleanExtra(EXTRA_USE_SLEEP_MODE, false)

            // Kích hoạt chế độ ngủ hoặc hẹn giờ thông thường
            if (useSleepMode) {
                sleepModeManager.activateSleepMode(durationMinutes)
            } else {
                MediaTimerService.startTimer(context, durationMinutes)
            }

            // Lên lịch cho lần tiếp theo

            scheduledTimerManager.rescheduleTimer(timerId)
        }
    }

    companion object {
        const val ACTION_EXECUTE_SCHEDULED_TIMER =
            "com.mediatimerapp.ACTION_EXECUTE_SCHEDULED_TIMER"
        const val EXTRA_TIMER_ID = "timer_id"
        const val EXTRA_TIMER_DURATION = "timer_duration"
        const val EXTRA_USE_SLEEP_MODE = "use_sleep_mode"
    }
}