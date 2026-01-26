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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.read5.singledata.PdfDocumentHolder
import com.mxalbert.zoomable.Zoomable
import com.mxalbert.zoomable.rememberZoomableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val lazyListState = rememberLazyListState()
    var pages by remember { mutableStateOf<List<ComicPage>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val path = PdfDocumentHolder.currentItem?.path
    if (path == null) {
        return
    }

    LaunchedEffect(path) {
        try {
            pages = loadComicPages(context, path)
            error = null
        } catch (e: Exception) {
            error = "加载失败:  $ {e.message}" // ✅ 修复字符串模板（无空格）
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
                                contentDescription = "Page  $ {index + 1}",
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

// 页面指示器（保持不变）
@Composable
fun PageIndicator(currentPage: Int, totalPages: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = Color.Black.copy(alpha = 0.6f),
        shadowElevation = 4.dp
    ) {
        Text(
            text = "  $currentPage /  $totalPages", // ✅ 修复字符串模板
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

// ========== 数据加载逻辑（保持不变）==========
data class ComicPage(val uri: Uri, val name: String)

private suspend fun loadComicPages(context: Context, storedPath: String): List<ComicPage> {
    return withContext(Dispatchers.IO) {
        val parts = storedPath.split('|', limit = 2)
        if (parts.size != 2) throw IllegalArgumentException("Invalid path format")

        val treeUri = Uri.parse(parts[0])
        val folderDocumentId = parts[1]

        val root = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, treeUri)
            ?: throw IllegalStateException("Failed to access tree URI")

        var current = root
        for (segment in folderDocumentId.split("/")) {
            if (segment.isEmpty()) continue
            val child = current.findFile(segment)
            if (child == null || !child.isDirectory) {
                throw IllegalStateException("Subfolder not found:  $segment") // ✅ 修复字符串模板
            }
            current = child
        }

        val children = current.listFiles() ?: emptyArray()
        val imageFiles = children.filter { it.isFile && isValidImageName(it.name) }
            .sortedBy { it.name?.lowercase() ?: "" }

        if (imageFiles.isEmpty()) throw IllegalStateException("No images found")

        imageFiles.map { file ->
            ComicPage(uri = file.uri, name = file.name ?: "unknown")
        }
    }
}

private fun isValidImageName(name: String?): Boolean {
    if (name.isNullOrBlank()) return false
    val ext = name.substringAfterLast('.', "").lowercase()
    return ext in setOf("jpg", "jpeg", "png", "webp", "bmp", "gif")
}