package com.example

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
        NavHost(
            navController = navController,
            startDestination = Screen.Feed.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(250))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(250))
            }
        ) {
            composable(
                route = Screen.Feed.route,
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth / 4 },
                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(250))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth / 4 },
                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(300))
                }
            ) { 
                FeedScreen(onNavigateToDetail = { reelId -> 
                    navController.navigate("detail/$reelId")
                }) 
            }
            composable(Screen.Chat.route) { ChatScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
            composable(
                route = "detail/{reelId}",
                arguments = listOf(navArgument("reelId") { type = NavType.StringType }),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(380, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(300)) + scaleIn(
                        initialScale = 0.94f,
                        animationSpec = tween(380, easing = FastOutSlowInEasing)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth / 4 },
                        animationSpec = tween(320, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(250))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth / 4 },
                        animationSpec = tween(320, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(380, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(300)) + scaleOut(
                        targetScale = 0.94f,
                        animationSpec = tween(380, easing = FastOutSlowInEasing)
                    )
                }
            ) { backStackEntry ->
                val reelId = backStackEntry.arguments?.getString("reelId") ?: ""
                DetailScreen(reelId = reelId, onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
