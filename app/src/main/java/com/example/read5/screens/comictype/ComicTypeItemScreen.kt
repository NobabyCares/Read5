package com.example.read5.screens.comictype

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.read5.bean.ComicType
import com.example.read5.bean.ItemKey
import com.example.read5.screens.CenteredText
import com.example.read5.screens.editdialog.ManagerEditDialog
import com.example.read5.screens.iteminfo.CoverPlaceholder
import com.example.read5.viewmodel.CoverExtractorViewModel
import com.example.read5.viewmodel.iteminfo.UpdateItemInfo
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


//显示数据项和封面生成
@Composable
fun ComicTypeItemScreen(
    comicTypeItem: ComicType,
    onChangeComicType: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val TAG = "ComicTypeItemScreen"
    //封面生成
    val coverExtractorViewModel: CoverExtractorViewModel = hiltViewModel()
    val itemInfoViewModel: UpdateItemInfo = hiltViewModel()


    var isShowChangeComicTypeDialog by remember { mutableStateOf(false) }



    // 检查封面是否已存在（PDF 或 EPUB）
    var isCoverReady by remember {
        mutableStateOf(
            // 注意：EPUB 封面也存为同名文件，所以 hasCover 能通用
            coverExtractorViewModel.hasCover(comicTypeItem.cover) // PDF 封面
        )
    }


    Column(
        modifier = modifier
            .width(96.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onChangeComicType()
                        Log.d(TAG, "短按触发")
                    },
                    onLongPress = {
                        isShowChangeComicTypeDialog = true
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
                val coverPath = coverExtractorViewModel.getCoverFile(comicTypeItem.cover).absolutePath
                AsyncImage(
                    model = "file://$coverPath",
                    contentDescription = "Book cover",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // ❌ 显示占位符
                CoverPlaceholder(comicTypeItem.name)
            }
        }

        // ===== 书名、作者等（保持不变）=====
        Text(
            text = comicTypeItem.name,
            modifier = Modifier.padding(top = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // ===== 书名、作者等（保持不变）=====
        Text(
            text = comicTypeItem.count.toString(),
            modifier = Modifier.padding(top = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }

    if (isShowChangeComicTypeDialog) {
        ChangeComicTypeScreen(
            comicType = comicTypeItem,
            onDismiss = { isShowChangeComicTypeDialog = false }
        )
    }


}
