package com.example.read5.screens.readview


import android.graphics.Color
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.read5.R
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import kotlinx.coroutines.delay
import java.io.File



@Composable
fun PdfView(
    path: String,
    modifier: Modifier = Modifier,
) {

    val file = File("/storage/emulated/0/AliYunPan/备份文件/A-书籍/2024/未备份2024-7-20/LeetCode 101：和你一起你轻松刷题（C++） (高畅) (Z-Library).pdf")
    Log.d("PdfView", "File exists: ${file.exists()}")
    Log.d("PdfView", "Can read: ${file.canRead()}")
    Log.d("PdfView", "Absolute path: ${file.absolutePath}")

    var totalPages by remember { mutableStateOf(0) }
    var currentPage by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isNightMode by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. PDF 主视图
        AndroidView(
            factory = { context ->
                PDFView(context, null).apply {
                    // 禁用父View的滑动冲突（如果嵌入ScrollView等）
                    isSwipeEnabled = true
                }
            },
            update = { pdfView ->
                // 每次重组时重新配置（防止重复加载）
                pdfView.apply {
                    // 加载 PDF 配置链
                    fromFile(File(path))
                        .enableSwipe(true) // 允许滑动翻页
                        .enableDoubletap(true) // 双击缩放
                        .swipeHorizontal(false) // 垂直翻页（true为水平翻页）
                        .enableAnnotationRendering(true) // 渲染批注
                        .enableAntialiasing(true) // 抗锯齿
                        .spacing(10) // 页面间距（单位：dp）
                        .autoSpacing(true) // 自动间距
                        .pageFitPolicy(FitPolicy.WIDTH) // 页面适配策略：宽度适配
                        .pageSnap(true) // 页面对齐
                        .pageFling(true) // 快速翻页
                        .nightMode(isNightMode) // 夜间模式
                        .scrollHandle(DefaultScrollHandle(pdfView.context)) // 添加滚动条手柄
                        .onLoad { nbPages ->
                            totalPages = nbPages
                            isLoading = false
                        }
                        .onPageChange { page, _ ->
                            currentPage = page + 1 // 库的页码从0开始，我们显示从1开始
                        }
                        .onPageError { page, t ->
                            errorMessage = "加载第${page + 1}页时出错: ${t.message}"
                            isLoading = false
                        }
                        .load()
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. 顶部页面指示器（仅在非全屏时显示）
        if (!isLoading && errorMessage == null) {
            PageIndicator(
                currentPage = currentPage,
                totalPages = totalPages,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            )
        }

        // 3. 底部控制栏
        if (!isLoading && errorMessage == null) {
            BottomControlBar(
                onZoomIn = { /* 可扩展：获取PDFView实例并调用 zoomIn() */ },
                onZoomOut = { /* 可扩展：获取PDFView实例并调用 zoomOut() */ },
                onToggleNightMode = { isNightMode = !isNightMode },
                onJumpToPage = { page ->
                    // 跳转到指定页面（注意：库的页码从0开始）
                    // 需要获取PDFView实例，这里需要稍复杂的实现
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // 4. 加载中遮罩
        if (isLoading) {
            LoadingOverlay(modifier = Modifier.matchParentSize())
        }

        // 5. 错误提示遮罩
        errorMessage?.let { message ->
            ErrorOverlay(
                message = message,
                onRetry = { /* 可以在这里实现重试逻辑 */ },
                onDismiss = { errorMessage = null },
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

// ------------------ 以下是辅助组件 ------------------

/**
 * 页面指示器组件
 */
@Composable
private fun PageIndicator(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ComposeColor.Black.copy(alpha = 0.6f),
        modifier = modifier
    ) {
        Text(
            text = "$currentPage / $totalPages",
            color = ComposeColor.White,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
/**
 * 底部控制栏组件（使用内置图标）
 */
@Composable
private fun BottomControlBar(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onToggleNightMode: () -> Unit,
    onJumpToPage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        color = ComposeColor.Black.copy(alpha = 0.7f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            // 缩小按钮 - 使用内置的 `Remove` 图标
            IconButton(onClick = onZoomOut) {
                Icon(
                    imageVector = Icons.Default.Home, // 替换为内置图标
                    contentDescription = "缩小",
                    tint = ComposeColor.White
                )
            }

            // 夜间模式切换 - 使用内置的 `DarkMode` 图标
            IconButton(onClick = onToggleNightMode) {
                Icon(
                    imageVector = Icons.Default.Home, // 替换为内置图标
                    contentDescription = "夜间模式",
                    tint = ComposeColor.White
                )
            }

            // 放大按钮 - 使用内置的 `Add` 图标
            IconButton(onClick = onZoomIn) {
                Icon(
                    imageVector = Icons.Default.Home, // 替换为内置图标
                    contentDescription = "放大",
                    tint = ComposeColor.White
                )
            }
        }
    }
}

/**
 * 加载遮罩组件
 */
@Composable
private fun LoadingOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(ComposeColor.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = ComposeColor.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text("加载中...", color = ComposeColor.White)
        }
    }
}


@Composable
private fun ErrorOverlay(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(ComposeColor.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                // 错误图标 - 使用内置的 `Error` 图标
                Icon(
                    imageVector = Icons.Default.Home, // 替换为内置图标
                    contentDescription = "错误",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("忽略")
                    }
                    Button(onClick = onRetry) {
                        Text("重试")
                    }
                }
            }
        }
    }
}