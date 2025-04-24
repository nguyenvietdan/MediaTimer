package com.monkey.mediatimer.presentations.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monkey.mediatimer.R
import com.monkey.mediatimer.common.NavItem
import com.monkey.mediatimer.presentations.navigation.Screen
import com.monkey.mediatimer.presentations.theme.MediaTimerTheme
import com.monkey.mediatimer.utils.getStringByRoute
import kotlinx.coroutines.launch

@Composable
fun AppDrawer(
    drawerState: DrawerState,
    currentScreen: String,
    onScreenSelected: (Screen) -> Unit,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val navigationItems = listOf(
        NavItem(
            screen = Screen.HomeScreen,
            icon = Icons.Default.Home,
            contentDescription = "Home"
        ),
        NavItem(
            screen = Screen.TimerScreen,
            icon = ImageVector.vectorResource(
                null,
                context.resources,
                R.drawable.baseline_access_time_24
            ),
            contentDescription = "Timer"
        ),
        NavItem(
            screen = Screen.SettingsScreen,
            icon = Icons.Default.Settings,
            contentDescription = "Settings"
        )
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier.height(dimensionResource(R.dimen.nav_header_height)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = stringResource(R.string.media_timer_app),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    //Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider()// todo checking height
                    Spacer(modifier = Modifier.height(12.dp))
                    navigationItems.forEach { item ->
                        val selected = item.screen.route == currentScreen
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.contentDescription
                                )
                            },
                            label = { Text(text = stringResource(getStringByRoute(item.screen.route))) },
                            selected = selected,
                            onClick = {
                                onScreenSelected(item.screen)
                                coroutineScope.launch {
                                    drawerState.close()
                                }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        },
        content = content
    )
}

private fun getDrawerTitleResource(name: String) = when (name) {
    Screen.HomeScreen.title -> R.string.home_screen_title
    Screen.TimerScreen.title -> R.string.timer_screen_title
    Screen.SettingsScreen.title -> R.string.settings_screen_title
    else -> R.string.app_name
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AppDrawerPreview() {
    MediaTimerTheme {

        AppDrawer(
            drawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
            Screen.HomeScreen.route,
            onScreenSelected = {},
            content = {})
    }
}