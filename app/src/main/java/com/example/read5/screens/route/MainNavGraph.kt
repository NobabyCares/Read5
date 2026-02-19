package com.example.read5.screens.route

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.read5.screens.BookShelfScreen
import com.example.read5.screens.CenteredText
import com.example.read5.screens.myview.IsShowScreen
import com.example.read5.screens.myview.MyViewScreen
import com.example.read5.screens.readview.comic.HorizontalComicReader
import com.example.read5.screens.readview.comic.VerticalComicReader
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
        composable("my_view") {
            MyViewScreen(navController,  searchItemInfo = searchItemInfo)
        }

        composable("item_not_show") {
            IsShowScreen(navHostController = navController, searchItemInfo = searchItemInfo)
        }

        composable("vertical_comic_view") {
            VerticalComicReader(navController)
        }

        composable("horizon_comic_view") {
            HorizontalComicReader(navController = navController)
        }

    }
}