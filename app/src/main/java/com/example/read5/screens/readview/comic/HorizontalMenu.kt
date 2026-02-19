package com.example.read5.screens.readview.comic

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizontalMenu(
    navController: NavHostController,
    readingProgress: Float,
    panSmoothing: Float,
    onProgressChanged: (Float) -> Unit = {},
    onPanSmoothing: (Float) -> Unit = {},
    modifier: Modifier = Modifier,
) {

    val TAG = "VerticalMenu"


    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp) // 增加高度以适应新增的选项行
            .background(Color.Black.copy(alpha = 0.7f))
    ) {

        // 美化后的进度条
        Slider(
            value = readingProgress.coerceIn(0f, 1f),
            onValueChange = { newValue ->
                Log.d(TAG, "Slider_change: $newValue")
                onProgressChanged(newValue)
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(horizontal = 8.dp)
                .height(48.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF4FC3F7),
                inactiveTrackColor = Color.Gray.copy(alpha = 0.4f)
            ),
            thumb = {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, Color.Black.copy(alpha = 0.3f), CircleShape)
                        .wrapContentSize(align = Alignment.Center)
                )
            }
        )

        // ===== 新增：平滑度调节控件 =====
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Smooth",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp
            )

            // 小型平滑度 Slider
            Slider(
                value = panSmoothing, // 需要传入当前值
                onValueChange = { newValue ->
                    onPanSmoothing(newValue) // 👈 直接回调
                },
                modifier = Modifier
                    .width(100.dp) // 紧凑宽度
                    .height(32.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF81D4FA), // 浅蓝，区别于主进度条
                    activeTrackColor = Color(0xFF81D4FA),
                    inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
                ),
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF81D4FA))
                            .border(1.5.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                    )
                }
            )

            Button(onClick = {
                navController.navigate("vertical_comic_view")
            }) {
                Text(text = "上下阅读")
            }
        }
    }
}