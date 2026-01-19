package com.example.read5.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.read5.viewmodel.ItemInfoViewModel
import com.example.read5.viewmodel.StoreHouseViewModel


// ——————— 书架页面 ———————
@Composable
fun BookShelfScreen(navController: NavHostController) {

    val storeHouseModel: StoreHouseViewModel = hiltViewModel()
    val itemInfoViewModel: ItemInfoViewModel = hiltViewModel()
    var showImportDialog by remember { mutableStateOf(false) }

    val storeHouses by storeHouseModel.storeHouses.collectAsState()


    val pagingFlow by itemInfoViewModel.pagedItems.collectAsState()
    val itemInfos = pagingFlow.collectAsLazyPagingItems()

    Column(modifier = Modifier.fillMaxSize()) {
        // 搜索栏
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            value = "",
            onValueChange = {},
            placeholder = { Text("一句顶一万句") },
            leadingIcon = { Icon(Icons.Filled.Home, contentDescription = "搜索占位") }, // ✅ Home
            trailingIcon = { Text("书城", fontWeight = FontWeight.Bold) },
            shape = RoundedCornerShape(24.dp)
        )
        Row {
            Text(
                text = "书城",
                modifier = Modifier.align(Alignment.CenterVertically),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "导入",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .clickable {
                        showImportDialog = true // ✅ 点击时显示弹窗
                    },
                fontWeight = FontWeight.Bold
            )
        }

//        仓库栏
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(storeHouses, key = { it.id }) { item ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            itemInfoViewModel.toggleType(item.id)
                            itemInfoViewModel.setCategory(item.id)
                        }
                        .padding(8.dp)
                        .wrapContentWidth()
                ) {
                    Text(
                        text = item.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // 只有当前项展开才显示 type，且限制高度
                    if (itemInfoViewModel.expandedStoreId.value == item.id) {
                        // 安全分割：处理 null、空字符串、多余空格
                        val tags = (item.type ?: "")
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }

                        if (tags.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                tags.forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = tag,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        // 图书网格
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 96.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(count =  itemInfos.itemCount) { index ->
                val item = itemInfos[index]
                if(item != null){
                    ItemInfoScreen(item) {
                    }
                }

            }
        }

        // 底部播放条
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("哲学性自杀", fontSize = 14.sp, maxLines = 1)
                        Text("20:02 / 42:38", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
                Row {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Home, contentDescription = "播放占位") // ✅ Home
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Home, contentDescription = "关闭占位") // ✅ Home
                    }
                }
            }
        }
    }
    // ✅ 关键修复：在 Column 外面（同级）添加弹窗！
    if (showImportDialog) {
        StoreHouseInputDialog(
            onDismiss = { showImportDialog = false },
        )
    }
}
