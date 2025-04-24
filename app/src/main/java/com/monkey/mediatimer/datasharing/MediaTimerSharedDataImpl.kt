package com.monkey.mediatimer.datasharing

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaTimerSharedDataImpl @Inject constructor(
    mediaSharedEvents: Map<String, @JvmSuppressWildcards MutableSharedFlow<*>>,
    mediaStateEvents: Map<String, @JvmSuppressWildcards MutableStateFlow<*>>
) : MediaTimerSharedData {

    override val sharedEvents: Map<String, MutableSharedFlow<*>> = mediaSharedEvents
    override val sharedStates: Map<String, MutableStateFlow<*>> = mediaStateEvents
}

@InstallIn(SingletonComponent::class)
@Module
abstract class MediaTimerSharedDataModule {

    @Binds
    abstract fun binds(impl: MediaTimerSharedDataImpl): MediaTimerSharedData
}