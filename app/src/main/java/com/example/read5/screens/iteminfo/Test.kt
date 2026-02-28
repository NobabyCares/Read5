package com.example.read5.screens.iteminfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.read5.bean.ItemInfo
import com.example.read5.db.AppDatabase
import com.example.read5.viewmodel.iteminfo.BookShelfOfItemInfoViewModel


/**
 * 🧪 绝对纯净测试屏
 * 1. 直接从 DAO 读 List
 * 2. 无 ViewModel，无 Paging
 * 3. UI 完全静态
 */
@Composable
fun PureDebugScreen() {


    val  bookShelfOfItemInfoViewModel: BookShelfOfItemInfoViewModel = hiltViewModel()
    // 1. 直接协程加载数据 (假设只取前 100 条测试)
    val pagingItems = bookShelfOfItemInfoViewModel.items.collectAsLazyPagingItems()



    // 2. 纯静态列表
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 96.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        // 加上 modifier 防止测量问题
        modifier = Modifier.fillMaxSize()
    ) {
        // ✅ 关键：直接遍历 pagingItems，但当作普通 List 用
        // 我们手动构建一个临时的 List 用于 items 扩展函数 (如果必须用 items(List))
        // 或者直接用 items(count) 配合 get

        // 方案 A: 使用 items(count) - 最接近底层，性能最好
        items(
            count = pagingItems.itemCount,
            key = { index ->
                val item = pagingItems[index]
                // 确保 Key 稳定：有 ID 用 ID，没数据用唯一负数
                item?.id ?: -(index + 1).toLong()
            }
        ) { index ->
            val item = pagingItems[index]
            if (item != null) {
                ItemInfoScreen(item, onToView = {})
            }
        }
    }
}
