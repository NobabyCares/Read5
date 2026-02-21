package com.example.read5.screens.readview.comic

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.read5.bean.ItemInfo
import com.example.read5.bean.VirtualCanvas
import com.example.read5.global.GlobalSettings
import com.example.read5.singledata.DocumentHolder
import com.example.read5.utils.comic.PixelTranslationIndex.searchByOffsetYPageIndex
import com.example.read5.viewmodel.comic.ComicViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HorizontalComicReader(navController: NavHostController) {
    val itemInfo = DocumentHolder.requireItem()
    val context = LocalContext.current
    key(itemInfo.path) {
        // 设置系统 UI 可见性为沉浸模式
        SideEffect {
            val window = (context as? Activity)?.window
            window?.decorView?.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
        HorizontalComicCanvasContent(navController, itemInfo)
    }
}

@Composable
private fun HorizontalComicCanvasContent(
    navController: NavHostController,
    itemInfo: ItemInfo
) {
    val TAG = "HorizontalComicCanvasContent"
    val context = LocalContext.current
    val comicViewModel: ComicViewModel = hiltViewModel()

    //获取背景颜色
    var backgroundColor by remember { mutableStateOf(GlobalSettings.getBackgroundColor()) }
    // 👈 用于启动协程
    val scope = rememberCoroutineScope()

    var virtualCanvas by remember { mutableStateOf<VirtualCanvas?>(null) }
    val pageCache by comicViewModel.pageCache.collectAsState()

    var isLoading by remember { mutableStateOf(true) }
    //数据库存储的数据
    var currentPage by remember { mutableIntStateOf(0) }
    var isMenuVisible by remember { mutableStateOf(false) }
    var panSmoothing by remember { mutableFloatStateOf(1f) }

    // ✅ 缩放状态：和原文件一致
    var scale by remember { mutableFloatStateOf(GlobalSettings.getScale()) }



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
    val totalPages = canvas.pageLayouts.size
    // 将保存的 offsetY（Int）转换为页码 index
    val initialPageIndex = searchByOffsetYPageIndex(itemInfo.currentPage, canvas)

    val currentPageIndex by remember {
        mutableIntStateOf(initialPageIndex)
    }

    val pagerState = rememberPagerState(
        initialPage = currentPageIndex.coerceIn(0, totalPages - 1),
        pageCount = { totalPages }
    )

    LaunchedEffect(pagerState.currentPage) {
        val current = pagerState.currentPage
        //这里一定要存储负数,因为在VirtualComicCanvas存储的是偏移量, 而偏移量始终为负数, 所以这里要取负数, 同意数据
        currentPage = -1 * canvas.pageLayouts[current].top
        val range = (current - 2)..(current + 2) // 预加载前后 2 页
        range.forEach { index ->
            if (index in 0 until totalPages) {
                // 👇 主动触发加载（即使 pageCache 还没被访问）
                comicViewModel.onViewportSlide(index)
            }
        }
        comicViewModel.updateCurrentPage(currentPage)
    }

    LaunchedEffect(scale) {
        delay(3000)
        GlobalSettings.setScale(scale)
    }

    // ⚡️ 关键：使用 DisposableEffect 处理退出时的保存
    DisposableEffect(Unit) {
        // 这个块在组件首次加载时执行
        // 返回一个 onDispose 块，在组件销毁时执行
        onDispose {
            // 更新 ItemInfo
            val finalItemInfo = itemInfo.copy(
                currentPage = currentPage
            )
            // 添加到历史记录
            GlobalSettings.addToHistory(finalItemInfo)
        }
    }

    // 使用 Box 作为最外层容器，分离手势区域
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
                    var startTime: Long? = null
                    var isMultiFinger = false

                    detectTapGestures(
                        onDoubleTap = {
                            // 双击切换缩放
                            scale = if (scale > 1.1f) 1f else 2f
                            Log.d(TAG, "Double tap detected, scale: $scale")
                        },
                        onTap = {
                            // 单点击切换菜单可见性
                            val duration =
                                if (startTime != null) System.currentTimeMillis() - startTime else 0L
                            if (!isMultiFinger && duration <= 250) {
                                isMenuVisible = !isMenuVisible
                                Log.d(TAG, "Tap detected, menu visible: $isMenuVisible")
                            }
                        },
                        onLongPress = {
                            Log.d(TAG, "Long press detected")
                        }
                    )
                }
        ) {
            // HorizontalPager 内容
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                key = { index -> canvas.pageLayouts[index].index }
            ) { pageIndex ->
                val bitmap = pageCache[canvas.pageLayouts[pageIndex].index]
                PageItem(bitmap = bitmap, backgroundColor = backgroundColor)
            }
        }

        // ===== 区域2：菜单区域（单独一层，消费事件）=====
        if (isMenuVisible) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.BottomCenter)  // 整个 Column 对齐到底部
                    .background(Color.Black.copy(alpha = 0.85f))  // 添加背景色让菜单更明显
            ) {
                HorizontalMenu(
                    navController = navController,
                    readingProgress = (pagerState.currentPage.toFloat() / totalPages),
                    onProgressChanged = {
                        val targetPage = (it * totalPages).toInt()
                        scope.launch {
                            pagerState.animateScrollToPage(targetPage)
                        }
                    }
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

        // 页码指示器（始终显示）
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (isMenuVisible) 190.dp else 8.dp) // 菜单显示时上移
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${pagerState.currentPage + 1} / $totalPages",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun PageItem(
    bitmap: ImageBitmap?,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        if (bitmap == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // 计算目标尺寸：让图片完整显示在屏幕内（不裁剪）
                val imageRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val canvasRatio = size.width / size.height

                val (dstWidth, dstHeight) = if (imageRatio > canvasRatio) {
                    // 图片更“宽” → 以宽度为准（Fit Width）
                    size.width to (size.width / imageRatio)
                } else {
                    // 图片更“高” → 以高度为准（Fit Height）
                    (size.height * imageRatio) to size.height
                }

                val drawX = (size.width - dstWidth) / 2f
                val drawY = (size.height - dstHeight) / 2f

                drawImage(
                    image = bitmap,
                    dstOffset = IntOffset(drawX.toInt(), drawY.toInt()),
                    dstSize = IntSize(dstWidth.toInt(), dstHeight.toInt())
                )
            }
        }
    }
}
