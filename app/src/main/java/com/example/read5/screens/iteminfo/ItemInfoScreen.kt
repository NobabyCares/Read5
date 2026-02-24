package com.example.read5.screens.iteminfo

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.read5.bean.ItemInfo
import com.example.read5.bean.ItemKey
import com.example.read5.screens.editdialog.ManagerEditDialog
import com.example.read5.viewmodel.CoverExtractorViewModel
import com.example.read5.viewmodel.iteminfo.UpdateItemInfo
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

//显示数据项和封面生成
@Composable
fun ItemInfoScreen(
    item: ItemInfo,
    onToView: () -> Unit,
    modifier: Modifier = Modifier
) {
    val TAG = "ItemInfoScreen"
    //封面生成
    val coverExtractorViewModel: CoverExtractorViewModel = hiltViewModel()

    //    数据库跟新
    val updateItemInfoViewModel: UpdateItemInfo = hiltViewModel()

    val key: ItemKey = ItemKey(hash = item.hash, path = item.path, androidId = item.androidId)

    var isShowMoreInfoDialg by remember { mutableStateOf(true) }

    var isCollect by remember {
        mutableStateOf(item.isCollect)
    }

    // 检查封面是否已存在（PDF 或 EPUB）
    var isCoverReady by remember {
        mutableStateOf(
            // 注意：EPUB 封面也存为同名文件，所以 hasCover 能通用
            coverExtractorViewModel.hasCover(item.hash) // PDF 封面
        )
    }

    // 🔑 按需生成封面（PDF 或 EPUB）
    LaunchedEffect(item.hash) {
        if (isCoverReady) return@LaunchedEffect
        if(item.fileType.lowercase() in listOf("pdf", "epub", "folder", "zip")){
            coverExtractorViewModel.initCoverExtractor(item.fileType)
            if(coverExtractorViewModel.generateCover(item.path, item.hash)){
                isCoverReady = true
            }
        }
    }

        Column(
            modifier = modifier
                .width(96.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            Log.d(TAG, "短按触发")
                            onToView()
                        },
                        onLongPress = {
                            isShowMoreInfoDialg = false
                            Log.d(TAG, "长按触发")
                        },
                        onDoubleTap = {
                            // 可选：双击支持
                            Log.d(TAG, "双击触发")
                        }
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .aspectRatio(0.7f)
                    .clip(RoundedCornerShape(8.dp))
                    .fillMaxWidth()

            ) {
                if (isCoverReady) {
                    // ✅ 加载已生成的封面（PDF/EPUB 共用）
                    val coverPath = coverExtractorViewModel.getCoverFile(item.hash).absolutePath
                    AsyncImage(
                        model = "file://$coverPath",
                        contentDescription = "Book cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // ❌ 显示占位符
                    CoverPlaceholder(item.name)
                }

                // 收藏图标
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Collected",
                    tint = if (item.isCollect) Color.Red else Color.Gray,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 4.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(2.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color.White.copy(alpha = 0.3f))
                        ) {
                            isCollect = !isCollect
                            updateItemInfoViewModel.updateCollectStatus(key, isCollect)
                        }

                )
            }

            // ===== 书名、作者等（保持不变）=====
            Text(
                text = item.name,
                modifier = Modifier.padding(top = 4.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.lastReadTimeFormatted,
                modifier = Modifier.padding(top = 4.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = (item.schedule * -1).toString() + "%",
                modifier = Modifier.padding(top = 4.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "总时长 " + formatMillisecondsToHMS(item.totalReadTime),
                modifier = Modifier.padding(top = 4.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1, // 👈 建议改为 1 行（"1h 23m" 不需要两行）
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = formatFileSize(item.fileSize),
                modifier = Modifier.padding(top = 4.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = timestampToDateTime(item.createTime),
                modifier = Modifier.padding(top = 4.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (item.author.isNotEmpty()) {
                Text(
                    text = item.author,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0x80E0E0E0), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = item.fileType.uppercase(),
                        fontSize = 8.sp,
                        color = Color.Black
                    )
                }

            }
        }

    if(!isShowMoreInfoDialg){
       ManagerEditDialog(item) {
           isShowMoreInfoDialg = true
       }
    }
}

//转化文件大小
/**
 * 将字节大小转换为可读的MB/GB格式
 * @param bytes 字节数
 * @return 格式化后的字符串，如 "2.5 MB", "1.2 GB"
 */
fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 MB"

    val mb = bytes.toDouble() / (1024 * 1024)  // 转换为MB
    val gb = mb / 1024  // 转换为GB

    return when {
        gb >= 1 -> String.format("%.1f GB", gb)
        mb >= 1 -> String.format("%.1f MB", mb)
        else -> String.format("%.1f KB", bytes.toDouble() / 1024)
    }
}
//颜色标题
fun getColorFromTitle(title: String): Color {
    val hash = title.hashCode()
    val r = (hash and 0xFF).toFloat() / 255f
    val g = ((hash ushr 8) and 0xFF).toFloat() / 255f
    val b = ((hash ushr 16) and 0xFF).toFloat() / 255f
    return Color(r, g, b)
}
//转换时间
fun formatMillisecondsToHMS(milliseconds: Long): String {
    if (milliseconds <= 0) return "0秒"

    val totalSeconds = milliseconds / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return buildString {
        if (hours > 0) {
            append("${hours}小时")
        }
        if (minutes > 0) {
            append("${minutes}分")
        }
        if (seconds > 0 || (hours.toInt() == 0 && minutes.toInt() == 0)) {
            // 如果只有秒，或者全为0（但前面已处理 <=0），确保至少显示秒
            append("${seconds}秒")
        }
    }
}

@SuppressLint("NewApi")
fun timestampToDateTime(timestampMs: Long, zoneId: ZoneId = ZoneId.of("Asia/Shanghai")): String {
    val instant = Instant.ofEpochMilli(timestampMs)
    val zonedDateTime = instant.atZone(zoneId)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return zonedDateTime.format(formatter)
}