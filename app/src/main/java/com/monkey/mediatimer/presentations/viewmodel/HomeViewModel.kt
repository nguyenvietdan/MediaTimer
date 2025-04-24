package com.monkey.mediatimer.presentations.viewmodel

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monkey.domain.repository.SharedPreferenceRepository
import com.monkey.mediatimer.common.MediaInfo
import com.monkey.mediatimer.di.IoDispatcher
import com.monkey.mediatimer.domain.MediaControllerMgr
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    val sharedPrefs: SharedPreferenceRepository,
    val mediaControllerMgr: MediaControllerMgr
) : ViewModel() {

    private val TAG = "HomeViewModel"

    //private val _remainTimer = MutableStateFlow<Long>(0L)
    //val remainTimer = _remainTimer.asStateFlow()
    private val _mediaSessions = MutableStateFlow<List<MediaInfo>>(emptyList())
    val mediaSessions: StateFlow<List<MediaInfo>> = _mediaSessions.asStateFlow()

    //private val _activeTimer = MutableStateFlow<TimerState>(TimerState.Inactive)
    //val activeTimer: StateFlow<TimerState> = _activeTimer.asStateFlow()

    private val timer: CountDownTimer? = null

    init {
        viewModelScope.launch(ioDispatcher) {
            sharedPrefs.stopTimer.collectLatest {
                //updateRemainTimer()
            }
        }
        observeMediaSession()
    }

    private fun observeMediaSession() {
        viewModelScope.launch(ioDispatcher) {
            mediaControllerMgr.activeMedias.collect { mediaInfoList ->
                Log.e(TAG, "observeMediaSession: ${mediaInfoList.size}" )
                _mediaSessions.value = mediaInfoList.map { mediaInfo ->
                    mediaInfo.copy()
                }
            }
        }
    }

    /*fun updateRemainTimer() {
        _remainTimer.value =
            (sharedPrefs.stopTimer.value - System.currentTimeMillis()).coerceAtLeast(0L)
    }*/

    fun togglePlayPauseState(packageName: String) {
        mediaControllerMgr.togglePlayPause(packageName)
    }

    fun stopMedia(packageName: String) {
        mediaControllerMgr.stopMedia(packageName)
    }

    fun changeVolume(packageName: String) {
        mediaControllerMgr.adjustVolume(context = context, volumeUp = false)
    }

    /**
     * for time is minutes to milliseconds
     */
    fun updateStopTimer(time: Int) {
        viewModelScope.launch(ioDispatcher) {
            val stopTimerMs = TimeUnit.MINUTES.toMillis(time.toLong())
            sharedPrefs.save(SharedPreferenceRepository.KEY_STOP_TIMER, stopTimerMs)
            // todo request stopMedia at here
        }
    }
}