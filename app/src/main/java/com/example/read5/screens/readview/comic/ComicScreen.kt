package com.example.read5.screens.readview.comic

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.read5.bean.ComicPage
import com.example.read5.singledata.PdfDocumentHolder
import com.example.read5.utils.comic.LazyZipComicUtils
import com.example.read5.utils.comic.loadComicPages
import com.mxalbert.zoomable.Zoomable
import com.mxalbert.zoomable.rememberZoomableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.read5.viewmodel.comic.ComicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicScreen(
    onBack: () -> Unit = {}
) {
    val comicViewModel: ComicViewModel = hiltViewModel()

    val context = LocalContext.current
    val lazyListState = rememberLazyListState()
    var pages by remember { mutableStateOf<List<ComicPage>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val path = PdfDocumentHolder.currentItem?.path ?: return

    LaunchedEffect(path) {
        try {
            pages = loadComicPages(context, path)
            error = null
        } catch (e: Exception) {
            error = "加载失败:  ${e.message}" // ✅ 修复字符串模板（无空格）
        }
    }

    BackHandler(enabled = true, onBack = onBack)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = error!!, color = Color.White)
                }
            }
            pages == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            else -> {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    itemsIndexed(pages!!) { index, page ->
                        // ✅ 使用 Zoomable 包裹每一页
                        val zoomableState = rememberZoomableState(
                            minScale = 0.8f,      // 最小缩放（略小于1可微调布局）
                            maxScale = 5f,        // 最大放大5倍
                            doubleTapScale  = 2.5f // 双击放大到2.5倍
                        )

                        Zoomable(
                            state = zoomableState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.75f) // 保持竖版漫画比例
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(page.uri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Page  ${index + 1}",
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                PageIndicator(
                    currentPage = lazyListState.firstVisibleItemIndex + 1,
                    totalPages = pages!!.size,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                )

                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 8.dp, start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "返回",
                        tint = Color.White
                    )
                }
            }
        }
    }
}



