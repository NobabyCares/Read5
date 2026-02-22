package com.example.read5.screens.route

import androidx.compose.animation.core.StartOffsetType
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontVariation
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.read5.screens.BookShelfScreen
import com.example.read5.screens.CenteredText
import com.example.read5.screens.auth.SettingsScreen
import com.example.read5.screens.myview.DatabaseQueryScreen
import com.example.read5.screens.myview.IsShowScreen
import com.example.read5.screens.myview.MyViewScreen
import com.example.read5.screens.readview.comic.HorizontalComicReader
import com.example.read5.screens.readview.comic.VerticalComicReader
import com.example.read5.screens.sortbar.SortField
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
    storeHouseViewModel: StoreHouseViewModel,
) {


    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(paddingValues),
    ) {
        // 1. 在 NavGraph 中定义带参数的路由
        composable(
            route = "bookshelf/{displayMode}",
            arguments = listOf(
                navArgument("displayMode") {
                    defaultValue = "bookdesk"
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val displayMode = backStackEntry.arguments?.getString("displayMode") ?: "bookdesk"

            BookShelfScreen(
                navController = navController,
                searchItemInfo = searchItemInfo,
                storeHouseModel = storeHouseViewModel,
                displayMode = displayMode,
            )
        }
        //跳转界面
        composable("reading") {
            CenteredText("跳转：阅读")
        }
        //我的设置界面
        composable("my_view") {
            MyViewScreen(navController,  searchItemInfo = searchItemInfo)
        }
        //隐藏项目
        composable("item_not_show") {
            IsShowScreen(navHostController = navController, searchItemInfo = searchItemInfo)
        }


        //密码设置界面
        composable("simple_password") {
            SettingsScreen()
        }
        //竖屏阅读
        composable("vertical_comic_view") {
            VerticalComicReader(navController)
        }
        //横屏阅读
        composable("horizon_comic_view") {
            HorizontalComicReader(navController = navController)
        }

        //横屏阅读
        composable("all_data_search") {
            DatabaseQueryScreen()
        }


    }
}