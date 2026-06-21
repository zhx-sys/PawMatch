package com.pawmatch.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pawmatch.app.data.api.ServerConfigManager
import com.pawmatch.app.data.api.TokenManager
import com.pawmatch.app.ui.navigation.PawMatchNavGraph
import com.pawmatch.app.ui.navigation.Screen
import com.pawmatch.app.ui.theme.PawMatchTheme
import com.pawmatch.app.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        TokenManager.init(this)
        ServerConfigManager.init(this)
        enableEdgeToEdge()
        setContent {
            PawMatchTheme {
                val authViewModel: AuthViewModel = viewModel()
                val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
                val navController = rememberNavController()

                val isShelter = TokenManager.userType == 1

                // 普通用户底部导航
                val normalBottomNavItems = listOf(
                    BottomNavItem(Screen.Home.route, Icons.Default.Home, "首页"),
                    BottomNavItem(Screen.Messages.route, Icons.Default.Chat, "消息"),
                    BottomNavItem(Screen.Friends.route, Icons.Default.People, "好友"),
                    BottomNavItem(Screen.Community.route, Icons.Default.Forum, "社区"),
                    BottomNavItem(Screen.Wiki.route, Icons.Default.MenuBook, "百科"),
                    BottomNavItem(Screen.PetGame.route, Icons.Default.Pets, "乐园"),
                    BottomNavItem(Screen.Profile.route, Icons.Default.Person, "我的")
                )

                // 救助站底部导航
                val shelterBottomNavItems = listOf(
                    BottomNavItem(Screen.ShelterDashboard.route, Icons.Default.Pets, "管理"),
                    BottomNavItem(Screen.ShelterPetsManage.route, Icons.Default.ListAlt, "宠物"),
                    BottomNavItem(Screen.ShelterAdoptions.route, Icons.Default.FactCheck, "审核"),
                    BottomNavItem(Screen.Messages.route, Icons.Default.Chat, "消息"),
                    BottomNavItem(Screen.Community.route, Icons.Default.Forum, "社区")
                )

                val bottomNavItems = if (isShelter) shelterBottomNavItems else normalBottomNavItems

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // 需要显示底部导航栏的路由
                val showBottomBar = bottomNavItems.any { it.route == currentDestination?.route }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (isLoggedIn && showBottomBar) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 3.dp
                            ) {
                                bottomNavItems.forEach { item ->
                                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = { Icon(item.icon, contentDescription = item.label) },
                                        label = { Text(item.label) }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        PawMatchNavGraph(
                            navController = navController,
                            isLoggedIn = isLoggedIn,
                            onLoginSuccess = {
                                val dest = if (TokenManager.userType == 1)
                                    Screen.ShelterDashboard.route
                                else
                                    Screen.Home.route
                                navController.navigate(dest) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

data class BottomNavItem(val route: String, val icon: ImageVector, val label: String)
