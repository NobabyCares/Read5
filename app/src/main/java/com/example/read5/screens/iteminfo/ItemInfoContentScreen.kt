package com.example.read5.screens.iteminfo

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.read5.global.GlobalSettings
import com.example.read5.screens.miniweight.LazyGridScrollbar
import com.example.read5.screens.miniweight.ScrollToTopButton
import com.example.read5.screens.sortbar.SortBarScreen
import com.example.read5.screens.sortbar.SortField
import com.example.read5.screens.sortbar.getSortOptions
import com.example.read5.screens.storehouse.StoreHouseCard
import com.example.read5.screens.storehouse.StoreHouseInputDialog
import com.example.read5.singledata.DocumentHolder
import com.example.read5.viewmodel.iteminfo.BookShelfOfItemInfoViewModel
import com.example.read5.viewmodel.iteminfo.UpdateItemInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ItemInfoContentScreen(
    navHostController: NavHostController,
    bookShelfOfItemInfoViewModel: BookShelfOfItemInfoViewModel,
) {
    val TAG = "ItemInfoContentScreen"

    //副作用
    val coroutineScope = rememberCoroutineScope()



    //DATA
    // ✅ 1. 收集 PagingData
    // collectAsLazyPagingItems() 会自动处理 StateFlow<PagingData>
    val itemInfos = bookShelfOfItemInfoViewModel.items.collectAsLazyPagingItems()
//    val itemInfos = bookShelfOfItemInfoViewModel.items.collectAsLazyPagingItems()

    //global数据
    //排序
    var currentSortType by remember { mutableStateOf(getSortOptions()) }
    var ascOrDesc by remember { mutableStateOf(GlobalSettings.getAscOrdesc()) }

    Log.d(TAG, "ItemInfoContentScreen - currentSortType: ${currentSortType.label} ascOrDesc: ${ascOrDesc}")



    //UI
    //记录LazyVerticalGrid滑动的位置
    val gridState = rememberLazyGridState()
    //悬浮顶部按钮
    val showScrollToTop = remember { mutableStateOf(false) }

    // 在 Box 之前添加这个 LaunchedEffect
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex }
            .collect { index ->
                val previousValue = showScrollToTop.value
                // 当滚动超过 3 个 item 时显示按钮
                showScrollToTop.value = index > 3
                if (previousValue != showScrollToTop.value) {
                    Log.d(
                        TAG,
                        "Scroll position changed: firstVisibleItemIndex=$index, showScrollToTop=${showScrollToTop.value}"
                    )
                }
            }
    }

    // 添加网格滚动偏移量监听
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemScrollOffset }
            .collect { offset ->
                if (offset % 100 == 0) { // 每100px打印一次，避免日志太多
                    Log.d(TAG, "Scroll offset: $offset")
                }
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
                    .weight(1f)
                    .onSizeChanged { size ->
                    },
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val readMode = GlobalSettings.getReadMode()
                items(
                    itemInfos.itemCount,
                    // 如果 item 为 null (加载中)，返回 -1 或其他占位符
                    key = { index ->
                        val item = itemInfos[index]
                        item?.id ?: -1L
                    }
                ) { index ->
                    itemInfos[index]?.let {
                        ItemInfoScreen(
                            it,
                            onToView = {
                            DocumentHolder.setCurrentItem(it)
                            navHostController.navigate(readMode) {
                                launchSingleTop = true
                                 }
                            },
                        )
                    }
                }
            }

            SortBarScreen(
                currentSortType = currentSortType,
                isAscending = ascOrDesc,
                onSortChanged =  {  sortOption, isAscending ->
                    currentSortType = sortOption
                    ascOrDesc = isAscending
                    bookShelfOfItemInfoViewModel.sortBySortField(sortOption, isAscending)
                    GlobalSettings.setSortType(sortOption.key)
                    GlobalSettings.setAscOrdesc(isAscending)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        // 滚动条
        LazyGridScrollbar(
            gridState = gridState,
            totalItems = itemInfos.itemCount.toLong(),
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