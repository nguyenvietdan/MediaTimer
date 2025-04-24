package com.monkey.mediatimer.common

interface SettingItemData

data class SettingSwitchItemData(
    val title: String = "",
    val subTitle: String = "",
    val checked: Boolean = false,
    val enabled: Boolean = true,
    val onToggle: (Boolean) -> Unit
) : SettingItemData

data class SettingValueItemData(
    val title: String,
    val value: String,
    val onClick: () -> Unit
) : SettingItemData

data class SettingsActionItem(
    val title: String,
    val onClick: () -> Unit
): SettingItemData