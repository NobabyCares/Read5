package com.example.read5.viewmodel.iteminfo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.read5.bean.ItemInfo
import com.example.read5.global.GlobalSettings
import com.example.read5.repository.iteminfo.ItemInfoRepository
import com.example.read5.screens.sortbar.SortField
import com.example.read5.screens.sortbar.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed interface BookShelfOfItemInfo{
    data class BOOKSHELF(val field: SortField, val ascOrDesc: Boolean, val categoryId: Long) : BookShelfOfItemInfo
    data object HISTORY : BookShelfOfItemInfo


}


@HiltViewModel
class BookShelfOfItemInfoViewModel @Inject constructor(
    private val itemInfoRepository: ItemInfoRepository
): ViewModel() {
    val TAG = "GetItemInfo"
    // ✅ 正确写法1：直接初始化
    private val _dataSource = MutableStateFlow<BookShelfOfItemInfo>(
        BookShelfOfItemInfo.BOOKSHELF(SortField.NAME, true, GlobalSettings.getRecentStoreHouse())
    )
    // ✅ 保存当前的分类ID
    private var _currentCategoryId = MutableStateFlow(GlobalSettings.getRecentStoreHouse())




    // 2. 使用 stateIn 转换为 StateFlow<PagingData<ItemInfo>>
    @OptIn(ExperimentalCoroutinesApi::class)
    val items: StateFlow<PagingData<ItemInfo>> = _dataSource
        .flatMapLatest { source ->
            when (source) {

                is BookShelfOfItemInfo.BOOKSHELF ->{
                    itemInfoRepository.sortBySortField(source.field, source.ascOrDesc, source.categoryId).cachedIn(viewModelScope)  // ✅ 重要
                }
                // 新增：处理历史记录
                is BookShelfOfItemInfo.HISTORY -> {
                    flow {
                        val historyList = GlobalSettings.getHistory()
                        emit(PagingData.from(historyList))
                    }.cachedIn(viewModelScope)
                }



            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PagingData.empty()
        )


    fun history() {
        _dataSource.value = BookShelfOfItemInfo.HISTORY
    }

    fun sortBySortField(sortOption: SortOption, ascending: Boolean) {
        //ascending: true 升序, false 降序
        _dataSource.value = BookShelfOfItemInfo.BOOKSHELF(sortOption.field, ascending, _currentCategoryId.value)
    }

    fun updateCollectStatus(id: Long, isCollect: Boolean) {
        viewModelScope.launch {
            itemInfoRepository.updateByCollect(id, isCollect)
        }
    }




}