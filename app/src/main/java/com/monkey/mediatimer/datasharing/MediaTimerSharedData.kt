package com.monkey.mediatimer.datasharing

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

interface MediaTimerSharedData {
    val sharedEvents: Map<String, @JvmSuppressWildcards MutableSharedFlow<*>>
    val sharedStates: Map<String, @JvmSuppressWildcards MutableStateFlow<*>>
}

@Suppress("UNCHECKED_CAST")
fun <T> MediaTimerSharedData.getEvent(name: String): MutableSharedFlow<T>? {
    return sharedEvents[name]?.let {
        it as MutableSharedFlow<T>
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> MediaTimerSharedData.getState(name: String): MutableStateFlow<T>? {
    return sharedStates[name]?.let {
        it as MutableStateFlow<T>
    }
}