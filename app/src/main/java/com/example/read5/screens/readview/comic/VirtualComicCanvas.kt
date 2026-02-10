package com.example.read5.screens.readview.comic

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.read5.bean.ComicPage
import com.example.read5.bean.ItemInfo
import com.example.read5.bean.ItemKey
import com.example.read5.bean.PageLayout
import com.example.read5.bean.VirtualCanvas
import com.example.read5.global.GlobalSettings
import com.example.read5.singledata.DocumentHolder
import com.example.read5.utils.comic.GestureUitils
import com.example.read5.utils.comic.GestureUitils.calculatePan
import com.example.read5.utils.comic.GestureUitils.calculateZoom
import com.example.read5.viewmodel.comic.ComicViewModel
import com.example.read5.viewmodel.iteminfo.UpdateItemInfo
import kotlinx.coroutines.delay
import kotlin.math.hypot
@Composable
fun VirtualComicCanvas(navController: NavHostController) {
    val itemInfo = DocumentHolder.requireItem()

    // 关键：用 key 保证切换漫画时状态重置
    key(itemInfo.path) {
        VirtualComicCanvasContent(navController, itemInfo)
    }
}

@Composable
private fun VirtualComicCanvasContent(
    navController: NavHostController,
    itemInfo: ItemInfo
) {
    val context = LocalContext.current
    val comicViewModel: ComicViewModel = hiltViewModel()

    var virtualCanvas by remember { mutableStateOf<VirtualCanvas?>(null) }
    val pageCache by comicViewModel.pageCache.collectAsState()
    var offsetY by remember { mutableStateOf(itemInfo.currentPage.toFloat()) } // ✅ 负值！
    var scale by remember { mutableStateOf(GlobalSettings.getScale()) }
    var isMenuVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            virtualCanvas = comicViewModel.initLoader(context, itemInfo)
        } finally {
            isLoading = false
        }
    }

    if (isLoading || virtualCanvas == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val canvas = virtualCanvas!!

    // 保存缩放
    LaunchedEffect(scale) {
        delay(3000)
        GlobalSettings.setScale(scale)
    }

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(canvas) {
                while (true) {
                    awaitEachGesture {
                        var startTime: Long? = null
                        var totalPan = Offset.Zero
                        var isMultiFinger = false
                        val panSmoothing = 1.3f  // 平移平滑系数
                        val zoomSmoothing = 0.6f // 缩放平滑系数

                        // 用于记录当前 canvas height
                        var currentCanvasHeight by mutableStateOf(0)

                        do {
                            val event = awaitPointerEvent()
                            // 只有在首次按下时记录 startTime
                            if (startTime == null && event.changes.any { it.pressed }) {
                                startTime = System.currentTimeMillis()
                                if (event.changes.size > 1) isMultiFinger = true
                            }
                            // 👇 在这里更新 canvas height（每次事件都可能变化）
                            currentCanvasHeight = size.height

                            // 处理移动/缩放
                            when (event.changes.size) {
                                1 -> {
                                    val pan = event.changes[0].positionChange()
                                    totalPan += pan

                                    // 平滑处理平移
                                    val smoothedY = pan.y * panSmoothing

                                    // 调整平移距离（减小灵敏度）
                                    offsetY += smoothedY / scale

                                    // 防止画布超出边界
                                    val maxOffset = (canvas.totalHeight * scale - size.height).coerceAtLeast(0f)
                                    offsetY = offsetY.coerceIn(-maxOffset, 0f)
                                    // 👇 关键：通知 ViewModel
                                    comicViewModel.onViewportScrolled(offsetY, currentCanvasHeight)
                                }
                                2 -> {
                                    isMultiFinger = true
                                    val rawZoom = GestureUitils.calculateZoom(event.changes)

                                    // 平滑处理缩放
                                    val smoothedZoom = 1 + (rawZoom - 1) * zoomSmoothing
                                    scale = (scale * smoothedZoom).coerceIn(1f, 5f)

                                }
                            }
                        } while (event.changes.any { it.pressed })

                        // 手指全部抬起
                        if (startTime != null) {
                            val duration = System.currentTimeMillis() - startTime
                            val distance = totalPan.getDistance()

                            // 更严格的点击检测
                            val isTap = !isMultiFinger && distance <= 12f && duration <= 250
                            if (isTap) {
                                isMenuVisible = !isMenuVisible
                            }
                        }
                    }
                }
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val visibleTop = -offsetY.toInt()
            val visibleBottom = visibleTop + size.height.toInt()

            canvas.pageLayouts
                .filter { it.bottom > visibleTop && it.top < visibleBottom }
                .forEach { page ->
                    pageCache[page.index]?.let { bitmap ->
                        // 计算缩放后的宽度和高度
                        val scaledWidth = (page.width * scale).toInt()
                        val scaledHeight = (page.height * scale).toInt()

                        // 横向居中
                        val drawX = ((size.width - scaledWidth) / 2f).toInt()
                        // 纵向位置也需要根据缩放进行调整，这里假设 offsetY 已经考虑了缩放因素
                        //val drawY = (page.top * scale + offsetY).toInt() // ✅ 正确公式
                        val drawY = ((page.top + offsetY) * scale).toInt()


                        drawImage(
                            image = bitmap,
                            dstOffset = IntOffset(drawX, drawY),
                            dstSize = IntSize(scaledWidth, scaledHeight)
                        )
                    }
                }
        }

        if (isMenuVisible) {
            TopReadingMenu(
                onDismiss = { isMenuVisible = false },
                modifier = Modifier.align(Alignment.TopCenter)
            )
            ButtomReadingMenu(
                onDismiss = { isMenuVisible = false },
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}