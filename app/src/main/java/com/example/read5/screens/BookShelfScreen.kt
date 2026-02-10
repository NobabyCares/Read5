package com.example.read5.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.read5.global.GlobalSettings
import com.example.read5.screens.iteminfo.ItemInfoScreen
import com.example.read5.screens.storehouse.StoreHouseCard
import com.example.read5.screens.storehouse.StoreHouseInputDialog
import com.example.read5.viewmodel.storehouse.StoreHouseViewModel
import com.example.read5.singledata.DocumentHolder
import com.example.read5.viewmodel.iteminfo.SearchItemInfo

// BookShelfScreen.kt
// ——————— 书架页面 ———————
@Composable
fun BookShelfScreen(
    navController: NavHostController,
    storeHouseModel: StoreHouseViewModel,
    searchItemInfo: SearchItemInfo
) {

    val TAG: String = "BookShelfScreen"

    //StoreHouse 数据收集
    val storeHouses by storeHouseModel.storeHouses.collectAsStateWithLifecycle()
    val isShow by storeHouseModel.isShow

    //是否显示导入compose
    var isImport by remember { mutableStateOf(false) }

    //ItemInfo 数据收集
    //这个是展示数据,可能是搜索数据,也可能是全部数据
    searchItemInfo.searchByCategory(GlobalSettings.getRecentStoreHouse())
    val itemInfos = searchItemInfo.items.collectAsLazyPagingItems()

    Column(modifier = Modifier.fillMaxSize()) {

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 96.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if(isShow){
                items(storeHouses) { storeHouse ->
                    StoreHouseCard(
                        storeHouse = storeHouse,
                        onClick = {
                            if(storeHouse.id != 1L){
                                GlobalSettings.setRecentStoreHouse(storeHouse.id)
                                searchItemInfo.searchByCategory(storeHouse.id)
                                storeHouseModel.isShow(false)
                            }else{
                                isImport =  true
                            }

                        }
                    )
                }

            }else{
                items(itemInfos.itemCount) { index ->
                    itemInfos[index]?.let { ItemInfoScreen(it, onToView = {
                        // ✅ 正确：触发导航，传递必要的参数
                        // ✅ 关键：对路径进行 URL 编码
                        // ✅ 关键：使用 Uri.encode()，不是 URLEncoder！
                        DocumentHolder.setCurrentItem(it)
                        GlobalSettings.addToHistory(it.id)
                        // ✅ 方式1：使用 navigate，确保正确进入栈
                        navController.navigate("comic_view") {
                            // 重要：不要 popUpTo，这样会保留返回栈
                            launchSingleTop = true
                        }
                    }) }
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
            SoundScreen()
        }

        if(isImport){
            StoreHouseInputDialog { isImport = false }
        }
    }
}
