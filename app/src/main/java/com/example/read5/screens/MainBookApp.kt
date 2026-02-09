package com.example.read5.screens

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.read5.screens.topbar.TopBar
import com.example.read5.viewmodel.iteminfo.SearchItemInfo
import com.example.read5.viewmodel.iteminfo.UpdateItemInfo
import com.example.read5.viewmodel.storehouse.StoreHouseViewModel


data class NavItem(val title: String, val route: String)



// ——————— App 入口 ———————
@Composable
fun MainBookApp () {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val getItemInViewModel: SearchItemInfo = hiltViewModel()
    val storeHouseViewModel: StoreHouseViewModel = hiltViewModel()
    val updateItemInfo: UpdateItemInfo = hiltViewModel()

    Scaffold(
        topBar = {
            // 只在特定页面隐藏 BottomBar（比如 PDF 阅读页）
            if (currentRoute == "bookshelf") {
                TopBar(navController, getItemInViewModel, storeHouseViewModel)
            }

        },
        bottomBar = {
            // 只在特定页面隐藏 BottomBar（比如 PDF 阅读页）
            if (currentRoute == "bookshelf") {
                BottomBarScreen(navController)
            }
        }
    ) { padding ->
        MainNavGraph(navController, "bookshelf", padding, getItemInViewModel, storeHouseViewModel)
}
}



