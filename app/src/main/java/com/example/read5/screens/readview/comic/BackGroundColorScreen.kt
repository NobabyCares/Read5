package com.example.read5.screens.readview.comic

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
fun BackGroundColorScreen(
    backgroundColor: Color,
    onBackgroundColorChanged: (Color) -> Unit
    ) {
    val TAG = "BackGroundColorScreen"
    var isBgMenuExpanded by remember { mutableStateOf(false) }
    // 预设背景色选项
    val backgroundOptions = listOf(
        Color.Black to "黑",
        Color(0xFF121212) to "深灰",
        Color(0xFFF5F5F5) to "浅灰",
        Color.White to "白"
    )
    // 1. 背景选择按钮
    Box(
        modifier = Modifier
            .clickable {
                Log.d(TAG, "🎨 背景菜单打开")
                isBgMenuExpanded = true
            }
            .padding(horizontal = 12.dp, vertical = 6.dp)
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
                contentDescription = "Background",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "背景",
                color = Color.White,
                fontSize = 12.sp
            )
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
            onDismissRequest = {
                Log.d(TAG, "🎨 背景菜单关闭")
                isBgMenuExpanded = false
            },
            modifier = Modifier.background(Color(0xFF2A2A2A))
        ) {
            backgroundOptions.forEach { (color, label) ->
                DropdownMenuItem(
                    onClick = {
                        Log.d(TAG, "🎨 选择背景: $label")
                        onBackgroundColorChanged(color)
                        isBgMenuExpanded = false
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
}