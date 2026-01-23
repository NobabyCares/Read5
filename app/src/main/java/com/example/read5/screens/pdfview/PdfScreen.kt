package com.example.read5.screens.pdfview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.read5.screens.ScreenshotButton
import com.example.read5.singledata.PdfDocumentHolder
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.shockwave.pdfium.PdfDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class Bookmark(
    val pageNumber: Int,
    val title: String,
    val time: Long = System.currentTimeMillis()
)


@Composable
fun PdfView(
    modifier: Modifier = Modifier,
    onPageChanged: (Int, Int) -> Unit = { _, _ -> },
    onLoadComplete: (Int) -> Unit = { },
    onError: (Throwable) -> Unit = { }
) {
    val showControls = true


    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<Throwable?>(null) }
    var totalPages by remember { mutableIntStateOf(0) }
    var currentPage by remember { mutableIntStateOf(0) }
    var showBookmarks by remember { mutableStateOf(false) }
    val pdfViewRef = remember { mutableStateOf<PDFView?>(null) }
    val bookmarks = remember { mutableStateListOf<Bookmark>() }

    val filePath = PdfDocumentHolder.currentItem
    if (filePath == null) {
        // 显示加载中或错误
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("文档路径无效")
        }
        return
    }

    // 显示加载状态
    if (isLoading) {
        IsLoading()
    }

    // 显示错误
    error?.let {
        ErrorScreen(error, click = {
                isLoading = true
                error = null
        })
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // 点击屏幕显示/隐藏控制面板
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.first().pressed) {
                        }
                    }
                }
            }
    ) {

        AndroidView(
            factory = { ctx ->
                val pdfView = PDFView(ctx, null)
                pdfViewRef.value = pdfView

                // 直接使用字符串路径
                filePath?.let { file ->
                    pdfView.fromFile(File(file.path))
                        .swipeHorizontal(false)
                        .enableDoubletap(true)
                        .enableSwipe(true)
                        .defaultPage(0)
                        .enableAnnotationRendering(true)
                        .spacing(10)
                        .autoSpacing(true)
                        .pageFitPolicy(com.github.barteksc.pdfviewer.util.FitPolicy.WIDTH)
                        .fitEachPage(true)
                        .pageSnap(true)
                        .pageFling(true)
                        .nightMode(false)
                        .onLoad { n ->
                            isLoading = false
                            totalPages = n
                            onLoadComplete(n)
                        }
                        .onPageChange { page, pageCount ->
                            currentPage = page
                            totalPages = pageCount
                            onPageChanged(page, pageCount)
                        }
                        .onPageError { page, t ->
                            error = t
                            onError(t)
                        }
                        .scrollHandle(DefaultScrollHandle(ctx))
                        .load()
                } ?: run {
                    // 如果 filePath 为 null，显示错误
                    pdfView
                }

                pdfView  // 返回 PDFView 实例
            },
            modifier = Modifier.fillMaxSize()
        )

        // 页面指示器
        if (!isLoading && totalPages > 0 && showControls) {
            PageIndicator(
                currentPage = currentPage + 1,
                totalPages = totalPages,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )
        }

        // 控制面板
        if (showControls && !isLoading && totalPages > 0) {
            ControlPanel(
                currentPage = currentPage,
                totalPages = totalPages,
                onPrevious = {
                    pdfViewRef.value?.let { pdfView ->
                        val prevPage = (currentPage - 1).coerceAtLeast(0)
                        pdfView.jumpTo(prevPage, true)
                    }
                },
                onNext = {
                    pdfViewRef.value?.let { pdfView ->
                        val nextPage = (currentPage + 1).coerceAtMost(totalPages - 1)
                        pdfView.jumpTo(nextPage, true)
                    }
                },
                onToggleBookmark = {
                    val existing = bookmarks.find { it.pageNumber == currentPage }
                    if (existing == null) {
                        bookmarks.add(Bookmark(
                            pageNumber = currentPage,
                            title = "第 ${currentPage + 1} 页"
                        ))
                    } else {
                        bookmarks.remove(existing)
                    }
                },
                onJumpToPage = { page ->
                    pdfViewRef.value?.let { pdfView ->
                        val targetPage = (page - 1).coerceIn(0, totalPages - 1)
                        pdfView.jumpTo(targetPage, true)
                    }
                },
                isBookmarked = bookmarks.any { it.pageNumber == currentPage },
                onShowBookmarks = { showBookmarks = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }

        // 书签列表
        if (showBookmarks) {
            BookmarkDialog(
                bookmarks = bookmarks,
                onDismiss = { showBookmarks = false },
                onBookmarkClick = { page ->
                    pdfViewRef.value?.let { pdfView ->
                        pdfView.jumpTo(page, true)
                    }
                    showBookmarks = false
                },
                onRemoveBookmark = { bookmark ->
                    bookmarks.remove(bookmark)
                }
            )
        }
    }
}





