package com.monkey.mediatimer.common

import android.content.Context
import com.monkey.mediatimer.domain.MediaControllerMgr
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MediaTimerModule {

    @Provides
    @Singleton
    fun provideMediaControllerManager(@ApplicationContext context: Context): MediaControllerMgr =
        MediaControllerMgr(context)

}