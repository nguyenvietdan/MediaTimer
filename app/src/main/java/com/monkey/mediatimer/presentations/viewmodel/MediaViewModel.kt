package com.monkey.mediatimer.presentations.viewmodel

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monkey.mediatimer.datasharing.MediaTimerSharedData
import com.monkey.mediatimer.datasharing.MediaTimerSharedDataHolder
import com.monkey.mediatimer.datasharing.getEvent
import com.monkey.mediatimer.domain.MediaControllerMgr
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaSharedData: MediaTimerSharedData,
    private val mediaControllerMgr: MediaControllerMgr
) : ViewModel() {

    private val TAG = "MediaViewModel"

    private val _hasWriteSettingsPermission = MutableStateFlow(Settings.System.canWrite(context))
    val hasWriteSettingsPermission: StateFlow<Boolean> = _hasWriteSettingsPermission.asStateFlow()

    init {
        MediaTimerSharedDataHolder.mediaTimerSharedData = mediaSharedData
        Log.e(TAG, "init MediaViewModel" )
        mediaSharedData.getEvent<Unit>("StopMedias")?.onEach {
            stopAllMedias()
        }?.launchIn(viewModelScope)
    }

    fun checkWriteSettingsPermission() {
        _hasWriteSettingsPermission.value = Settings.System.canWrite(context)
    }

    private fun stopAllMedias() {
        //mediaControllerMgr.stopAllMedia()
    }
}