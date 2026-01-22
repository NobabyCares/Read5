package com.example.read5.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.read5.screens.readview.PdfView
import java.net.URLDecoder

//路由控制, 后面添加路由
@Composable
fun MainNavGraph(
    navController: NavHostController,
    startDestination: String,
    paddingValues: PaddingValues // 用于接收 Scaffold 的 innerPadding
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable("bookshelf") {
            BookShelfScreen(navController)
        }
        composable("reading") {
            CenteredText("跳转：阅读")
        }
        composable("audiobook") {
            CenteredText("跳转：有声书")
        }


        composable(
            route = "pdfViewer/{filePath}", // 参数名改为 filePath
            arguments = listOf(
                navArgument("filePath") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // ✅ 关键：获取并解码参数
            val encodedPath = backStackEntry.arguments?.getString("filePath") ?: ""
            val decodedPath = URLDecoder.decode(encodedPath, "UTF-8")
            PdfView(path = decodedPath, modifier = Modifier.fillMaxSize())
        }
    }
}
