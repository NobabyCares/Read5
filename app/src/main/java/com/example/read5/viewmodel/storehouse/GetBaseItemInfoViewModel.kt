package com.example.read5.viewmodel.storehouse

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.filter
import com.example.read5.bean.ItemInfo
import com.example.read5.repository.ItemInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
* 这里是 ItemInfo Bean 数据库操作的ViewModel
* */
@HiltViewModel
class GetBaseItemInfoViewModel @Inject constructor(
    private val itemInfoRepository: ItemInfoRepository
) : ViewModel() {

    // 当前筛选的 category（null = 全部）
    private val _currentCategory = MutableStateFlow<Long>(1L)

    // 当前展开的 StoreHouse 的 id，初始为 -1（表示无展开）
    private val _expandedStoreId = mutableStateOf<Long?>(null)
    val expandedStoreId: State<Long?> = _expandedStoreId

    private val _fileTypeFilter = MutableStateFlow<String?>(null)


    private val _searchQuery = MutableStateFlow("")




    // ✅ 核心：每当 category 变化，就发射新的 PagingData 流
    private val pagedItems: StateFlow<Flow<PagingData<ItemInfo>>> = _currentCategory
        .map { categoryId ->
            itemInfoRepository.getItemsPager(categoryId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = itemInfoRepository.getItemsPager(1L)
        )

    val filteredPagedItems: StateFlow<Flow<PagingData<ItemInfo>>> =
        combine(pagedItems, _fileTypeFilter) { baseFlow, fileType ->
            if (fileType == null) {
                baseFlow // 不过滤
            } else {
                baseFlow.mapLatest { pagingData ->
                    pagingData.filter { item ->
                        item.fileType == fileType
                    }
                }
            }
        }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = pagedItems.value // 初始不过滤
            )

    val searchResults: Flow<PagingData<ItemInfo>> = _searchQuery
        .debounce(300) // 防抖 300ms
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(PagingData.empty())
            else itemInfoRepository.searchByName(query.trim())
        }



    fun updateQuery(newQuery: String) {
        _searchQuery.value = newQuery
    }


    fun toggleType(storeId: Long) {
        if (_expandedStoreId.value == storeId) {
            // 收起：清空 filter，自动显示全部
            _fileTypeFilter.value = null
            _expandedStoreId.value = null
        } else {
            // 展开：记录当前仓库
            _expandedStoreId.value = storeId
            // 注意：此时不设置 category！category 应由 setCategory 单独控制
        }
    }


    // 切换分类（或设为 null 查看全部）
    fun setCategory(categoryId: Long = 1L) {
        _currentCategory.value = categoryId
    }

//切换类型
    fun setFileTypeFilter(type: String?) {
        _fileTypeFilter.value = type
    }


    // 插入数据（Room 会自动触发所有相关 PagingSource 刷新！）
    fun insert(items: List<ItemInfo>) {
        viewModelScope.launch {
            itemInfoRepository.insert(items)
        }
    }


}