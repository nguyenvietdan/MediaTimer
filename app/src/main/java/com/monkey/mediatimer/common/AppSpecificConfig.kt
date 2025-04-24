package com.monkey.mediatimer.common

import com.monkey.mediatimer.R

/**
 * Cấu hình và hỗ trợ đặc biệt cho các ứng dụng media phổ biến
 */
object AppSpecificConfig {
    // Danh sách các ứng dụng được hỗ trợ đặc biệt
    val supportedApps = mapOf(
        "com.spotify.music" to AppConfig(
            appName = "Spotify",
            icon = R.drawable.baseline_music_note_24,// todo replace late
            priority = 100,
            specialFeatures = listOf(
                SpecialFeature.FADE_OUT_SUPPORT,
                SpecialFeature.CROSSFADE_CONTROL
            )
        ),
        "com.google.android.youtube" to AppConfig(
            appName = "YouTube",
            icon = R.drawable.baseline_music_note_24,// todo VideoLibrary,
            priority = 90,
            specialFeatures = listOf(SpecialFeature.BACKGROUND_PLAYBACK_CONTROL)
        ),
        "com.zhiliaoapp.musically" to AppConfig(
            appName = "TikTok",
            icon = R.drawable.baseline_music_note_24, // todo.VideoLibrary,
            priority = 80,
            specialFeatures = listOf(SpecialFeature.BACKGROUND_PLAYBACK_CONTROL)
        ),
        "com.netflix.mediaclient" to AppConfig(
            appName = "Netflix",
            icon = R.drawable.baseline_music_note_24, // todo .VideoLibrary,
            priority = 85,
            specialFeatures = listOf(SpecialFeature.SCREEN_DIMMING)
        ),
        "com.amazon.avod.thirdpartyclient" to AppConfig(
            appName = "Prime Video",
            icon = R.drawable.baseline_music_note_24, // todo.VideoLibrary,
            priority = 85,
            specialFeatures = listOf(SpecialFeature.SCREEN_DIMMING)
        ),
        "com.pandora.android" to AppConfig(
            appName = "Pandora",
            icon = R.drawable.baseline_music_note_24, // todo .MusicNote,
            priority = 70,
            specialFeatures = listOf(SpecialFeature.FADE_OUT_SUPPORT)
        ),
        "com.google.android.apps.podcasts" to AppConfig(
            appName = "Google Podcasts",
            icon = R.drawable.baseline_music_note_24, // todo.Podcasts,
            priority = 75,
            specialFeatures = listOf(SpecialFeature.SLEEP_MARKER, SpecialFeature.FADE_OUT_SUPPORT)
        )
    )

    // Lấy cấu hình của một ứng dụng cụ thể
    fun getAppConfig(packageName: String): AppConfig {
        return supportedApps[packageName] ?: AppConfig(
            appName = packageName,
            icon = R.drawable.baseline_music_note_24,// todo.MusicNote,
            priority = 0,
            specialFeatures = emptyList()
        )
    }

    // Kiểm tra xem package có phải là ứng dụng được hỗ trợ đặc biệt không
    fun isSpeciallySupportedApp(packageName: String): Boolean {
        return packageName in supportedApps
    }
}

// Cấu hình cho mỗi ứng dụng
data class AppConfig(
    val appName: String,
    val icon: Int,
    val priority: Int, // Số càng cao thì ưu tiên hiển thị càng cao
    val specialFeatures: List<SpecialFeature>
)

// Các tính năng đặc biệt mà chúng ta có thể hỗ trợ cho ứng dụng
enum class SpecialFeature {
    FADE_OUT_SUPPORT, // Hỗ trợ giảm dần âm lượng trước khi dừng
    BACKGROUND_PLAYBACK_CONTROL, // Khả năng điều khiển phát trong nền
    CROSSFADE_CONTROL, // Điều khiển chức năng crossfade của ứng dụng
    SCREEN_DIMMING, // Làm tối màn hình cho ứng dụng video
    SLEEP_MARKER // Đánh dấu vị trí ngủ trong podcast/audiobook
}