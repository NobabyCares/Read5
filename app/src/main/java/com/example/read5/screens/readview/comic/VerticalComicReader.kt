package com.example.read5.screens.readview.comic

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.read5.bean.ItemInfo
import com.example.read5.bean.VirtualCanvas
import com.example.read5.global.GlobalSettings
import com.example.read5.singledata.DocumentHolder
import com.example.read5.utils.comic.GestureUitils
import com.example.read5.utils.comic.PixelTranslationIndex
import com.example.read5.viewmodel.comic.ComicViewModel
import kotlinx.coroutines.delay

@Composable
fun VerticalComicReader(navController: NavHostController) {
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
    val TAG = "VirtualComicCanvasContent"
    val context = LocalContext.current
    val comicViewModel: ComicViewModel = hiltViewModel()

    //获取设置信息
    var backgroundColor by remember { mutableStateOf(GlobalSettings.getBackgroundColor()) }

    var virtualCanvas by remember { mutableStateOf<VirtualCanvas?>(null) }
    val pageCache by comicViewModel.pageCache.collectAsState()

    // ✅ 负值！
    var offsetY by remember { mutableFloatStateOf(itemInfo.currentPage.toFloat()) }
    var scale by remember { mutableFloatStateOf(GlobalSettings.getScale()) }
    var isMenuVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    // ✅ 新增：记录 Canvas 高度
    var currentCanvasHeight by remember { mutableIntStateOf(0) }
    // 平移平滑系数
    var panSmoothing by remember { mutableFloatStateOf(GlobalSettings.getPanSmoothing()) }

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
    LaunchedEffect(scale, panSmoothing) {
        delay(3000)
        GlobalSettings.setScale(scale)
        GlobalSettings.setSlidingSpeed(panSmoothing)
    }

    // ⚡️ 关键：使用 DisposableEffect 处理退出时的保存
    DisposableEffect(Unit) {
        // 这个块在组件首次加载时执行
        // 返回一个 onDispose 块，在组件销毁时执行
        onDispose {
            // 更新 ItemInfo
            val finalItemInfo = itemInfo.copy(
                currentPage = offsetY.toInt()
            )
            // 添加到历史记录
            GlobalSettings.addToHistory(finalItemInfo)
        }
    }

    // 使用 Box 作为最外层容器
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // ===== 区域1：画布区域（处理手势）=====
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    while (true) {
                        awaitEachGesture {
                            Log.d(TAG, "awaitEachGesture")
                            var startTime: Long? = null
                            var totalPan = Offset.Zero
                            var isMultiFinger = false

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
                                        val maxOffset =
                                            (canvas.totalHeight * scale - size.height).coerceAtLeast(
                                                0f
                                            )
                                        offsetY = offsetY.coerceIn(-maxOffset, 0f)
                                        // 👇 关键：通知 ViewModel
                                        comicViewModel.onViewportScrolled(
                                            offsetY,
                                            currentCanvasHeight
                                        )
                                    }

                                    2 -> {
                                        isMultiFinger = true
                                        val rawZoom = GestureUitils.calculateZoom(event.changes)

                                        // 平滑处理缩放
                                        scale = (scale * rawZoom).coerceIn(1f, 5f)
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
            // Canvas 绘制内容
            Canvas(Modifier.fillMaxSize()) {
                currentCanvasHeight = size.height.toInt() // ✅ 关键！
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
        // 页码指示器（始终显示）
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (isMenuVisible) 190.dp else 16.dp) // 菜单显示时上移到菜单上方
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${PixelTranslationIndex.searchByOffsetYPageIndex(offsetY.toInt(),canvas)} / ${canvas.pageLayouts.size}",
                color = Color.White,
                fontSize = 14.sp
            )
        }

        //菜单
        if (isMenuVisible) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)  // 整个 Column 对齐到底部
                    .background(Color.Black.copy(alpha = 0.85f))  // 添加背景色让菜单更明显
            ) {
                // ✅ 底部菜单：对齐屏幕底部
                VerticalMenu(
                    navController = navController,
                    readingProgress = (-offsetY) / canvas.totalHeight.toFloat(),
                    onProgressChanged = { newProgress ->
                        val targetOffsetY = -(canvas.totalHeight * newProgress.coerceIn(0f, 1f))
                        offsetY = targetOffsetY
                        comicViewModel.onViewportScrolled(offsetY, currentCanvasHeight)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                )

                // 第二行：PanSmoothScreen 和 BackGroundColorScreen 平分空间
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(Color.Black.copy(alpha = 0.85f)),
                    horizontalArrangement = Arrangement.SpaceEvenly  // 平分空间
                ) {
                    // PanSmoothScreen 占一半
                    Box(
                        modifier = Modifier.weight(1f)  // 权重为1，平分空间
                    ) {
                        PanSmoothScreen(
                            panSmoothing = panSmoothing,
                            onPanSmoothing = {
                                panSmoothing = it
                                Log.d("VerticalMenu", "Pan smoothing changed to: $it")
                            }
                        )
                    }

                    // 间距
                    Spacer(modifier = Modifier.width(16.dp))

                    // BackGroundColorScreen 占一半
                    Box(
                        modifier = Modifier.weight(1f)  // 权重为1，平分空间
                    ) {
                        BackGroundColorScreen(
                            backgroundColor = backgroundColor,
                            onBackgroundColorChanged = {
                                backgroundColor = it
                                GlobalSettings.setBackgroundColor(it)
                                Log.d("VerticalMenu", "Background color changed to: $it")
                            }
                        )
                    }

                }
            }
        }

    }
}

