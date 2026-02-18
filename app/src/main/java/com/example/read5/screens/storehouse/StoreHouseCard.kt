package com.example.read5.screens.storehouse

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.trace
import com.example.read5.bean.StoreHouse
import com.example.read5.utils.TimeUitls

@Composable
fun StoreHouseCard(
    storeHouse: StoreHouse,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val TAG = "StoreHouseCard"

    var isShowMoreInfoDialg by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier
            .width(120.dp)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onClick()
                        Log.d(TAG, "短按触发")
                    },
                    onLongPress = {
                        isShowMoreInfoDialg = true
                        Log.d(TAG, "长按触发")
                    },
                    onDoubleTap = {
                        // 可选：双击支持
                        Log.d(TAG, "双击触发")
                    }
                )
            }
    ) {
        // === 封面区域 ===
        Box(
            modifier = Modifier
                .aspectRatio(0.7f)
                .clip(RoundedCornerShape(12.dp))
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp)) // 微投影
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            getColorFromTitle(storeHouse.name).copy(alpha = 0.9f),
                            getColorFromTitle(storeHouse.name).copy(alpha = 0.6f)
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // 主图标 + 首字母
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Home, // 更贴合“书架”语义
                    contentDescription = "书架",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = storeHouse.name.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // 右上角装饰图标（可选：收藏/类型标识）
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
                    .size(16.dp)
            )
        }

        // === 书架名称 ===
        Text(
            text = storeHouse.name,
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.onSurface
        )

        // === 底部信息行 ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 书籍数量
            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${storeHouse.count} 本",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }

            // 类型标签（如果存在）
            if (storeHouse.type.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = storeHouse.type,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // === 最后更新时间 ===
        Text(
            text = TimeUitls.formatUpdateTime(storeHouse.lastUpdateTime), // ✅ 使用真实时间戳
            modifier = Modifier.padding(top = 4.dp),
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.outline,
            maxLines = 1
        )
    }

    if(isShowMoreInfoDialg){
        StoreHouseMoreInfoDialog(
            storeHouse = storeHouse,
            onDismiss = {
                isShowMoreInfoDialg = false
            }
        )
    }
}

// 颜色生成函数（保持不变）
fun getColorFromTitle(title: String): Color {
    val hash = title.hashCode()
    val r = (hash and 0xFF).toFloat() / 255f
    val g = ((hash ushr 8) and 0xFF).toFloat() / 255f
    val b = ((hash ushr 16) and 0xFF).toFloat() / 255f
    return Color(r, g, b).copy(alpha = 0.85f)
}