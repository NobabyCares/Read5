package com.example.read5.viewmodel.comictype

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.read5.bean.ComicType
import com.example.read5.bean.ItemWithTypes
import com.example.read5.repository.comictype.ComicTypeRepositoryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComicTypeSearchViewModel @Inject constructor(
    private val repository: ComicTypeRepositoryApi
): ViewModel() {

    val TAG = "ComicTypeSearchViewModel"

    // ✅ 保存显示状态
    private val _isShowItemInfo = MutableStateFlow(false)
    val isShowItemInfo: StateFlow<Boolean> = _isShowItemInfo.asStateFlow()
    // ✅ 保存当前选中的 typeId
    private val _currentTypeId = MutableStateFlow<Int?>(null)
    val currentTypeId: StateFlow<Int?> = _currentTypeId.asStateFlow()

    // 直接暴露 StateFlow
    val allTypes: StateFlow<List<ComicType>> = repository
        .getAllTypesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )



    // 2. 创建一个 StateFlow 来暴露 ItemWithTypes 列表
    // 初始值为空列表
    val itemsByType: StateFlow<List<ItemWithTypes>> = _currentTypeId
        .flatMapLatest { typeId ->
            if (typeId == null) {
                // 如果没有选中类型，返回空流
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                // 如果有选中类型，调用 suspend 函数获取数据，并转为 Flow
                kotlinx.coroutines.flow.flow {
                    try {
                        val result = getItemsByTypeId(typeId)
                        emit(result)
                    } catch (e: Exception) {
                        Log.e(TAG, "加载书籍失败: ${e.message}")
                        emit(emptyList()) // 出错也发空列表，避免 UI 卡住
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


     fun insertType(type: ComicType) {
        viewModelScope.launch {
            try {
                repository.insertType(type)
                // 可选：插入成功后刷新列表（如果 getAll 是 Flow，会自动更新）
            } catch (e: Exception) {
                Log.e(TAG, "插入失败：${e.message}")
            }
        }
    }

    // ✅ 新增：更新方法（先删除再插入）
    fun updateItemTypes(itemId: Long, newTypeIds: List<Int>) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Updating item $itemId with types: $newTypeIds")
                repository.updateItemTypes(itemId, newTypeIds)
                Log.d(TAG, "Update successful")

                // 刷新当前分类的书籍列表
                _currentTypeId.value?.let { typeId ->
                    loadItemsForType(typeId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "更新失败", e)
            }
        }
    }

    suspend fun getTypeIdByItemId(id: Long): List<Int> {
        return repository.getTypeIdByItemId(id)
    }

    fun deleteById(typeId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteById(typeId)
                // 可选：插入成功后刷新列表（如果 getAll 是 Flow，会自动更新）
            } catch (e: Exception) {
                Log.e(TAG, "插入失败：${e.message}")
            }
        }
    }

    fun updateComicTypeName(id: Int, name: String) {
        viewModelScope.launch {
            try {
                repository.updateComicTypeName(id, name)
                // 可选：插入成功后刷新列表（如果 getAll 是 Flow，会自动更新）
            } catch (e: Exception) {
                Log.e(TAG, "插入失败：${e.message}")
            }
        }
    }

    // 注意：getItemsByTypeId 保持为 suspend fun 供内部 flow 使用即可
    private suspend fun getItemsByTypeId(typeId: Int): List<ItemWithTypes> {
        val result = repository.getItemsByTypeId(typeId)
        repository.updateComicTypeCount(typeId, result.size)
        return result
    }

    // 3. 提供一个普通函数供 UI 调用，用来切换 typeId
    fun loadItemsForType(typeId: Int) {
        _currentTypeId.value = typeId
    }


    fun updateComicTypeCover(ids: List<Int>, cover: String) {
        viewModelScope.launch {
            try {
                // 直接传整个列表进去
                repository.updateComicTypeCover(ids,  cover)
                // 如果用的是 Flow 观察数据，UI 会自动刷新
            } catch (e: Exception) {
                Log.e(TAG, "更新失败", e)
            }
        }
    }



    // ✅ 进入分类
    fun enterType(typeId: Int) {
        _currentTypeId.value = typeId
        _isShowItemInfo.value = true
    }

    // ✅ 返回分类列表
    fun backToTypes() {
        _isShowItemInfo.value = false
        // 注意：不清除 _currentTypeId，保留它以便返回时直接使用
    }


    fun changeShowItemInfo(show: Boolean) {
        _isShowItemInfo.value = show
        if (!show) {
            // 如果返回分类列表，清除 typeId 以节省资源
            _currentTypeId.value = null
        }
    }



}