package com.monkey.mediatimer.domain

import android.content.Context
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.toMutableStateList
import com.monkey.mediatimer.common.AppSpecificConfig
import com.monkey.mediatimer.common.MediaInfo
import com.monkey.mediatimer.utils.getAppNameFromPackage
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaControllerMgr @Inject constructor(@ApplicationContext private val context: Context) {

    private val TAG = "MediaController"
    // todo checking move to domain

    private val _activeMedias = MutableStateFlow<List<MediaInfo>>(emptyList())
    val activeMedias: StateFlow<List<MediaInfo>> = _activeMedias.asStateFlow()
    private var _currentIndex = 0

    fun addOrUpdateMedia(mediaController: MediaController) {
        // todo checking add package to it
        Log.i(TAG, "addOrUpdateMedia: before")
        val metadata = mediaController.metadata
        val packageName = mediaController.packageName
        val appName = getAppNameFromPackage(context, packageName)
        val title = metadata?.description?.title?.toString() ?: "Unknown"
        val artist =
            metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "Unknown"
        val artIcon = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)

        val durationMs = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION)?.coerceAtLeast(0L) ?: 0L
        val state = mediaController.playbackState?.state ?: PlaybackState.STATE_NONE
        val currentPosition = mediaController.playbackState?.let {
            val timeDelta =
                SystemClock.elapsedRealtime() - it.lastPositionUpdateTime // Calculate time passed since last update
            it.position + (timeDelta * it.playbackSpeed).toLong() // Calculate current position
        }?.coerceIn(0, durationMs) ?: 0L
        val appConfig = AppSpecificConfig.getAppConfig(packageName)

        Log.i(
            TAG, "addOrUpdateMedia: checking icon ${artIcon}" +
                    " state: ${mediaController.playbackState?.state}" +
                    " currentPosition: $currentPosition"
        )
        val newActiveMedias = _activeMedias.value.toMutableStateList()
        val index = newActiveMedias.indexOfFirst { it.packageName == packageName }
        val id = if (index != -1) {
            newActiveMedias[index].id
        } else _currentIndex++
        val media = MediaInfo(
            id,
            packageName,
            appName,
            title,
            artist,
            state,
            durationMs,
            currentPosition,
            mediaController,
            artIcon,
            appConfig.icon,
            appConfig.specialFeatures
        )
        if (index != -1) {
            newActiveMedias[index] = media
        } else {
            newActiveMedias.add(media)
        }
        //newActiveMedias.updateOrAddItem(media)
        _activeMedias.value = newActiveMedias.sortedByDescending { it.id }
        Log.i(TAG, "addOrUpdateMedia: $packageName ${activeMedias.value.size}")
    }

    /**
     * Remove a media from the list of active medias. This will also stop the media if it's currently playing.
     */
    fun removeMedia(pkg: String) {
        Log.i(TAG, "removeMedia: $pkg ")
        val newActiveMedias = _activeMedias.value.toMutableStateList()
        newActiveMedias.removeIf { it.packageName == pkg }
        _activeMedias.value = newActiveMedias
    }

    /**
     * Clear all active medias from the list. This will also stop any currently playing media.
     */
    fun clearAll() {
        _activeMedias.value = emptyList()
    }

    fun stopAllMedia() {
        Log.i(TAG, "stopAllMedia: ")
        _activeMedias.value.forEach { mediaInfo ->
            mediaInfo.controller?.transportControls?.pause()
        }
    }

    /**
     * Toggle play/pause for the given package. If the package is not found in the list of active medias, do nothing.
     */
    fun togglePlayPause(pkg: String) {
        val mediaInfo = _activeMedias.value.find { it.packageName == pkg } ?: return
        if (mediaInfo.isPlaying()) {
            mediaInfo.controller?.transportControls?.pause()
        } else mediaInfo.controller?.transportControls?.play()
    }

    fun stopMedia(pkg: String) =
        _activeMedias.value.find { it.packageName == pkg }?.controller?.transportControls?.pause()

    fun stopAllMedias() {
        // todo checking current medias and stop them
        _activeMedias.value.forEach { mediaInfo ->
            mediaInfo.controller?.transportControls?.pause()
        }
        // Thay đổi trạng thái audio focus để dừng các ứng dụng không hỗ trợ điều khiển trực tiếp
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(
                android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .build()
            )
        } else {
            audioManager.abandonAudioFocus(null)
        }
    }

    fun adjustVolume(context: Context, volumeUp: Boolean) {
        // todo handle volume
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val direction = if (volumeUp) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            direction,
            AudioManager.FLAG_SHOW_UI
        )
    }

}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MediaControllerManagerEntryPoint {
    fun mediaControllerManager(): MediaControllerMgr
}