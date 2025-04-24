package com.monkey.domain.model

/**
 * setup fou user interface
 */
data class UISettings(
    val darkMode: Boolean = false,
    val useSystemTheme: Boolean = true,
    val accentColor: String = "blue",
    val showNotifications: Boolean = true
)
