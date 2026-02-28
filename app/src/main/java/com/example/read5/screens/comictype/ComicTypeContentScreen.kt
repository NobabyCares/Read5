package com.example.read5.screens.comictype

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.read5.bean.ItemInfo
import com.example.read5.global.GlobalSettings
import com.example.read5.screens.editdialog.ManagerEditDialog
import com.example.read5.screens.iteminfo.ItemInfoScreen
import com.example.read5.singledata.DocumentHolder
import com.example.read5.viewmodel.comictype.ComicTypeSearchViewModel

@Composable
fun ComicTypeContentScreen(navHostController: NavHostController) {
    val TAG = "ComicTypeContentScreen"
    val comicTypeSearchViewModel: ComicTypeSearchViewModel = hiltViewModel()

    // 从 ViewModel 获取状态
    val comicTypeItems by comicTypeSearchViewModel.allTypes.collectAsStateWithLifecycle()
    val itemsWithTypes by comicTypeSearchViewModel.itemsByType.collectAsStateWithLifecycle()
    val isShowItemInfo by comicTypeSearchViewModel.isShowItemInfo.collectAsStateWithLifecycle()
    val currentTypeId by comicTypeSearchViewModel.currentTypeId.collectAsStateWithLifecycle()

    // 对话框状态
    var showManagerDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<ItemInfo?>(null) }

    // ✅ 当有 currentTypeId 时，确保加载数据
    LaunchedEffect(currentTypeId) {
        currentTypeId?.let { typeId ->
            // 这里不需要额外操作，因为 itemsByType 已经通过 flatMapLatest 自动加载
            Log.d(TAG, "Current type ID: $typeId")
        }
    }

    // 处理返回键
    BackHandler(enabled = isShowItemInfo) {
        comicTypeSearchViewModel.backToTypes()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 96.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isShowItemInfo) {
                // ✅ 显示分类下的书籍
                items(itemsWithTypes) { itemWrapper ->
                    ItemInfoScreen(
                        item = itemWrapper.item,
                        onToView = {
                            DocumentHolder.setCurrentItem(itemWrapper.item)
                            val readMode = GlobalSettings.getReadMode()
                            navHostController.navigate(readMode) {
                                launchSingleTop = true
                            }
                        },
                        onLongPress = {
                            Log.d(TAG, "Long press on item ${itemWrapper.item.id}")
                            selectedItem = itemWrapper.item
                            showManagerDialog = true
                        }
                    )
                }
            } else {
                // ✅ 显示分类列表
                items(comicTypeItems) { type ->
                    ComicTypeItemScreen(
                        comicTypeItem = type,
                        onChangeComicType = {
                            comicTypeSearchViewModel.enterType(type.id)  // ✅ 使用 enterType
                        }
                    )
                }
            }
        }
    }

    // 显示编辑对话框
    if (showManagerDialog && selectedItem != null) {
        ManagerEditDialog(
            item = selectedItem!!,
            onDismiss = {
                showManagerDialog = false
                selectedItem = null
            }
        )
    }
}