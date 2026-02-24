package com.example.read5.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import com.example.read5.bean.StoreHouse
import com.example.read5.global.GlobalSettings
import com.example.read5.screens.iteminfo.ItemInfoScreen
import com.example.read5.screens.miniweight.LazyGridScrollbar
import com.example.read5.screens.miniweight.ScrollToTopButton
import com.example.read5.screens.sortbar.SortBarScreen
import com.example.read5.screens.sortbar.SortField
import com.example.read5.screens.sortbar.SortOption
import com.example.read5.screens.sortbar.getSortOptions
import com.example.read5.screens.storehouse.StoreHouseCard
import com.example.read5.screens.storehouse.StoreHouseInputDialog
import com.example.read5.viewmodel.storehouse.StoreHouseViewModel
import com.example.read5.singledata.DocumentHolder
import com.example.read5.viewmodel.iteminfo.SearchItemInfo
import java.lang.Long.MAX_VALUE

@Composable
fun BookShelfScreen(
    navController: NavHostController,
    storeHouseModel: StoreHouseViewModel,
    searchItemInfo: SearchItemInfo,
    displayMode: String,

) {
    val TAG = "BookShelfScreen"

    val storeHouses by storeHouseModel.storeHouses.collectAsStateWithLifecycle()
    val itemInfos = searchItemInfo.items.collectAsLazyPagingItems()

    val isShow by storeHouseModel.isShow
    var isImport by remember { mutableStateOf(false) }

    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    val showScrollToTop = remember { mutableStateOf(false) }
    var currentSortType by remember { mutableStateOf(getSortOptions()) }
    var ascOrDesc by remember { mutableStateOf(GlobalSettings.getAscOrdesc()) }

    val storeCount = GlobalSettings.getitemCount()




    // ✅ 使用 LaunchedEffect 控制初始化，只在第一次加载时执行
    // 使用 displayMode 决定加载什么数据
    LaunchedEffect(displayMode, currentSortType, ascOrDesc) {
        when (displayMode) {
            "history" -> {
                searchItemInfo.history()
                storeHouseModel.isShow(false)
            }
            "bookdesk" -> {
                searchItemInfo.sortBySortField(currentSortType, ascOrDesc)
                storeHouseModel.isShow(false)
            }
            "category" -> {
                searchItemInfo.searchByCategory(GlobalSettings.getRecentStoreHouse())
                storeHouseModel.isShow(false)
            }
            "bookshelf" -> {
                storeHouseModel.isShow(true)
            }
            else -> {
               Log.e(TAG, "Invalid displayMode: $displayMode")
            }
        }
    }

    // 在 Box 之前添加这个 LaunchedEffect
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex }
            .collect { index ->
                // 当滚动超过 3 个 item 时显示按钮
                showScrollToTop.value = index > 3
            }
    }



    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Adaptive(minSize = 96.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isShow) {
                    items(storeHouses) { storeHouse ->
                        StoreHouseCard(
                            storeHouse = storeHouse,
                            onClick = {
                                if (storeHouse.id != 1L) {
                                    GlobalSettings.setRecentStoreHouse(storeHouse.id)
                                    GlobalSettings.setitemCount(storeHouse.count)
                                    searchItemInfo.searchByCategory(storeHouse.id)
                                    storeHouseModel.isShow(false)
                                    navController.navigate("bookshelf/category")
                                } else {
                                    isImport = true
                                }
                            }
                        )
                    }
                } else {
                    val readMode = GlobalSettings.getReadMode()
                    items(itemInfos.itemCount) { index ->
                        itemInfos[index]?.let {
                            ItemInfoScreen(it, onToView = {
                                DocumentHolder.setCurrentItem(it)
                                navController.navigate(readMode) {
                                    launchSingleTop = true
                                }
                            })
                        }
                    }
                }
            }

            SortBarScreen(
                currentSortType = currentSortType,
                isAscending = ascOrDesc,
                onSortChanged =  {  sortOption, isAscending ->
                    currentSortType = sortOption
                    ascOrDesc = isAscending
                    searchItemInfo.sortBySortField(sortOption, isAscending)
                    GlobalSettings.setSortType(sortOption.key)
                    GlobalSettings.setAscOrdesc(isAscending)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            if (isImport) {
                StoreHouseInputDialog { isImport = false }
            }
        }


        // 滚动条
        LazyGridScrollbar(
            gridState = gridState,
            totalItems = storeCount,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
        )


        // 返回顶部按钮 - 调整位置到SortBar上方
        if (showScrollToTop.value) {
            ScrollToTopButton(
                onClick = {
                    coroutineScope.launch {
                        gridState.animateScrollToItem(0)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)  // 仍然右下角对齐
                    .padding(bottom = 80.dp, end = 16.dp)  // 增加底部padding，让按钮在SortBar上方
            )
        }
    }
}


