package com.example.read5.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.example.read5.bean.ItemInfo
import com.example.read5.utils.ComicCoverExtractor
import com.example.read5.utils.EpubCoverExtractor
import com.example.read5.utils.PdfCoverGenerator

@Composable
fun ItemInfoScreen(
    item: ItemInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // 检查封面是否已存在（PDF 或 EPUB）
    var isCoverReady by remember {
        mutableStateOf(
            PdfCoverGenerator.hasCover(context, item.hash) // PDF 封面
            // 注意：EPUB 封面也存为同名文件，所以 hasCover 能通用
        )
    }

    // 🔑 按需生成封面（PDF 或 EPUB）
    LaunchedEffect(item.hash) {
        if (isCoverReady) return@LaunchedEffect

        when (item.fileType.lowercase()) {
            "pdf" -> {
                val success = PdfCoverGenerator.generatePdfCover(context, item.path, item.hash)
                if (success) isCoverReady = true
            }
            "epub" -> {
                // 提取 EPUB 封面
                val bitmap = EpubCoverExtractor.extractCover(item.path)
                if (bitmap != null) {
                    // 保存为与 PDF 相同的格式（WebP/PNG）
                    val saved = PdfCoverGenerator.saveCoverBitmap(context, item.hash, bitmap)
                    bitmap.recycle()
                    if (saved) isCoverReady = true
                }
            }
            "folder", "zip", "cbz", "comic" -> {
                val bitmap = ComicCoverExtractor.extractCover(context, item.path)
                if (bitmap != null) {
                    val saved = PdfCoverGenerator.saveCoverBitmap(context, item.hash, bitmap)
                    bitmap.recycle()
                    if (saved) isCoverReady = true
                }
            }
            // 可扩展 azw3 等
        }
    }

    Column(
        modifier = modifier
            .width(96.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(0.7f)
                .clip(RoundedCornerShape(8.dp))
                .fillMaxWidth()
        ) {
            if (isCoverReady) {
                // ✅ 加载已生成的封面（PDF/EPUB 共用）
                val coverPath = PdfCoverGenerator.getCoverFile(context, item.hash).absolutePath
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
            if (item.isCollect) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Collected",
                    tint = Color.Yellow,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 4.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(2.dp)
                )
            }
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
            val progressText = if (item.totalPage > 0) {
                "${item.currentPage}/${item.totalPage}"
            } else {
                "${item.schedule}%"
            }
            Text(
                text = progressText,
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// === 封面占位符（保持不变）===
@Composable
private fun CoverPlaceholder(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(getColorFromTitle(title)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title.firstOrNull()?.toString() ?: "?",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun getColorFromTitle(title: String): Color {
    val hash = title.hashCode()
    val r = (hash and 0xFF).toFloat() / 255f
    val g = ((hash ushr 8) and 0xFF).toFloat() / 255f
    val b = ((hash ushr 16) and 0xFF).toFloat() / 255f
    return Color(r, g, b)
}
@Composable
fun CenteredText(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text)
    }
}
