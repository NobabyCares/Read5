package com.example.read5.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.example.read5.global.GlobalSettings
import com.example.read5.screens.iteminfo.ItemInfoScreen
import com.example.read5.screens.sortbar.SortBarScreen
import com.example.read5.screens.storehouse.StoreHouseCard
import com.example.read5.screens.storehouse.StoreHouseInputDialog
import com.example.read5.viewmodel.storehouse.StoreHouseViewModel
import com.example.read5.singledata.DocumentHolder
import com.example.read5.viewmodel.iteminfo.SearchItemInfo

@Composable
fun BookShelfScreen(
    navController: NavHostController,
    storeHouseModel: StoreHouseViewModel,
    searchItemInfo: SearchItemInfo,
) {
    val storeHouses by storeHouseModel.storeHouses.collectAsStateWithLifecycle()
    val isShow by storeHouseModel.isShow
    var isImport by remember { mutableStateOf(false) }
    val itemInfos = searchItemInfo.items.collectAsLazyPagingItems()


    // ✅ 使用 LaunchedEffect 控制初始化，只在第一次加载时执行
    LaunchedEffect(Unit) {
        searchItemInfo.searchByCategory(GlobalSettings.getRecentStoreHouse())
    }


    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val showScrollToTop = remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(gridState.firstVisibleItemIndex) {
        showScrollToTop.value = gridState.firstVisibleItemIndex > 3
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
                                    searchItemInfo.searchByCategory(storeHouse.id)
                                    storeHouseModel.isShow(false)
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
                                GlobalSettings.addToHistory(it)
                                navController.navigate(readMode) {
                                    launchSingleTop = true
                                }
                            })
                        }
                    }
                }
            }

            SortBarScreen(
                GlobalSettings.getRecentStoreHouse(),
                searchItemInfo = searchItemInfo
            )

            if (isImport) {
                StoreHouseInputDialog { isImport = false }
            }
        }

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

/**
 * 返回顶部悬浮按钮
 */
@Composable
fun ScrollToTopButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = "返回顶部"
        )
    }
}