package com.example.read5.screens.readview.comic

import android.util.Log
import androidx.compose.animation.SharedTransitionScope.PlaceHolderSize.Companion.contentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.read5.bean.ComicPage
import com.example.read5.bean.PageLayout
import com.example.read5.bean.VirtualCanvas
import com.example.read5.singledata.DocumentHolder
import com.example.read5.viewmodel.comic.ComicViewModel

@Composable
fun VirtualComicCanvas(
    modifier: Modifier = Modifier
) {
    val TAG = "VirtualComicCanvas"
    val path = DocumentHolder.currentItem?.path
    val context = LocalContext.current
    val comicViewModel: ComicViewModel = hiltViewModel()
    val virtualCanvas by comicViewModel.virtualCanvas.collectAsState()
    val pageCache by comicViewModel.pageCache.collectAsState()

//    上下偏移
    var offsetY by remember { mutableStateOf(0f) }
//    缩放
    var scale by remember { mutableStateOf(1f) }

    LaunchedEffect(path) {
        if (path != null) {
            comicViewModel.initLoader(context, path)
        }
    }

    if (virtualCanvas == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val canvas = virtualCanvas!!
    // ✅ 正确方式：用手势控制 offsetY
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    // 1. 更新缩放
                    scale = (scale * zoom).coerceIn(0.5f, 5f)
                    offsetY = (offsetY + (pan.y / scale)).coerceAtLeast(
                        (-(canvas.totalHeight - size.height)).toFloat() // 限制不能拉过底部
                    ).coerceAtMost(0f) // 不能拉过顶部
                }

            }

    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 计算可见区域（Y 范围）
            val visibleTop = -offsetY.toInt()
            val visibleBottom = visibleTop + size.height.toInt()

            // 筛选可见页面
            val visiblePages = canvas.pageLayouts.filter { page ->
                page.bottom > visibleTop && page.top < visibleBottom
            }

            // 触发未加载页面的加载
            visiblePages.forEach { page ->
                if (pageCache[page.index] == null) {
                    // 启动加载（ViewModel 内部处理协程）
                    comicViewModel.loadPage(page.index)
                }
            }

            // 绘制每一页
            visiblePages.forEach { page ->
                pageCache[page.index]?.let { bitmap ->
                    // 计算缩放后的宽度和高度
                    val scaledWidth = (page.width * scale).toInt()
                    val scaledHeight = (page.height * scale).toInt()

                    // 横向居中
                    val drawX = ((size.width - scaledWidth) / 2f).toInt()
                    // 纵向位置也需要根据缩放进行调整，这里假设 offsetY 已经考虑了缩放因素
                    val drawY = ((page.top + offsetY) * scale).toInt()

                    drawImage(
                        image = bitmap,
                        dstOffset = IntOffset(drawX, drawY),
                        dstSize = IntSize(scaledWidth, scaledHeight)
                    )
                }
            }
        }
    }
}