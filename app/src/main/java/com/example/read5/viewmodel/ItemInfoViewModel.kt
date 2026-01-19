package com.example.read5.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.read5.bean.ItemInfo
import com.example.read5.bean.StoreHouse
import com.example.read5.repository.ItemInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
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

    fun toggleType(storeId: Long) {
        _expandedStoreId.value = if (_expandedStoreId.value == storeId) {
            null // 再次点击则收起
        } else {
            storeId // 展开该项
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

    // 切换分类（或设为 null 查看全部）
    fun setCategory(categoryId: Long?) {
        _currentCategory.value = categoryId
    }

    // 插入数据（Room 会自动触发所有相关 PagingSource 刷新！）
    fun insert(items: List<ItemInfo>) {
        viewModelScope.launch {
            itemInfoRepository.insert(items)
        }
    }
}