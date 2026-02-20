package com.example.read5.screens.readview.comic

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.read5.global.GlobalSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizontalMenu(
    navController: NavHostController,
    backgroundColor: Color,
    readingProgress: Float,
    panSmoothing: Float,
    onProgressChanged: (Float) -> Unit = {},
    onPanSmoothing: (Float) -> Unit = {},
    onBackgroundColorChanged: (Color) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val TAG = "HorizontalMenu"

    // 预设背景色选项
    val backgroundOptions = listOf(
        Color.Black to "黑",
        Color(0xFF121212) to "深灰",
        Color(0xFFF5F5F5) to "浅灰",
        Color.White to "白"
    )

    var isBgMenuExpanded by remember { mutableStateOf(false) }
    var isSmoothMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
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

                Slider(
                    value = readingProgress.coerceIn(0f, 1f),
                    onValueChange = { newValue ->
                        Log.d(TAG, "Slider_change: $newValue")
                        onProgressChanged(newValue)
                    },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color(0xFF4FC3F7),
                        inactiveTrackColor = Color.Gray.copy(alpha = 0.4f)
                    ),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.5.dp, Color.Black.copy(alpha = 0.3f), CircleShape)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 底部功能按钮行（三等分）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. 背景选择按钮
                Box {
                    Row(
                        modifier = Modifier
                            .clickable { isBgMenuExpanded = true }
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Background",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "背景",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        // 当前背景色预览
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                .background(backgroundColor)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isBgMenuExpanded,
                        onDismissRequest = { isBgMenuExpanded = false },
                        modifier = Modifier.background(Color(0xFF2A2A2A))
                    ) {
                        backgroundOptions.forEach { (color, label) ->
                            DropdownMenuItem(
                                onClick = {
                                    onBackgroundColorChanged(color)
                                    GlobalSettings.setBackgroundColor(color)
                                    isBgMenuExpanded = false
                                    Log.d(TAG, "Background color changed to: $color")
                                },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                        )
                                        Text(
                                            text = label,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                        if (color == backgroundColor) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color(0xFF4FC3F7),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }


                // 3. 竖屏阅读按钮
                Button(
                    onClick = {
                        navController.navigate("vertical_comic_view") {
                            popUpTo("horizon_comic_view") {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                        GlobalSettings.setReadMode("vertical_comic_view")
                        Log.d(TAG, "Switching to vertical mode")
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .height(36.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "上下阅读",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}