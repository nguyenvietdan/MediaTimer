package com.monkey.mediatimer.di

import android.content.Context
import com.monkey.data.reposiitory.BatterySharedPreferenceImplRepositoryImpl
import com.monkey.data.reposiitory.DefaultSharedPreferenceValueImpl
import com.monkey.data.reposiitory.SharedPreferenceImplRepositoryImpl
import com.monkey.domain.repository.BatterySharedPreferenceRepository
import com.monkey.domain.repository.DefaultSharedPreferenceValue
import com.monkey.domain.repository.SharedPreferenceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun provideSharedPrefDefaultValue(): DefaultSharedPreferenceValue =
        DefaultSharedPreferenceValueImpl()

    @Provides
    @Singleton
    fun provideSharedPreference(
        @ApplicationContext context: Context,
        defaultValue: DefaultSharedPreferenceValue
    ): SharedPreferenceRepository = SharedPreferenceImplRepositoryImpl(context, defaultValue)

    @Provides
    @Singleton
    fun provideBatterySharedPreference(
        @ApplicationContext context: Context,
        defaultValue: DefaultSharedPreferenceValue
    ): BatterySharedPreferenceRepository =
        BatterySharedPreferenceImplRepositoryImpl(context, defaultValue)
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SharedPreferenceRepositoryEntryPoint {
    fun sharedPreferenceRepository(): SharedPreferenceRepository
}