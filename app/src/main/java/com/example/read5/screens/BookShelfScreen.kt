package com.example.read5.screens

import android.util.Log
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.read5.bean.StoreHouse
import com.example.read5.global.GlobalSettings
import com.example.read5.screens.bottombar.BottomBarScreen
import com.example.read5.screens.comictype.ComicTypeContentScreen
import com.example.read5.screens.comictype.ComicTypeItemScreen
import com.example.read5.screens.iteminfo.ItemInfoContentScreen
import com.example.read5.screens.iteminfo.ItemInfoScreen
import com.example.read5.screens.miniweight.LazyGridScrollbar
import com.example.read5.screens.miniweight.ScrollToTopButton
import com.example.read5.screens.sortbar.SortBarScreen
import com.example.read5.screens.sortbar.SortField
import com.example.read5.screens.sortbar.SortOption
import com.example.read5.screens.sortbar.getSortOptions
import com.example.read5.screens.storehouse.StoreHouseCard
import com.example.read5.screens.storehouse.StoreHouseContentScreen
import com.example.read5.screens.storehouse.StoreHouseInputDialog
import com.example.read5.screens.topbar.SearchBarScreen
import com.example.read5.screens.topbar.TopBarContent
import com.example.read5.viewmodel.storehouse.StoreHouseViewModel
import com.example.read5.singledata.DocumentHolder
import com.example.read5.viewmodel.SearchComicTypeAndItemInfo
import com.example.read5.viewmodel.SearchMode
import com.example.read5.viewmodel.SearchResult
import com.example.read5.viewmodel.comictype.ComicTypeSearchViewModel
import com.example.read5.viewmodel.iteminfo.BookShelfOfItemInfoViewModel
import java.lang.Long.MAX_VALUE

@Composable
fun BookShelfScreen(
    navHostController: NavHostController,
    displayMode: String,
) {

    val TAG = "BookShelfScreen"
    Log.d(TAG, "BookShelfScreen: ${displayMode}")
    // 1. 旧的 ViewModel (保持原有逻辑不变，负责正常列表)
    val bookShelfOfItemInfoViewModel: BookShelfOfItemInfoViewModel = hiltViewModel()
    val storeHouseViewModel: StoreHouseViewModel = hiltViewModel()

    // 2. ✅ 新增：引入独立的搜索 ViewModel
    val searchComicTypeAndItemInfo: SearchComicTypeAndItemInfo = hiltViewModel()

    //Global全局数据
    var readMode by remember { mutableStateOf(GlobalSettings.getReadMode()) }
    //global数据
    //排序
    val currentSortType = getSortOptions()
    val ascOrDesc = GlobalSettings.getAscOrdesc()

    // --- 本地状态管理 ---
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // 收集搜索 ViewModel 的数据
    val searchResults by searchComicTypeAndItemInfo.results.collectAsStateWithLifecycle()
    val isLoading by searchComicTypeAndItemInfo.isLoading.collectAsStateWithLifecycle()
    val currentMode by searchComicTypeAndItemInfo.currentMode.collectAsStateWithLifecycle()

    // --- 关键：根据当前页面模式，决定搜索策略 ---
    // 如果是 "comicType" 页面，默认搜分类；否则默认搜书籍
    val targetMode = if (displayMode == "comicType") SearchMode.COMIC_TYPE else SearchMode.ITEMS

    // 拦截返回键
    BackHandler(enabled = isSearching) {
        isSearching = false
        searchQuery = ""
    }

    // --- 监听输入，触发搜索 ---
    LaunchedEffect(searchQuery, targetMode) {
        if (isSearching) {
            // 调用新 ViewModel 的搜索方法
            searchComicTypeAndItemInfo.search(query = searchQuery, mode = targetMode)
        } else {
            // 退出搜索时清空数据
            searchComicTypeAndItemInfo.clear()
        }
    }

    // --- 拦截返回键 ---
    BackHandler(enabled = isSearching) {
        isSearching = false
        searchQuery = ""
        searchComicTypeAndItemInfo.clear()
    }


    // ✅ 这里才是 Scaffold 该待的地方！
    Scaffold(
        // 底部导航栏现在由书架页自己控制
        bottomBar = {
            if (!isSearching) BottomBarScreen(navHostController = navHostController)
        },
        topBar = {
            // ✅ 关键：只调用 TopBarContent，把所有状态和回调传给它
            TopBarContent(
                navController = navHostController,
                // 传递显示搜索框,这是搜索UI的回调
                onIsSearchingChange = { newIsSearching ->
                    isSearching = newIsSearching
                },
                // 传递回调,这是搜索数据的回调
                onQueryChange = { newQuery ->
                    searchQuery = newQuery
                }
            )
        }
    ) {paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues) // ✅ 加上这一行！
        ) {
            if (isSearching) {
                // ✅ 搜索界面：使用新 ViewModel 的数据
                Column(modifier = Modifier.fillMaxSize()) {
                    if (isLoading && searchResults.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (searchResults.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("未找到相关内容", style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        // 显示搜索结果列表
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 96.dp),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = searchResults,
                                key = { item ->
                                    // 生成唯一 Key：区分 Book 和 Category
                                    when (item) {
                                        is SearchResult.ItemInfoItems -> "book_${item.item.id}"
                                        is SearchResult.ComicTypeItems -> "cat_${item.type.id}"
                                    }
                                }
                            ) { result ->
                                // ✅ 核心：根据结果类型渲染不同卡片
                                when (result) {
                                    is SearchResult.ItemInfoItems -> {
                                        // 渲染书籍卡片
                                        ItemInfoScreen(result.item, onToView = {
                                            DocumentHolder.setCurrentItem(result.item)
                                            navHostController.navigate(readMode) {
                                                launchSingleTop = true
                                            }
                                        })
                                    }
                                    is SearchResult.ComicTypeItems -> {
                                        // 渲染分类卡片
                                        ComicTypeItemScreen(comicTypeItem = result.type)
                                    }
                                }
                            }
                        }
                    }
                }
            } else{
                when (displayMode) {
                    "history" -> {
                        bookShelfOfItemInfoViewModel.history()
                        storeHouseViewModel.isShow(false)
                        ItemInfoContentScreen(
                            navHostController = navHostController,
                            bookShelfOfItemInfoViewModel = bookShelfOfItemInfoViewModel
                        )
                    }
                    "bookdesk" -> {
                        bookShelfOfItemInfoViewModel.sortBySortField(currentSortType, ascOrDesc)
                        storeHouseViewModel.isShow(false)
                        ItemInfoContentScreen(
                            navHostController = navHostController,
                            bookShelfOfItemInfoViewModel = bookShelfOfItemInfoViewModel)
                    }
                    "bookshelf" -> {
                        StoreHouseContentScreen(navHostController = navHostController, storeHouseViewModel = storeHouseViewModel)
                    }
                    "comicType" -> {
                        ComicTypeContentScreen(navHostController = navHostController)
                    }
                    else -> {
                        Log.e(TAG, "Invalid displayMode: $displayMode")
                    }
                }
            }
        }
    }

}


