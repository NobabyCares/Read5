package com.example.read5.viewmodel.iteminfo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.read5.bean.ItemInfo
import com.example.read5.global.GlobalSettings
import com.example.read5.repository.iteminfo.ItemInfoRepository
import com.example.read5.screens.sortbar.SortField
import com.example.read5.screens.sortbar.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SearchItemInfo @Inject constructor(
    private val itemInfoRepository: ItemInfoRepository
): ViewModel() {
    val TAG = "GetItemInfo"
    // ✅ 正确写法1：直接初始化
    private val _dataSource = MutableStateFlow<SearchItemDataSource>(
        SearchItemDataSource.searchByCategory(GlobalSettings.getRecentStoreHouse())
     )

    // 2. 使用 stateIn 转换为 StateFlow<PagingData<ItemInfo>>
    @OptIn(ExperimentalCoroutinesApi::class)
    val items: StateFlow<PagingData<ItemInfo>> = _dataSource
        .flatMapLatest { source ->
            when (source) {
                is SearchItemDataSource.searchByCategory -> {
                    Log.d(TAG, "searchByCategory")
                    itemInfoRepository.searchByCategory(source.categoryId).cachedIn(viewModelScope)  // ✅ 重要
                }

                is SearchItemDataSource.searchById ->{
                        itemInfoRepository.searchById(source.id).cachedIn(viewModelScope)  // ✅ 重要
                    }

                is SearchItemDataSource.searchByName ->{
                    itemInfoRepository.searchByName(source.query.trim()).cachedIn(viewModelScope)  // ✅ 重要
                }

                is SearchItemDataSource.searchByIsShow ->{
                    itemInfoRepository.searchByIsShow().cachedIn(viewModelScope)  // ✅ 重要
                }

                is SearchItemDataSource.searchByIsCollect ->{
                    itemInfoRepository.searchByIsCollect().cachedIn(viewModelScope)  // ✅ 重要
                }

                is SearchItemDataSource.sortByField ->{
                    itemInfoRepository.sortBySortField(source.name, source.ascending, source.category).cachedIn(viewModelScope)  // ✅ 重要
                }

            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PagingData.empty()
        )

    //搜索名字
    fun searchByName(newQuery: String) {
            _dataSource.value = SearchItemDataSource.searchByName(newQuery)
    }

    fun searchById(newQuery: List<Long>) {
        _dataSource.value = SearchItemDataSource.searchById(newQuery)
    }

    fun searchByCategory(categoryId: Long) {
        _dataSource.value = SearchItemDataSource.searchByCategory(categoryId)
    }

    fun searchByIsShow() {
        _dataSource.value = SearchItemDataSource.searchByIsShow(false)
    }

    fun searchByIsCollect() {
        _dataSource.value = SearchItemDataSource.searchByIsCollect
    }
    fun sortBySortField(sortOption: SortOption, category: Long) {
        //ascending: true 升序, false 降序
        _dataSource.value = SearchItemDataSource.sortByField(sortOption.field, sortOption.ascending, category)
    }
}