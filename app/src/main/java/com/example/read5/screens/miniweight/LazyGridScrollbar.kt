package com.example.read5.screens.miniweight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/*
* 右边的滑动条
*
* */
@Composable
fun LazyGridScrollbar(
    gridState: LazyGridState,
    totalItems: Long,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val visibleItems = gridState.layoutInfo.visibleItemsInfo.size
    if (totalItems <= visibleItems) return

    val firstVisibleIndex = gridState.firstVisibleItemIndex
    val scrollProgress = firstVisibleIndex.toFloat() / (totalItems - visibleItems)

    // 小球大小
    val thumbSize = 24.dp

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(32.dp)  // 更宽的点击区域
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        val newProgress = (offset.y / size.height).coerceIn(0f, 1f)
                        val targetIndex = ((totalItems - visibleItems) * newProgress).toInt()
                        coroutineScope.launch {
                            gridState.scrollToItem(targetIndex)
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, _ ->
                        val newProgress = (change.position.y / size.height).coerceIn(0f, 1f)
                        val targetIndex = ((totalItems - visibleItems) * newProgress).toInt()
                        coroutineScope.launch {
                            gridState.scrollToItem(targetIndex)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // 小球滑块
        Canvas(
            modifier = Modifier
                .fillMaxHeight()
                .width(thumbSize)
                .align(Alignment.Center)
        ) {
            val thumbCenterY = size.height * scrollProgress
            val thumbRadius = thumbSize.toPx() / 2

            // 限制小球不超出上下边界
            val boundedCenterY = thumbCenterY.coerceIn(
                thumbRadius,
                size.height - thumbRadius
            )

            // 阴影效果
            drawCircle(
                color = Color.Black.copy(alpha = 0.2f),
                radius = thumbRadius + 2f,
                center = Offset(x = size.width / 2 + 1f, y = boundedCenterY + 1f)
            )

            // 小球渐变
            val brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF4FC3F7),
                    Color(0xFF2196F3)
                )
            )

            drawCircle(
                brush = brush,
                radius = thumbRadius,
                center = Offset(x = size.width / 2, y = boundedCenterY)
            )

            // 高光
            drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                radius = thumbRadius * 0.25f,
                center = Offset(
                    x = size.width / 2 - thumbRadius * 0.2f,
                    y = boundedCenterY - thumbRadius * 0.2f
                )
            )
        }
    }
}