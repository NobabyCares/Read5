package com.example.read5.screens.comictype

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.read5.bean.ComicType
import com.example.read5.global.GlobalSettings
import com.example.read5.screens.iteminfo.ItemInfoScreen
import com.example.read5.singledata.DocumentHolder
import com.example.read5.viewmodel.comictype.ComicTypeSearchViewModel

@Composable
fun ComicTypeContentScreen(navHostController: NavHostController){

    val TAG = "ComicTypeContentScreen"
    val comicTypeSearchViewModel: ComicTypeSearchViewModel = hiltViewModel()

    //DATA
    val comicTypeItems by comicTypeSearchViewModel.allTypes.collectAsStateWithLifecycle()
    // 1. 监听数据变化
    val itemsWithTypes by comicTypeSearchViewModel.itemsByType.collectAsStateWithLifecycle()

    //UI
    //加载数据
    var isShowItemInfo by remember { mutableStateOf(false) }
    //id
    var typeId by remember { mutableIntStateOf(0) }

    // 2. 当 typeId 变化时，通知 ViewModel 加载数据
    LaunchedEffect(typeId) {
        comicTypeSearchViewModel.loadItemsForType(typeId)
    }

    // ✅ 核心：拦截返回键
    BackHandler(enabled = isShowItemInfo) {
        // 如果正在显示详情，按下返回键只关闭详情，不退出页面
        isShowItemInfo = false
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
            if(isShowItemInfo){
                items(itemsWithTypes) { itemWrapper  ->
                    ItemInfoScreen( item = itemWrapper.item, onToView = {
                        DocumentHolder.setCurrentItem(itemWrapper.item)
                        val readMode = GlobalSettings.getReadMode()
                        navHostController.navigate(readMode) {
                            launchSingleTop = true
                        }
                    })
                }
            }else{
                items(comicTypeItems) { index ->
                    ComicTypeItemScreen(comicTypeItem = index){
                        isShowItemInfo = true
                        typeId = index.id
                    }
                }
            }
        }

    }
}





