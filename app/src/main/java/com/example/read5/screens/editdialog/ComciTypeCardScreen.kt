package com.example.read5.screens.editdialog

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.read5.bean.ComicType
import com.example.read5.screens.CenteredText
import com.example.read5.viewmodel.CoverExtractorViewModel
import java.io.File

@Composable
fun ComciTypeCardScreen(
    comicType: ComicType,
    isSelected: Boolean,
    onSelectedChange: (Boolean) -> Unit
) {
    val TAG = "ComicTypeDebug"
    val coverExtractorViewModel: CoverExtractorViewModel = hiltViewModel()

    Log.d(TAG, "Card rendering - ${comicType.name}, isSelected: $isSelected")

    // 检查封面是否存在
    var isCoverReady by remember(comicType.cover) {
        mutableStateOf(
            if (comicType.cover.isNotEmpty()) {
                coverExtractorViewModel.hasCover(comicType.cover)
            } else {
                false
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                Log.d(TAG, "Card clicked - ${comicType.name}, current isSelected: $isSelected")
                Log.d(TAG, "Calling onSelectedChange with: ${!isSelected}")
                onSelectedChange(!isSelected)
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        } else null,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ===== 封面图片区域 =====
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                if (isCoverReady && comicType.cover.isNotEmpty()) {
                    // ✅ 有封面：加载图片
                    val coverFile = coverExtractorViewModel.getCoverFile(comicType.cover)
                    if (coverFile.exists()) {
                        AsyncImage(
                            model = "file://${coverFile.absolutePath}",
                            contentDescription = comicType.name,
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
                                text = comicType.name.take(2).uppercase(),
                                fontSize = 24.sp,
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
                            text = comicType.name.take(2).uppercase(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // ✅ 选中状态的覆盖层
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ===== 分类名称 =====
            Text(
                text = comicType.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // ===== 计数显示 =====
            if (comicType.count > 0) {
                Badge(
                    modifier = Modifier.padding(top = 2.dp),
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                ) {
                    Text(
                        text = comicType.count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}