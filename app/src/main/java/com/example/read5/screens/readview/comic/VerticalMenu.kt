package com.example.read5.screens.readview.comic

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.read5.global.GlobalSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerticalMenu(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    readingProgress: Float,
    onProgressChanged: (Float) -> Unit = {},
    onOffsetYChange: () -> Unit = {},
) {
    val TAG = "VerticalMenu"


    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                Color.Black.copy(alpha = 0.85f),
                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
            // ✅ 添加日志查看触摸事件
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        Log.d(TAG, "🖱️ 拖动中: dragAmount=$dragAmount")
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 顶部进度条
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "进度",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    modifier = Modifier.width(40.dp)
                )

                // ✅ 为Slider添加单独的触摸检测
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    SimpleProgressBar(
                        progress = readingProgress,
                        onProgressChange = {
                            Log.d(TAG, "Slider进度改变: $it")
                            onProgressChanged(it)
                        }
                    )
                }


            }

            Spacer(modifier = Modifier.height(8.dp))

            // 底部功能按钮行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 3. 横屏阅读按钮
                Button(
                    onClick = {

                        onOffsetYChange()

                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .height(36.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "横屏阅读",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}