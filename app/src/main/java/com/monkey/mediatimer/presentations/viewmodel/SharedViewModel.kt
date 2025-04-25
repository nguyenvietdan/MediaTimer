package com.monkey.mediatimer.presentations.viewmodel

import androidx.lifecycle.ViewModel
import com.monkey.mediatimer.common.TimerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor() : ViewModel() {

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Inactive)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    fun updateTimerState(state: TimerState) {
        _timerState.value = state
    }
}