package com.example.read5.screens.storehouse

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.read5.bean.StoreHouse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun StoreHouseCard(
    storeHouse: StoreHouse,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(120.dp)  // 比ItemInfoScreen稍宽一些
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // 封面区域
        Box(
            modifier = Modifier
                .aspectRatio(0.7f)  // 与ItemInfoScreen保持一致的比例
                .clip(RoundedCornerShape(12.dp))  // 圆角更大
                .fillMaxWidth()
                .background(
                    color = getColorFromTitle(storeHouse.name),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // 书架图标或首字母
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "书架",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = storeHouse.name.take(1),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 收藏状态
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "未收藏",
                tint =  Color.Gray,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-6).dp, y = 6.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(3.dp)
                    .size(18.dp)
            )
        }

        // 书架名称
        Text(
            text = storeHouse.name,
            modifier = Modifier.padding(top = 8.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )

        // 藏书信息
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 书籍数量标签
            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${storeHouse.name} 本",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }

            // 书架类型标签
            if (storeHouse.type.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = storeHouse.type,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }
            }
        }

        // 最后更新时间（如果有的话）
        if (storeHouse.name != null) {
            Text(
                text = formatUpdateTime(1),
                modifier = Modifier.padding(top = 2.dp),
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

// 从ItemInfoScreen复制过来的颜色生成函数
fun getColorFromTitle(title: String): Color {
    val hash = title.hashCode()
    val r = (hash and 0xFF).toFloat() / 255f
    val g = ((hash ushr 8) and 0xFF).toFloat() / 255f
    val b = ((hash ushr 16) and 0xFF).toFloat() / 255f
    return Color(r, g, b)
}

// 格式化时间
fun formatUpdateTime(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Date()
    val diff = now.time - timestamp

    return when {
        diff < 24 * 60 * 60 * 1000 -> "今天"
        diff < 2 * 24 * 60 * 60 * 1000 -> "昨天"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${(diff / (24 * 60 * 60 * 1000)).toInt()}天前"
        else -> SimpleDateFormat("MM/dd", Locale.getDefault()).format(date)
    }
}