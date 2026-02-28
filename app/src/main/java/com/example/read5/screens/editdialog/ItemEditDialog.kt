package com.example.read5.screens.editdialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.read5.bean.ItemInfo
import com.example.read5.viewmodel.CoverExtractorViewModel
import com.example.read5.viewmodel.iteminfo.UpdateItemInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEditDialog(
    item: ItemInfo,
    onDismiss: () -> Unit = {}
) {
    val TAG = "ItemEditDialog"
    val updateItemInfoViewModel = hiltViewModel<UpdateItemInfo>()
    val coverExtractorViewModel: CoverExtractorViewModel = hiltViewModel()

    // 检查封面是否存在
    var isCoverReady by remember(item.hash) {
        mutableStateOf(
            if (item.hash.isNotEmpty()) {
                coverExtractorViewModel.hasCover(item.hash)
            } else {
                false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // ===== 项目信息卡片（带封面）=====
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ===== 左侧封面图片 =====
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    if (isCoverReady && item.hash.isNotEmpty()) {
                        // ✅ 有封面：加载图片
                        val coverFile = coverExtractorViewModel.getCoverFile(item.hash)
                        if (coverFile.exists()) {
                            AsyncImage(
                                model = "file://${coverFile.absolutePath}",
                                contentDescription = item.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // 文件不存在，显示占位符
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.name.take(2).uppercase(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } else {
                        // ❌ 无封面：显示文字占位符
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.name.take(2).uppercase(),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // ===== 右侧信息 =====
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 书名
                    Text(
                        text = item.name ?: "未命名",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        fontWeight = FontWeight.Bold
                    )

                    // 作者
                    if (item.author.isNotEmpty()) {
                        Text(
                            text = "作者: ${item.author}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 文件类型
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 文件类型标签
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = item.fileType.uppercase(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        // 显示/隐藏状态标签
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = if (item.isShow) "已显示" else "已隐藏",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (item.isShow) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                } else {
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                }
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ===== 隐藏/显示按钮 =====
        Button(
            onClick = {
                val newIsShow = !item.isShow
                updateItemInfoViewModel.updateByIsShow(item.id, newIsShow)
                updateItemInfoViewModel.updateByCount(item.id, newIsShow)
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (item.isShow)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = if (item.isShow) "隐藏项目" else "取消隐藏",
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ===== 额外信息（可选）=====
        Text(
            text = "文件大小: ${formatFileSize(item.fileSize)}  |  总时长: ${formatMillisecondsToHMS(item.totalReadTime)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

// 辅助函数（如果不在当前文件，需要导入）
fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 MB"
    val mb = bytes.toDouble() / (1024 * 1024)
    val gb = mb / 1024
    return when {
        gb >= 1 -> String.format("%.1f GB", gb)
        mb >= 1 -> String.format("%.1f MB", mb)
        else -> String.format("%.1f KB", bytes.toDouble() / 1024)
    }
}

fun formatMillisecondsToHMS(milliseconds: Long): String {
    if (milliseconds <= 0) return "0秒"
    val totalSeconds = milliseconds / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return buildString {
        if (hours > 0) append("${hours}小时")
        if (minutes > 0) append("${minutes}分")
        if (seconds > 0 || (hours == 0L && minutes == 0L)) append("${seconds}秒")
    }
}