package com.monkey.domain.model

/**
 * common setup
 */
data class AppSettings(
    val startOnBoot: Boolean = false,
    val autoCheckForUpdates: Boolean = true,
    val analyticsEnabled: Boolean = true,
    val useExactAlarms: Boolean = true
)
