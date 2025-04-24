package com.monkey.mediatimer.datasharing

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

private const val DEFAULT_FLOW_BUFFER_CAPACITY = 64

sealed class MediaTimerSharedEvent<T>(
    replay: Int = 0,
    extraBufferCapacity: Int = DEFAULT_FLOW_BUFFER_CAPACITY,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND
) : MutableSharedFlow<T> by MutableSharedFlow(replay, extraBufferCapacity, onBufferOverflow) {

    class StopMedias @Inject constructor() : MediaTimerSharedEvent<Unit>()
}

@InstallIn(SingletonComponent::class)
@Module
abstract class MediaTimerSharedEventModule {
    @Binds
    @IntoMap
    @StringKey("StopMedias")
    abstract fun bindStopMedias(stopMedias: MediaTimerSharedEvent.StopMedias): MutableSharedFlow<*>
}