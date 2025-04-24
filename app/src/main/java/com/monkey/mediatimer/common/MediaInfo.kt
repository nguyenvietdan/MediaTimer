package com.monkey.mediatimer.common

import android.graphics.Bitmap
import android.media.session.MediaController
import android.media.session.PlaybackState

// todo move to data late

data class MediaInfo(
    val id: Int,
    val packageName: String,
    val appName: String,
    val title: String,
    val artist: String,
    val state: Int,
    val duration: Long = 0L,
    val position: Long = 0L,
    val controller: MediaController? = null,
    val artIcon: Bitmap? = null,
    val appIcon: Int? = null,
    val specialFeatures: List<SpecialFeature>? = null
) {
    fun isPlaying() = state == PlaybackState.STATE_PLAYING
}

