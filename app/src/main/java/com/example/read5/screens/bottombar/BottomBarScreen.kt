package com.example.read5.screens.bottombar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState


data class NavItem(val title: String, val route: String)
//底部
@Composable
fun BottomBarScreen(navHostController: NavController) {
    val navBackStackEntry by navHostController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    NavigationBar {
        val items = listOf(
            NavItem("书架", "bookshelf/bookdesk"),
            NavItem("我", "my_view/home")
        )

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(Icons.Filled.Home, contentDescription = null) }, // ✅ 统一用 Home
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navHostController.navigate(item.route) {
                        popUpTo(navHostController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }

            )
        }
    }
}