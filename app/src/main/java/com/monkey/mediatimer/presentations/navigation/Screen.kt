package com.monkey.mediatimer.presentations.navigation

sealed class Screen(val route: String, val title: String) {
    object HomeScreen : Screen("home", "Home")
    object SettingsScreen : Screen("settings", "Settings")
    object TimerScreen : Screen("timer", "Timer")
}