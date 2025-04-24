package com.monkey.mediatimer.presentations.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.monkey.mediatimer.presentations.components.AppDrawer
import com.monkey.mediatimer.presentations.screens.Homescreen
import com.monkey.mediatimer.presentations.screens.SettingsScreenExtend
import com.monkey.mediatimer.presentations.screens.TimerScreen
import com.monkey.mediatimer.presentations.viewmodel.MediaViewModel
import com.monkey.mediatimer.presentations.viewmodel.TimerViewModel
import com.monkey.mediatimer.utils.navigateWithPopupTo

@Composable
fun MediaTimerNavigation(
    mediaViewModel: MediaViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var currentScreen by rememberSaveable { mutableStateOf(Screen.HomeScreen.route) }
    val scope = rememberCoroutineScope()

    val timerViewModel: TimerViewModel = hiltViewModel()

    AppDrawer(
        drawerState = drawerState,
        currentScreen = currentScreen,
        onScreenSelected = { screen ->
            navController.navigateWithPopupTo(screen.route)
            currentScreen = screen.route
        }
    ) {
        MediaTimerNavigationGraph(navController, drawerState, timerViewModel, mediaViewModel) { route ->
            currentScreen = route
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarScreen(title: String, openDrawer: () -> Unit) {
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth(),
        title = { Text(title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        navigationIcon = {
            IconButton(onClick = {
                openDrawer()
            }) {
                Icon(Icons.Default.Menu, contentDescription = "Drawer Menu")
            }
        })
}

@Composable
fun MediaTimerNavigationGraph(
    navController: NavHostController,
    drawerState: DrawerState,
    timerViewModel: TimerViewModel,
    mediaViewModel: MediaViewModel,
    onScreenChanged: (String) -> Unit,
) {
    NavHost(navController, startDestination = Screen.HomeScreen.route) {
        composable(Screen.HomeScreen.route) {
            onScreenChanged(Screen.HomeScreen.route)
            Homescreen(nav = navController, drawerState =  drawerState, timerViewModel = timerViewModel)
        }
        composable(Screen.TimerScreen.route) {
            onScreenChanged(Screen.TimerScreen.route)
            TimerScreen(nav = navController, drawerState =  drawerState, viewModel = timerViewModel)
        }
        composable(Screen.SettingsScreen.route) {
            onScreenChanged(Screen.SettingsScreen.route)
            SettingsScreenExtend(navController = navController, drawerState = drawerState, mediaViewModel = mediaViewModel)
            //SettingsScreen(navController = navController, drawerState =  drawerState, mediaViewModel = mediaViewModel)
        }
    }
}