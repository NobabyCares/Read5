package com.example.read5.screens.readview.comic

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.read5.bean.ItemInfo
import com.example.read5.bean.VirtualCanvas
import com.example.read5.global.GlobalSettings
import com.example.read5.singledata.DocumentHolder
import com.example.read5.viewmodel.comic.ComicViewModel
import kotlinx.coroutines.delay

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
    val context = LocalContext.current
    val comicViewModel: ComicViewModel = hiltViewModel()

    var virtualCanvas by remember { mutableStateOf<VirtualCanvas?>(null) }
    val pageCache by comicViewModel.pageCache.collectAsState()
    var isLoading by remember { mutableStateOf(true) }

    // ✅ 复用原逻辑：从 currentPage 初始化（注意：原文件是 Float，这里转 Int）
    var currentPageIndex by remember {
        mutableIntStateOf(itemInfo.currentPage.coerceAtLeast(0))
    }

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

    val pagerState = rememberPagerState(
        initialPage = currentPageIndex.coerceIn(0, totalPages - 1),
        pageCount = { totalPages }
    )

    LaunchedEffect(pagerState.currentPage) {
        val current = pagerState.currentPage
        val range = (current - 2)..(current + 2) // 预加载前后 2 页
        range.forEach { index ->
            if (index in 0 until totalPages) {
                val realPageIndex = canvas.pageLayouts[index].index
                // 👇 主动触发加载（即使 pageCache 还没被访问）
                comicViewModel.onViewportSlide(
                    realPageIndex,
                    currentCanvasHeight = 2000
                )
            }
        }
    }




    LaunchedEffect(scale) {
        delay(3000)
        GlobalSettings.setScale(scale)
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // 双击切换缩放
                        scale = if (scale > 1.1f) 1f else 2f
                    },
                    onTap = {
                    }
                )
            }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { index -> canvas.pageLayouts[index].index }
        ) { pageIndex ->
            val bitmap = pageCache[canvas.pageLayouts[pageIndex].index]
            PageItem(bitmap = bitmap)
        }

        // 页码指示器
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(8.dp)
        ) {
            Text(
                text = "${pagerState.currentPage + 1} / $totalPages",
                color = Color.White
            )
        }
    }
}

@Composable
private fun PageItem(
    bitmap: ImageBitmap?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (bitmap == null) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
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
