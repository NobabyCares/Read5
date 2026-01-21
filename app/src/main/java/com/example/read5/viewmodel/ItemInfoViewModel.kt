package com.example.read5.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.filter
import com.example.read5.bean.ItemInfo
import com.example.read5.bean.StoreHouse
import com.example.read5.repository.ItemInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ItemInfoViewModel @Inject constructor(
    private val itemInfoRepository: ItemInfoRepository
) : ViewModel() {

    // 当前筛选的 category（null = 全部）
    private val _currentCategory = MutableStateFlow<Long?>(null)

    // 当前展开的 StoreHouse 的 id，初始为 -1（表示无展开）
    private val _expandedStoreId = mutableStateOf<Long?>(null)
    val expandedStoreId: State<Long?> = _expandedStoreId

    private val _fileTypeFilter = MutableStateFlow<String?>(null)


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


    // ✅ 核心：每当 category 变化，就发射新的 PagingData 流
    val pagedItems: StateFlow<Flow<PagingData<ItemInfo>>> = _currentCategory
        .map { categoryId ->
            itemInfoRepository.getItemsPager(categoryId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = itemInfoRepository.getItemsPager(null)
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

    // 切换分类（或设为 null 查看全部）
    fun setCategory(categoryId: Long?) {
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