package com.example.read5.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home // ✅ 只导入 Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController


data class NavItem(val title: String, val route: String)



// ——————— App 入口 ———————
@Composable
fun MainBookApp () {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route



    Scaffold(
        topBar = {
        },
        bottomBar = {
            // 只在特定页面隐藏 BottomBar（比如 PDF 阅读页）
            if (currentRoute != "pdf_view") {
                BottomBarScreen(navController)
            }
        }
    ) { padding ->
        MainNavGraph(navController, "bookshelf", padding)
}
}



