package com.monkey.mediatimer.common

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap

data class MediaSessionInfo(
    val packageName: String = "",
    val title: String = "",
    val artist: String = "",
    val albumArt: ImageBitmap? = null,
    val isPlaying: Boolean = false,// todo using state for checking stop
    val duration: Long = 0L,
    val currentPosition: Long = 0L,
    val artIcon: Bitmap? = null
)
