package com.monkey.mediatimer.presentations.viewmodel

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.monkey.data.local.DefaultValue
import com.monkey.domain.repository.SharedPreferenceRepository
import com.monkey.domain.repository.SharedPreferenceRepository.Companion.KEY_END_TIME_MILLISECONDS
import com.monkey.mediatimer.common.TimerState
import com.monkey.mediatimer.di.IoDispatcher
import com.monkey.mediatimer.domain.StopMediaWorker
import com.monkey.mediatimer.service.MediaTimerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val sharedPrefs: SharedPreferenceRepository
) : ViewModel() {

    private val TAG = "TimerViewModel"
    private val isDebug = false

    private val _endTimeMilliseconds = MutableStateFlow(0L)// default 0 milliseconds
    val endTimeMilliseconds: StateFlow<Long> = _endTimeMilliseconds.asStateFlow()

    private val _selectedMinutes =
        MutableStateFlow(sharedPrefs.defaultTimerDuration.value.coerceAtMost(sharedPrefs.maxTimerDuration.value))// default 0 minutes
    val selectedMinutes: StateFlow<Long> = _selectedMinutes.asStateFlow()

    private val _remainTime = MutableStateFlow(0L)
    val remainTime: StateFlow<Long> = _remainTime.asStateFlow()

    private val _maxTimeDuration = MutableStateFlow(DefaultValue.DEFAULT_MAX_TIMER_DURATION)
    val maxTimeDuration: StateFlow<Long> = _maxTimeDuration.asStateFlow()

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Inactive)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private var timer: CountDownTimer? = null

    init {
        _endTimeMilliseconds.value = sharedPrefs.endTimeMilliseconds.value
        // init remain time if timer is active and running or paused.
        if (_endTimeMilliseconds.value > System.currentTimeMillis() && MediaTimerService.timerState is TimerState.Active) {
            val remainingTime = _endTimeMilliseconds.value - System.currentTimeMillis()
            _selectedMinutes.value = TimeUnit.MILLISECONDS.toMinutes(remainingTime)
            _timerState.value = MediaTimerService.timerState.value.copy()
            if (MediaTimerService.timerState.value is TimerState.Running) {
                executeTimer((_timerState.value as TimerState.Running).remainingMillis)
            } else if (MediaTimerService.timerState.value is TimerState.Paused) {
                _remainTime.value = (_timerState.value as TimerState.Paused).remainingMillis
                timer?.cancel()
            }
        }

        observeTimerState()

        viewModelScope.launch {
            sharedPrefs.maxTimerDuration.collectLatest { maxDuration ->
                _maxTimeDuration.value = maxDuration
                if (_selectedMinutes.value > _maxTimeDuration.value) {
                    _selectedMinutes.value = _maxTimeDuration.value
                }
            }
        }
    }

    private fun observeTimerState() {
        viewModelScope.launch(ioDispatcher) {
            MediaTimerService.timerState.collect { timeState ->
                Log.e(TAG, "observeMediaSession: ${timeState}" )
            }
        }
    }

    fun updateSelectedMinutes(minutes: Long) {
        viewModelScope.launch(ioDispatcher) {
            _selectedMinutes.value = minutes.coerceIn(
                0L,
                _maxTimeDuration.value
            )
        }
    }

    fun startTimer() {
        val endTime = minuteToCurrentMilliseconds(_selectedMinutes.value)
        viewModelScope.launch(ioDispatcher) {
            _endTimeMilliseconds.value = endTime
            sharedPrefs.save(KEY_END_TIME_MILLISECONDS, _endTimeMilliseconds.value)
        }
        MediaTimerService.startTimer(
            context,
            selectedMinutes.value.toInt(),
            sharedPrefs.sleepModeEnabled.value
        )
        // current only using service for handle stop media when timer end, but not use worker to stop media now.
        if (isDebug) {
            requestStopMedia(TimeUnit.MINUTES.toMillis(_selectedMinutes.value))
        }
        countDownRemainingTime(TimeUnit.MINUTES.toMillis(_selectedMinutes.value))
        // use coroutine to count down time instead of countdown timer because it's more flexible and easy to test.
        if (isDebug) {
            startCountDown(endTime)
        }
    }

    fun cancelTimer() {
        _remainTime.value = 0L
        MediaTimerService.stopTimer(context)
        timer?.cancel()
        _timerState.value = TimerState.Inactive
    }

    fun pauseOrResumeTimer() {
        if (_timerState.value is TimerState.Running) {
            MediaTimerService.pauseTimer(context)
            pauseTimer()
        } else {
            MediaTimerService.resumeTimer(context)
            resumeTimer()
        }
    }

    private fun startCountDown(endTime: Long) {
        viewModelScope.launch {
            while (System.currentTimeMillis() < endTime /*&& _isTimerActive.value*/) {
                val remainingMillis = endTime - System.currentTimeMillis()
                if (remainingMillis <= 0) {
                    _remainTime.value = 0L
                    break
                }
                _remainTime.value = remainingMillis
                delay(1000) // update every seconds
            }
        }
    }

    private fun countDownRemainingTime(durationMillis: Long) {
        timer?.cancel()
        val startTimer = System.currentTimeMillis()
        val endTimer = durationMillis + startTimer
        _timerState.value = TimerState.Running(
            startTimeMillis = startTimer,
            endTimeMillis = endTimer,
            remainingMillis = durationMillis,
            totalDurationMillis = durationMillis,
            useSleepMode = false
        )
        executeTimer(durationMillis)
    }

    private fun resumeTimer() {
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
            executeTimer(currentState.remainingMillis)
        }
    }

    private fun executeTimer(durationMillis: Long) {
        Log.i(TAG, "executeTimer: $durationMillis")
        timer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val currentState = _timerState.value
                if (currentState is TimerState.Running) {
                    _timerState.value = currentState.copy(remainingMillis = millisUntilFinished)
                    _remainTime.value = millisUntilFinished
                }
            }

            override fun onFinish() {
                _remainTime.value = 0L
                _timerState.value = TimerState.Inactive
            }
        }
        timer?.start()
    }

    private fun pauseTimer() {
        val currentState = _timerState.value
        if (currentState is TimerState.Running) {
            _timerState.value = TimerState.Paused(
                startTimeMillis = currentState.startTimeMillis,
                pausedAtMillis = System.currentTimeMillis(),
                remainingMillis = currentState.remainingMillis,
                totalDurationMillis = currentState.totalDurationMillis,
                useSleepMode = currentState.useSleepMode
            )
            timer?.cancel()
        }
    }

    private fun requestStopMedia(time: Long) {
        Log.i(TAG, "requestStopMedia: time $time")
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag("stop_media_task")
        val stopMediaRequest = OneTimeWorkRequestBuilder<StopMediaWorker>().setInitialDelay(
            time,
            TimeUnit.MILLISECONDS
        ).addTag("stop_media_task").build()
        workManager.enqueue(stopMediaRequest)
    }

    private fun minuteToCurrentMilliseconds(minutes: Long) =
        TimeUnit.MINUTES.toMillis(minutes) + System.currentTimeMillis()
}