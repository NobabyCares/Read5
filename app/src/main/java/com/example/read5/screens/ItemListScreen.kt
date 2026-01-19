package com.example.read5.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.read5.viewmodel.ItemInfoViewModel
@Composable
fun ItemListScreen(viewModel: ItemInfoViewModel = hiltViewModel()) {
    val pagingFlow by viewModel.pagedItems.collectAsState()
    val pagingItems = pagingFlow.collectAsLazyPagingItems()

    Column { // 👈 用 Column 包裹 LazyColumn + 按钮，否则按钮不会显示！
        LazyColumn(
            modifier = Modifier.weight(1f) // 占据剩余空间
        ) {
            // ✅ 正确遍历方式：使用 itemCount + 索引
            items(count = pagingItems.itemCount) { index ->
                val item = pagingItems[index]
                if (item != null) {
                    Text("${item.name} - Category: ${item.category}")
                }
            }

            // 加载更多指示器
            if (pagingItems.loadState.append is LoadState.Loading) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // 分类切换按钮（放在列表下方）
        Row(
            modifier = Modifier.padding(8.dp)
        ) {
            Button(onClick = { viewModel.setCategory(null) }) {
                Text("全部")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.setCategory(1L) }) {
                Text("分类1")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.setCategory(7L) }) {
                Text("分类2")
            }
        }
    }
}