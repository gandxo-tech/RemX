package com.example

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen

sealed class Screen(val route: String, val title: String, val icon: @Composable () -> Unit) {
    object Feed : Screen("feed", "Souvenirs", { Icon(Icons.Filled.List, contentDescription = "Souvenirs") })
    object Chat : Screen("chat", "Mémoire", { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Mémoire") })
    object Profile : Screen("profile", "Espace", { Icon(Icons.Filled.Person, contentDescription = "Espace") })
}

@Composable
fun MainApp(viewModel: MainViewModel = viewModel()) {
    val appState by viewModel.appState.collectAsState()

    androidx.compose.animation.Crossfade(targetState = appState, label = "AppTransition") { state ->
        when (state) {
            is AppState.Loading -> {
                // Optional loading indication
            }
            is AppState.NeedsOnboarding -> {
                OnboardingScreen(
                    onRegister = { name, password, onResult ->
                        viewModel.registerUser(name, password, onResult)
                    },
                    onLogin = { name, password, onResult ->
                        viewModel.loginUser(name, password, onResult)
                    }
                )
            }
            is AppState.Ready -> {
                MainAppContent()
            }
        }
    }
}

@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    val items = listOf(Screen.Feed, Screen.Chat, Screen.Profile)

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val isBottomBarVisible = currentDestination?.route in items.map { it.route }
            
            if (isBottomBarVisible) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = screen.icon,
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.testTag("nav_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Feed.route, Modifier.padding(innerPadding)) {
            composable(Screen.Feed.route) { 
                FeedScreen(onNavigateToDetail = { reelId -> 
                    navController.navigate("detail/$reelId")
                }) 
            }
            composable(Screen.Chat.route) { ChatScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
            composable(
                "detail/{reelId}",
                arguments = listOf(navArgument("reelId") { type = NavType.StringType })
            ) { backStackEntry ->
                val reelId = backStackEntry.arguments?.getString("reelId") ?: ""
                DetailScreen(reelId = reelId, onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
