package com.example.read5.screens

import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.read5.screens.iteminfo.CenteredText
import com.example.read5.screens.readview.EpubScreen
import com.example.read5.screens.readview.comic.VirtualComicCanvas
import com.example.read5.screens.readview.pdfview.PdfView
import com.example.read5.viewmodel.storehouse.GetItemInfoViewModel
import com.example.read5.viewmodel.storehouse.StoreHouseViewModel
import java.net.URLDecoder
import java.net.URLEncoder



//路由控制, 后面添加路由
@Composable
fun MainNavGraph(
    navController: NavHostController,
    startDestination: String,
    // 用于接收 Scaffold 的 innerPadding
    paddingValues: PaddingValues,
    getItemInfoViewModel: GetItemInfoViewModel,
    storeHouseViewModel: StoreHouseViewModel
) {



    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(paddingValues),
    ) {
        composable("bookshelf") {
            BookShelfScreen(navController,getItemInfoViewModel = getItemInfoViewModel, storeHouseModel = storeHouseViewModel)
        }
        composable("reading") {
            CenteredText("跳转：阅读")
        }
        composable("audiobook") {
            CenteredText("跳转：有声书")
        }

        composable("pdf_view") {
            VirtualComicCanvas(navController)
        }

    }
}