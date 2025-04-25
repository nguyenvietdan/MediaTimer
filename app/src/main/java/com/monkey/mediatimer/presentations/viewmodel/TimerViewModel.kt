package com.monkey.mediatimer.presentations.viewmodel

import android.app.Application
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
import com.monkey.mediatimer.MediaTimerApplication
import com.monkey.mediatimer.common.TimerState
import com.monkey.mediatimer.di.IoDispatcher
import com.monkey.mediatimer.domain.StopMediaWorker
import com.monkey.mediatimer.service.MediaTimerService
import com.monkey.mediatimer.utils.isDebug
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val application: Application,
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val sharedPrefs: SharedPreferenceRepository
) : ViewModel() {

    private val TAG = "TimerViewModel"

    private val _endTimeMilliseconds = MutableStateFlow(0L)// default 0 milliseconds
    val endTimeMilliseconds: StateFlow<Long> = _endTimeMilliseconds.asStateFlow()

    private val _selectedMinutes =
        MutableStateFlow(sharedPrefs.defaultTimerDuration.value.coerceAtMost(sharedPrefs.maxTimerDuration.value))// default 0 minutes
    val selectedMinutes: StateFlow<Long> = _selectedMinutes.asStateFlow()

    private val _remainTime = MutableStateFlow(0L)
    val remainTime: StateFlow<Long> = _remainTime.asStateFlow()

    private val _maxTimeDuration = MutableStateFlow(DefaultValue.DEFAULT_MAX_TIMER_DURATION)
    val maxTimeDuration: StateFlow<Long> = _maxTimeDuration.asStateFlow()

    val sharedViewModel: SharedViewModel by lazy {
        (application as MediaTimerApplication).sharedViewModel
    }

    private var timer: CountDownTimer? = null

    init {
        _endTimeMilliseconds.value = sharedPrefs.endTimeMilliseconds.value
        // init remain time if timer is active and running or paused.

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

    // observe timer state from shared view model to update remain time.
    private fun observeTimerState() {
        viewModelScope.launch(ioDispatcher) {
            sharedViewModel.timerState.distinctUntilChanged { old, new ->
                return@distinctUntilChanged old == new
            }.collect { state ->
                when (state) {
                    is TimerState.Running -> {
                        _remainTime.value = state.remainingMillis
                    }
                    is TimerState.Paused -> _remainTime.value = state.remainingMillis

                    is TimerState.Inactive -> _remainTime.value = 0
                    else -> {}
                }
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
        // use coroutine to count down time instead of countdown timer because it's more flexible and easy to test.
        if (isDebug) {
            startCountDown(endTime)
        }
    }

    fun cancelTimer() {
        _remainTime.value = 0L
        MediaTimerService.stopTimer(context)
        timer?.cancel()
    }

    fun pauseOrResumeTimer() {
        if (sharedViewModel.timerState.value is TimerState.Running) {
            MediaTimerService.pauseTimer(context)
        } else {
            MediaTimerService.resumeTimer(context)
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