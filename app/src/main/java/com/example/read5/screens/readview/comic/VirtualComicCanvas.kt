package com.example.read5.screens.readview.comic

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.SharedTransitionScope.PlaceHolderSize.Companion.contentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.read5.bean.ComicPage
import com.example.read5.bean.ItemKey
import com.example.read5.bean.PageLayout
import com.example.read5.bean.VirtualCanvas
import com.example.read5.global.GlobalSettings
import com.example.read5.singledata.DocumentHolder

import com.example.read5.viewmodel.comic.ComicViewModel
import com.example.read5.viewmodel.iteminfo.UpdateItemInfo
import kotlinx.coroutines.delay

@Composable
fun VirtualComicCanvas(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val TAG = "VirtualComicCanvas"
    val itemInfo = DocumentHolder.requireItem()
    val path = DocumentHolder.requireItem().path


//    key,用于数据库更新，因为数据库是联合主键
    val key = ItemKey(
        androidId = itemInfo.androidId,
        path = path ,
        hash = itemInfo.hash
    )
    val context = LocalContext.current


    val comicViewModel: ComicViewModel = hiltViewModel()
    val updateItemInfo: UpdateItemInfo = hiltViewModel()
//    虚拟画布生成
    val virtualCanvas by comicViewModel.virtualCanvas.collectAsState()
//    缓存
    val pageCache by comicViewModel.pageCache.collectAsState()

//    上下偏移
    var offsetY by remember { mutableStateOf(itemInfo.currentPage.toFloat()) }
//    缩放
    var scale by remember { mutableStateOf(GlobalSettings.getScale()) }
    // 添加：用于记录菜单是否可见的状态
    var menuVisible by remember { mutableStateOf(false) }



    // ✅ 修改 BackHandler 逻辑
    BackHandler {
        // 方法1: 检查是否能 pop
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
        } else {
            // 方法2: 直接导航到 bookshelf
            navController.navigate("bookshelf") {
                // 关键设置：清除所有页面
                popUpTo(0) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

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

    // ✅ 新增：防抖保存阅读进度
    LaunchedEffect(offsetY, key, scale) {
        if (path != null && virtualCanvas != null) {
            // 防抖：等待 1 秒无变化再保存
            delay(3000)
            // 再次检查是否还是同一个 path（避免旧任务覆盖新书）
            updateItemInfo.updateCurrentPage(currentPage = offsetY.toInt(), key = key)
            GlobalSettings.setScale(scale)
        }
    }

    val canvas = virtualCanvas!!

    // ✅ 正确方式：用手势控制 offsetY
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    // 1. 更新缩放
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    // 控制缩放范围
                    scale = scale.coerceIn(0.5f, 5f) // 根据需求调整最小最大值
                    // 调整平移距离
                    offsetY += pan.y / scale * 1.5f
                    // 防止画布超出边界
                    val maxOffset = (canvas.totalHeight * scale - size.height).coerceAtLeast(0f)
                    offsetY = offsetY.coerceIn(-maxOffset, 0f)
                }
            }
            .pointerInput(Unit) {
                // 单独监听点击（不会干扰上面的 transform）
                detectTapGestures(
                    onTap = {
                        menuVisible = !menuVisible
                    },
                    // 可选：长按也触发
                    onLongPress = {
                        menuVisible = true
                    }
                )
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

            visiblePages.forEach { page ->
                comicViewModel.loadPage(page.index)
            }

            Log.d(TAG, "visiblePages: ${pageCache.size}")
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
        // 菜单
        if (menuVisible) {
            TopReadingMenu(
                onDismiss = { menuVisible = false },
                modifier = Modifier.align(Alignment.TopCenter)
            )
            ButtomReadingMenu(
                onDismiss = { menuVisible = false },
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }

    }
}


