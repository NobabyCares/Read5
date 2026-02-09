package com.example.read5.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.read5.screens.readview.comic.VirtualComicCanvas
import com.example.read5.viewmodel.iteminfo.SearchItemInfo
import com.example.read5.viewmodel.storehouse.StoreHouseViewModel


//路由控制, 后面添加路由
@Composable
fun MainNavGraph(
    navController: NavHostController,
    startDestination: String,
    // 用于接收 Scaffold 的 innerPadding
    paddingValues: PaddingValues,
    searchItemInfo: SearchItemInfo,
    storeHouseViewModel: StoreHouseViewModel
) {



    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(paddingValues),
    ) {
        composable("bookshelf") {
            BookShelfScreen(navController,searchItemInfo = searchItemInfo, storeHouseModel = storeHouseViewModel)
        }
        composable("reading") {
            CenteredText("跳转：阅读")
        }
        composable("audiobook") {
            CenteredText("跳转：有声书")
        }

        composable("comic_view") {
            VirtualComicCanvas(navController)
        }

    }
}