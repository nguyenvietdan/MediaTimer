package com.monkey.mediatimer.common

import androidx.compose.ui.graphics.vector.ImageVector
import com.monkey.mediatimer.presentations.navigation.Screen

data class NavItem(
    val screen: Screen,
    val icon: ImageVector,
    val contentDescription: String
)