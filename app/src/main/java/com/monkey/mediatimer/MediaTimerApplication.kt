package com.monkey.mediatimer

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.monkey.mediatimer.presentations.viewmodel.SharedViewModel
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MediaTimerApplication : Application(), ViewModelStoreOwner {

    private val appViewModelStore = ViewModelStore()

    val sharedViewModel: SharedViewModel by lazy {
        ViewModelProvider(this)[SharedViewModel::class.java]
    }

    override val viewModelStore: ViewModelStore = appViewModelStore
}