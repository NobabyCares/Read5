/*
package com.example.read5.screens.readview.comic

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.read5.singledata.DocumentHolder
import com.example.read5.viewmodel.comic.ComicViewModel

@Composable
fun ComicScreen(
    onBack: () -> Unit = {}
) {
    val TAG = "ComicScreen"
    val comicViewModel: ComicViewModel = hiltViewModel()

    // 收集 currentPage 状态
    val currentPage by comicViewModel.currentPage.collectAsState()

    val context = LocalContext.current
    val lazyListState = rememberLazyListState()
    var error by remember { mutableStateOf<String?>(null) }

    val path = DocumentHolder.currentItem?.path ?: return

    LaunchedEffect(path) {
        try {
            comicViewModel.initLoader(context, path)
            error = null
        } catch (e: Exception) {
            error = "加载失败:  ${e.message}" // ✅ 修复字符串模板（无空格）
        }
    }

    Button(onClick = {comicViewModel.loadPage(0)}) {
        Text(text = "Load image")
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        when {
            error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = error!!, color = Color.White)
                }
            }

            else -> {
                currentPage?.let {

                    Image(
                        bitmap = it,
                        contentDescription = "描述图片内容（无障碍）",
                        modifier = Modifier.fillMaxSize(), // 或其他尺寸
                        contentScale = ContentScale.Fit // 可选：控制缩放方式
                    )
                }
            }
        }
    }
}



*/
