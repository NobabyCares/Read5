package com.example.read5.screens.readview.comic

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
@Composable
fun PanSmoothScreen(
    panSmoothing: Float,
    onPanSmoothing: (Float) -> Unit = {},
    modifier: Modifier = Modifier
){
    val TAG = "PanSmoothScreen"
    var isSmoothMenuExpanded by remember { mutableStateOf(false) }
    // 2. 平滑度选择按钮
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                Log.d(TAG, "⚡ 平滑菜单点击")
                isSmoothMenuExpanded = !isSmoothMenuExpanded
            }
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Smoothing",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "平滑",
                color = Color.White,
                fontSize = 12.sp
            )
            Text(
                text = "${(panSmoothing * 100).toInt()}%",
                color = Color(0xFF4FC3F7),
                fontSize = 12.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = isSmoothMenuExpanded,
            onDismissRequest = {
                Log.d(TAG, "⚡ 平滑菜单关闭")
                isSmoothMenuExpanded = false
            },
            modifier = Modifier.background(Color(0xFF2A2A2A))
        ) {
            DropdownMenuItem(
                onClick = { /* Keep expanded to allow sliding */ },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "平滑度调节",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Slider(
                            value = panSmoothing,
                            onValueChange = { newValue ->
                                Log.d(TAG, "⚡ 平滑滑块拖动: $newValue")
                                onPanSmoothing(newValue)
                            },
                            valueRange = 0.5f..3f,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF81D4FA),
                                activeTrackColor = Color(0xFF81D4FA),
                                inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
                            )
                        )
                        Text(
                            text = "${(panSmoothing * 100).toInt()}%",
                            color = Color(0xFF81D4FA),
                            fontSize = 14.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                },
                enabled = true
            )
        }
    }

}