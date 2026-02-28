package com.example.read5.screens.storehouse

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.read5.global.GlobalSettings
import com.example.read5.viewmodel.storehouse.StoreHouseViewModel

@Composable
fun StoreHouseContentScreen(
    navHostController: NavHostController,
    storeHouseViewModel: StoreHouseViewModel,
) {
    val TAG= "StoreHouseContentScreen"

    //DATA
    val storeHouseItems by storeHouseViewModel.storeHouses.collectAsStateWithLifecycle()

    //UI
    //记录LazyVerticalGrid滑动的位置
    val gridState = rememberLazyGridState()
    //控制顶部悬浮按钮
    val showScrollToTop = remember { mutableStateOf(false) }
    var isImport by remember { mutableStateOf(false) }

    // 在 Box 之前添加这个 LaunchedEffect
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex }
            .collect { index ->
                // 当滚动超过 3 个 item 时显示按钮
                showScrollToTop.value = index > 3
            }
    }

    Column {
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
            items(storeHouseItems.size) { index ->
                val storeHouse = storeHouseItems[index]
                StoreHouseCard(
                    storeHouse = storeHouse,
                    onClick = {
                        Log.d(TAG, "点击了 StoreHouseContentScreen")
                        if (storeHouse.id != 1L) {
                            GlobalSettings.setRecentStoreHouse(storeHouse.id)
                            GlobalSettings.setitemCount(storeHouse.count)
                            storeHouseViewModel.isShow(false)
                            navHostController.navigate("bookshelf/bookdesk")
                        } else {
                            isImport = true
                        }
                    }
                )
            }

        }

        if (isImport) {
            StoreHouseInputDialog { isImport = false }
        }
    }

}