package com.example.read5.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.read5.viewmodel.ItemInfoViewModel
import com.example.read5.viewmodel.StoreHouseViewModel
import com.example.read5.singledata.DocumentHolder

// BookShelfScreen.kt
// ——————— 书架页面 ———————
@Composable
fun BookShelfScreen(navController: NavHostController) {

    val TAG: String = "BookShelfScreen"
    val storeHouseModel: StoreHouseViewModel = hiltViewModel()
    val itemInfoViewModel: ItemInfoViewModel = hiltViewModel()




    val storeHouses by storeHouseModel.storeHouses.collectAsState()


    val filteredItemFlow by itemInfoViewModel.filteredPagedItems.collectAsState()
    val filteritems = filteredItemFlow.collectAsLazyPagingItems()


    //    导入控制
    var showImportDialog by remember { mutableStateOf(false) }
//    搜索控制
    var searchQuery by remember { mutableStateOf("") }

    // 搜索结果（输入即搜 + 防抖）
    val searchResults = itemInfoViewModel.searchResults.collectAsLazyPagingItems()
    LaunchedEffect(searchQuery) {
        itemInfoViewModel.updateQuery(searchQuery)
    }

    // ✅ 新代码：根据是否在搜索，决定显示哪个列表
    val isSearching = searchQuery.isNotBlank()
    val displayItems = if (isSearching) searchResults else filteritems


    Column(modifier = Modifier.fillMaxSize()) {

        // ✅ 使用轻量 SearchBar，不占满屏幕
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            modifier = Modifier
        )

//导入栏
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

        StoreHouseSelector(storeHouses = storeHouses)

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 96.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(displayItems.itemCount) { index ->
                displayItems[index]?.let { ItemInfoScreen(it, onClick = {
                    // ✅ 正确：触发导航，传递必要的参数
                    // ✅ 关键：对路径进行 URL 编码
                    // ✅ 关键：使用 Uri.encode()，不是 URLEncoder！
                    DocumentHolder.setCurrentItem(it)
                    navController.navigate("pdf_view")
                }) }
            }
        }

        // 底部播放条
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            SoundScreen()
        }
    }

    // ✅ 关键修复：在 Column 外面（同级）添加弹窗！
    if (showImportDialog) {
        StoreHouseInputDialog(
            onDismiss = { showImportDialog = false },
        )
    }




}
