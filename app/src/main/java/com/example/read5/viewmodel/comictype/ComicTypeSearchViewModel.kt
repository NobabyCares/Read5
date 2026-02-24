package com.example.read5.viewmodel.comictype

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.read5.bean.ComicType
import com.example.read5.repository.comictype.ComicTypeRepositoryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComicTypeSearchViewModel @Inject constructor(
    private val repository: ComicTypeRepositoryApi
): ViewModel() {

    val TAG = "ComicTypeSearchViewModel"


    // 直接暴露 StateFlow
    val allTypes: StateFlow<List<ComicType>> = repository
        .getAllTypesFlow()
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

    fun insertItemToTypes(itemId: Long, typeIds: List<Int>) {
        viewModelScope.launch {
            try {
                repository.insertItemToTypes(itemId, typeIds)
                // 可选：插入成功后刷新列表（如果 getAll 是 Flow，会自动更新）
            } catch (e: Exception) {
                Log.e(TAG, "插入失败：${e.message}")
            }
        }
    }

    public suspend fun getTypeIdByItemId(id: Long): List<Int> {
        return repository.getTypeIdByItemId(id)
    }



}