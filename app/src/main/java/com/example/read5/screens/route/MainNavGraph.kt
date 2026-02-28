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
import com.example.read5.screens.auth.PasssWordSettingsScreen
import com.example.read5.screens.myview.DatabaseQueryScreen
import com.example.read5.screens.myview.MyViewScreen
import com.example.read5.screens.readview.comic.HorizontalComicReader
import com.example.read5.screens.readview.comic.VerticalComicReader
import com.example.read5.screens.sortbar.SortField
import com.example.read5.viewmodel.storehouse.StoreHouseViewModel


//路由控制, 后面添加路由
@Composable
fun MainNavGraph(
    navController: NavHostController,
    startDestination: String,
) {


    NavHost(
        navController = navController,
        startDestination = startDestination,
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
                navHostController = navController,
                displayMode = displayMode,
            )
        }


        //我的设置界面
        composable(
            route = "my_view/{displayMode}",
            arguments = listOf(
                navArgument("displayMode") {
                    defaultValue="Home"
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val displayMode = backStackEntry.arguments?.getString("displayMode") ?: "Home"
            MyViewScreen(navController, displayMode)
        }



        //竖屏阅读
        composable(route = "vertical_comic_view/{offsetY}",
            arguments = listOf(
                navArgument("offsetY") {
                    defaultValue = 0
                }
            )) {backStackEntry ->
            val offsetY = backStackEntry.arguments?.getInt("offsetY") ?: 0
            VerticalComicReader(navController, offsetY)
        }
        //横屏阅读
        composable(route = "horizon_comic_view/{offsetY}",
            arguments = listOf(
                navArgument("offsetY") {
                    defaultValue = 0
                }
            )) {backStackEntry ->
            val offsetY = backStackEntry.arguments?.getInt("offsetY") ?: 0
            HorizontalComicReader(navController = navController, offsetY)
        }
    }
}