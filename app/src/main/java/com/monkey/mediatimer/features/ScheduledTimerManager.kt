package com.monkey.mediatimer.features

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.monkey.domain.repository.BatterySharedPreferenceRepository
import com.monkey.domain.repository.BatterySharedPreferenceRepository.Companion.KEY_SCHEDULED_TIMERS
import com.monkey.mediatimer.R
import com.monkey.mediatimer.features.ScheduledTimerReceiver.Companion.ACTION_EXECUTE_SCHEDULED_TIMER
import com.monkey.mediatimer.features.ScheduledTimerReceiver.Companion.EXTRA_TIMER_DURATION
import com.monkey.mediatimer.features.ScheduledTimerReceiver.Companion.EXTRA_TIMER_ID
import com.monkey.mediatimer.features.ScheduledTimerReceiver.Companion.EXTRA_USE_SLEEP_MODE
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject

class ScheduledTimerManager @Inject constructor(
    @ApplicationContext val context: Context,
    private val sharedPrefs: BatterySharedPreferenceRepository
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val _scheduledTimers = MutableStateFlow<List<ScheduledTimer>>(emptyList())
    val scheduledTimer: StateFlow<List<ScheduledTimer>> = _scheduledTimers.asStateFlow()

    init {
        // Tải danh sách hẹn giờ đã lưu
        loadScheduledTimers()
    }

    /**
     * Thêm một hẹn giờ theo lịch mới
     */
    fun addScheduledTimer(
        name: String,
        hour: Int,
        minute: Int,
        durationMinutes: Int,
        enabledDays: Set<DayOfWeek>,
        useSleepMode: Boolean = false,
        isEnabled: Boolean = true
    ): ScheduledTimer {
        val timer = ScheduledTimer(
            id = UUID.randomUUID().toString(),
            name = name,
            hour = hour,
            minute = minute,
            durationMinutes = durationMinutes,
            enabledDays = enabledDays,
            useSleepMode = useSleepMode,
            isEnabled = isEnabled
        )

        val currentList = _scheduledTimers.value.toMutableList()
        currentList.add(timer)
        _scheduledTimers.value = currentList

        // Lưu vào SharedPreferences
        saveScheduledTimers()

        // Đặt lịch cho báo thức nếu hẹn giờ được kích hoạt
        if (isEnabled) {
            scheduleAlarm(timer)
        }

        return timer
    }

    /**
     * Xóa một hẹn giờ theo lịch
     */
    fun removeScheduledTimer(timerId: String) {
        val timer = _scheduledTimers.value.find { it.id == timerId } ?: return

        // Hủy báo thức
        cancelAlarm(timer)

        // Xóa khỏi danh sách
        val currentList = _scheduledTimers.value.toMutableList()
        currentList.removeAll { it.id == timerId }
        _scheduledTimers.value = currentList

        // Lưu vào SharedPreferences
        saveScheduledTimers()
    }

    /**
     * Cập nhật trạng thái bật/tắt của một hẹn giờ
     */
    fun updateTimerEnabled(timerId: String, isEnabled: Boolean) {
        val currentList = _scheduledTimers.value.toMutableList()
        val timerIndex = currentList.indexOfFirst { it.id == timerId }

        if (timerIndex != -1) {
            val timer = currentList[timerIndex]
            val updatedTimer = timer.copy(isEnabled = isEnabled)
            currentList[timerIndex] = updatedTimer

            if (isEnabled) {
                scheduleAlarm(updatedTimer)
            } else {
                cancelAlarm(timer)
            }

            _scheduledTimers.value = currentList
            saveScheduledTimers()
        }
    }

    /**
     * Cập nhật thông tin của một hẹn giờ theo lịch
     */
    fun updateScheduledTimer(
        timerId: String,
        name: String? = null,
        hour: Int? = null,
        minute: Int? = null,
        durationMinutes: Int? = null,
        enabledDays: Set<DayOfWeek>? = null,
        useSleepMode: Boolean? = null
    ) {
        val currentList = _scheduledTimers.value.toMutableList()
        val timerIndex = currentList.indexOfFirst { it.id == timerId }

        if (timerIndex != -1) {
            val timer = currentList[timerIndex]

            // Hủy báo thức cũ
            cancelAlarm(timer)

            // Tạo timer mới với các thông tin cập nhật
            val updatedTimer = timer.copy(
                name = name ?: timer.name,
                hour = hour ?: timer.hour,
                minute = minute ?: timer.minute,
                durationMinutes = durationMinutes ?: timer.durationMinutes,
                enabledDays = enabledDays ?: timer.enabledDays,
                useSleepMode = useSleepMode ?: timer.useSleepMode
            )

            currentList[timerIndex] = updatedTimer

            // Đặt lại báo thức nếu được kích hoạt
            if (updatedTimer.isEnabled) {
                scheduleAlarm(updatedTimer)
            }

            _scheduledTimers.value = currentList
            saveScheduledTimers()
        }
    }

    /**
     * Lên lịch báo thức cho hẹn giờ
     */
    private fun scheduleAlarm(timer: ScheduledTimer) {
        if (timer.enabledDays.isEmpty()) return

        // Tạo intent để kích hoạt khi báo thức đến giờ
        val intent = Intent(context, ScheduledTimerReceiver::class.java).apply {
            action = ACTION_EXECUTE_SCHEDULED_TIMER
            putExtra(EXTRA_TIMER_ID, timer.id)
            putExtra(EXTRA_TIMER_DURATION, timer.durationMinutes)
            putExtra(EXTRA_USE_SLEEP_MODE, timer.useSleepMode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            timer.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Tính thời gian kích hoạt tiếp theo
        val nextTriggerTime = getNextTriggerTime(timer.hour, timer.minute, timer.enabledDays)

        // Đặt báo thức
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextTriggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                nextTriggerTime,
                pendingIntent
            )
        }
    }

    /**
     * Hủy báo thức cho hẹn giờ
     */
    private fun cancelAlarm(timer: ScheduledTimer) {
        val intent = Intent(context, ScheduledTimerReceiver::class.java).apply {
            action = ACTION_EXECUTE_SCHEDULED_TIMER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            timer.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    /**
     * Tính thời gian kích hoạt tiếp theo cho hẹn giờ
     */
    private fun getNextTriggerTime(hour: Int, minute: Int, enabledDays: Set<DayOfWeek>): Long {
        if (enabledDays.isEmpty()) return System.currentTimeMillis()

        val now = LocalDateTime.now()
        val targetTime = LocalTime.of(hour, minute)

        // Kiểm tra các ngày được kích hoạt
        var daysToAdd = 0
        var dayOfWeek = now.dayOfWeek

        // Nếu thời gian hôm nay đã qua
        if (now.toLocalTime().isAfter(targetTime)) {
            daysToAdd = 1 // Bắt đầu kiểm tra từ ngày mai
        }

        // Tìm ngày tiếp theo được kích hoạt
        var found = false
        while (!found && daysToAdd < 8) { // Kiểm tra tối đa 7 ngày tiếp theo
            dayOfWeek = now.dayOfWeek.plus(daysToAdd.toLong())
            if (enabledDays.contains(dayOfWeek)) {
                found = true
            } else {
                daysToAdd++
            }
        }

        if (!found) return System.currentTimeMillis() // Không tìm thấy ngày phù hợp

        // Tạo thời gian kích hoạt
        val triggerDate = now.plusDays(daysToAdd.toLong())
            .withHour(hour)
            .withMinute(minute)
            .withSecond(0)
            .withNano(0)

        return ZonedDateTime.of(triggerDate, ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    /**
     * Tải danh sách hẹn giờ từ SharedPreferences
     */
    private fun loadScheduledTimers() {
        val timerListJson = sharedPrefs.scheduledTimer
        // Trong ứng dụng thực, sẽ phân tích chuỗi JSON thành danh sách hẹn giờ
        // Hiện tại, chúng ta sử dụng danh sách trống
        _scheduledTimers.value = emptyList()
    }

    /**
     * Lưu danh sách hẹn giờ vào SharedPreferences
     */
    private fun saveScheduledTimers() {
        // Trong ứng dụng thực, sẽ chuyển đổi danh sách hẹn giờ thành chuỗi JSON
        // và lưu vào SharedPreferences
        CoroutineScope(Dispatchers.IO).launch {
            sharedPrefs.save(KEY_SCHEDULED_TIMERS, "[]")
        }

    }
    data class ScheduledTimer(
        val id: String,
        val name: String,
        var hour: Int,
        val minute: Int,
        val durationMinutes: Int,
        val enabledDays: Set<DayOfWeek>,
        val useSleepMode: Boolean,
        val isEnabled: Boolean
    ) {
        val formattedTime
            get() = String.format("%02d:%02d", hour, minute)
        val formattedDays: String
            @Composable
            get() {
                if (enabledDays.size == 7) return stringResource(R.string.daily)
                val weekdaySet = setOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY,
                    DayOfWeek.SATURDAY,
                    DayOfWeek.SUNDAY
                )
                if (enabledDays.containsAll(weekdaySet) && enabledDays.size == 5) return stringResource(
                    R.string.days_of_the_week
                )
                if (enabledDays.containsAll(
                        setOf(
                            DayOfWeek.SATURDAY,
                            DayOfWeek.SUNDAY
                        )
                    ) && enabledDays.size == 2
                ) return stringResource(R.string.weekend)

                val dayNames = enabledDays.map { it.name }.toTypedArray()
                return dayNames.joinToString { ", " }
            }
    }

    /**
     * Lên lịch lại cho hẹn giờ sau khi được kích hoạt
     */
    fun rescheduleTimer(timerId: String) {
        val timer = _scheduledTimers.value.find { it.id == timerId } ?: return
        scheduleAlarm(timer)
    }

    companion object {
        private const val PREFERENCES_NAME = "scheduled_timers_preferences"

    }
}