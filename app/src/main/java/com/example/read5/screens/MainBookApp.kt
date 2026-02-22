package com.example.read5.screens

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.read5.screens.route.MainNavGraph
import com.example.read5.screens.topbar.TopBarContent
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

    val searchItemInViewModel: SearchItemInfo = hiltViewModel()
    val storeHouseViewModel: StoreHouseViewModel = hiltViewModel()
    val updateItemInfo: UpdateItemInfo = hiltViewModel()

    //显示底部导航栏
    val showBottomBar = currentRoute in listOf("vertical_comic_view", "horizon_comic_view")

    var displayMode by rememberSaveable { mutableStateOf("bookdesk") }
    Scaffold(
        topBar = {
            // 只在特定页面隐藏 BottomBar（比如 PDF 阅读页）
            if (currentRoute != null) {
                if (currentRoute.startsWith("bookshelf")) {
                    TopBarContent(
                        navController, searchItemInViewModel,
                    )
                }
            }

        },
        bottomBar = {
            // 只在特定页面隐藏 BottomBar（比如 PDF 阅读页）
            if (!showBottomBar) {
                BottomBarScreen(navController)
            }
        }
    ) { padding ->
        MainNavGraph(navController,
            "bookshelf/bookdesk",
            padding,
            searchItemInViewModel,
            storeHouseViewModel,
           )
}
}



