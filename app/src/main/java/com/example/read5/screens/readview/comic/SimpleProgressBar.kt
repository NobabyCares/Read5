package com.example.read5.screens.readview.comic

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun SimpleProgressBar(
    progress: Float = 0f,
    onProgressChange: (Float) -> Unit = {},
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF2A2A2A),  // 深灰色背景，匹配菜单
    progressColor: Color = Color(0xFF4FC3F7),    // 亮蓝色进度
    thumbColor: Color = Color.White,              // 白色滑块
    height: androidx.compose.ui.unit.Dp = 10.dp,   // 加高进度条
    thumbSize: androidx.compose.ui.unit.Dp = 30.dp // 加大滑块
) {
    val TAG = "SimpleProgressBar"

    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(progress) }

    LaunchedEffect(progress) {
        if (!isDragging) {
            dragProgress = progress
        }
    }

    Box(
        modifier = modifier
            .height(height + thumbSize + 8.dp)  // 增加更多空间
            .fillMaxWidth()
            .padding(horizontal = 4.dp)  // 添加左右边距
            // 手势检测, 这里两段手势检测肯定有问题, 但暂时管不了了
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        dragProgress = newProgress
                        isDragging = true
                        onProgressChange(newProgress)
                        Log.d(TAG, "拖动开始: $newProgress")
                    },
                    onDrag = { change, _ ->
                        val newProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                        dragProgress = newProgress
                        onProgressChange(newProgress)
                        Log.d(TAG, "拖动中: $newProgress")
                    },
                    onDragEnd = {
                        isDragging = false
                        Log.d(TAG, "拖动结束")
                    },
                    onDragCancel = {
                        isDragging = false
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        dragProgress = newProgress
                        onProgressChange(newProgress)
                        Log.d(TAG, "点击跳转: $newProgress")
                    }
                )
            }
    ) {
        // 进度条背景（带圆角）
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .align(Alignment.Center)
        ) {
            // 背景条（圆角）
            drawRoundRect(
                color = backgroundColor,
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
            )

            // 进度条（圆角）
            drawRoundRect(
                color = progressColor,
                size = androidx.compose.ui.geometry.Size(
                    width = size.width * (if (isDragging) dragProgress else progress),
                    height = size.height
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
            )
        }

        // 滑块
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(thumbSize)
                .align(Alignment.Center)
        ) {
            val currentProgress = if (isDragging) dragProgress else progress
            val thumbCenterX = (size.width * currentProgress).coerceIn(
                thumbSize.toPx() / 2,
                size.width - thumbSize.toPx() / 2
            )

            // 滑块外圈（白色带阴影效果）
            drawCircle(
                color = thumbColor,
                radius = thumbSize.toPx() / 2,
                center = Offset(thumbCenterX, size.height / 2),
                style = Stroke(width = 2.dp.toPx())
            )

            // 滑块内圈（填充）
            drawCircle(
                color = progressColor,
                radius = thumbSize.toPx() / 3,
                center = Offset(thumbCenterX, size.height / 2)
            )

            // 如果正在拖动，添加一个光晕效果
            if (isDragging) {
                drawCircle(
                    color = progressColor.copy(alpha = 0.3f),
                    radius = thumbSize.toPx() * 0.8f,
                    center = Offset(thumbCenterX, size.height / 2)
                )
            }
        }
    }
}