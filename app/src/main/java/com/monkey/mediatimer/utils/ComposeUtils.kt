package com.monkey.mediatimer.utils

import androidx.navigation.NavHostController
import com.monkey.mediatimer.R
import com.monkey.mediatimer.presentations.navigation.Screen

fun NavHostController.navigateWithPopupTo(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

fun getStringByRoute(route: String) = when (route) {
    Screen.HomeScreen.route -> R.string.home_screen_title
    Screen.TimerScreen.route -> R.string.timer_screen_title
    Screen.SettingsScreen.route -> R.string.settings_screen_title
    else -> R.string.app_name
}

