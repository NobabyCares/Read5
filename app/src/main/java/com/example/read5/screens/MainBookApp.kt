package com.example.read5.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home // ✅ 只导入 Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController



data class NavItem(val title: String, val route: String)



// ——————— App 入口 ———————
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBookApp () {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
        },
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    NavItem("阅读", "reading"),
                    NavItem("书架", "bookshelf"),
                    NavItem("有声书", "audiobook"),
                    NavItem("我", "profile")
                )

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Home, contentDescription = null) }, // ✅ 统一用 Home
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }

                    )
                }
            }
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = "bookshelf",
            modifier = Modifier.padding(padding)
        ) {
            composable("bookshelf") {
                BookShelfScreen(navController)
            }
            composable("reading") { CenteredText("跳转：阅读") }
            composable("audiobook") { CenteredText("跳转：有声书") }
            composable("profile") { CenteredText("跳转：我的") }
        }
    }
}



@Composable
fun BookAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        typography = Typography(),
        content = content
    )
}