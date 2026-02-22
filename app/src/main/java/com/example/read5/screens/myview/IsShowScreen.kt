    package com.example.read5.screens.myview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.read5.global.GlobalSettings
import com.example.read5.screens.iteminfo.ItemInfoScreen
import com.example.read5.screens.storehouse.StoreHouseCard
import com.example.read5.singledata.DocumentHolder
import com.example.read5.viewmodel.iteminfo.SearchItemInfo

@Composable
fun IsShowScreen(
    navHostController: NavHostController,
    searchItemInfo: SearchItemInfo
) {
    //这个是展示数据,可能是搜索数据,也可能是全部数据
    val itemInfos = searchItemInfo.items.collectAsLazyPagingItems()

    Column {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 96.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

                items(itemInfos.itemCount) { index ->
                    itemInfos[index]?.let { ItemInfoScreen(it, onToView = {
                        // ✅ 正确：触发导航，传递必要的参数
                        // ✅ 关键：对路径进行 URL 编码
                        // ✅ 关键：使用 Uri.encode()，不是 URLEncoder！
                        DocumentHolder.setCurrentItem(it)
                        GlobalSettings.addToHistory(it)
                        // ✅ 方式1：使用 navigate，确保正确进入栈
                        navHostController.navigate(GlobalSettings.getReadMode()) {
                            // 重要：不要 popUpTo，这样会保留返回栈
                            launchSingleTop = true
                        }
                    }) }
                }
            }

        }

}