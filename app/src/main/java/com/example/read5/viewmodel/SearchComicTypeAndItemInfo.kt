package com.example.read5.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.map
import com.example.read5.bean.ComicType
import com.example.read5.bean.ItemInfo
import com.example.read5.repository.comictype.ComicTypeRepository
import com.example.read5.repository.comictype.ComicTypeRepositoryApi // 假设的路径
import com.example.read5.repository.iteminfo.ItemInfoRepository
import com.example.read5.repository.iteminfo.ItemInfoRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

// 定义搜索模式
enum class SearchMode {
    ITEMS,      // 搜书 (A表)
    COMIC_TYPE   // 搜分类 (B表)
}

// 定义统一的搜索结果 (使用 Sealed Class 类型安全地包裹不同数据)
sealed class SearchResult {
    data class ItemInfoItems(val item: ItemInfo) : SearchResult()
    data class ComicTypeItems(val type: ComicType) : SearchResult()
}

@HiltViewModel
class SearchComicTypeAndItemInfo @Inject constructor(
    private val itemRepository: ItemInfoRepositoryImpl,
    private val comicTypeRepository: ComicTypeRepositoryApi
) : ViewModel() {

    // 搜索结果列表
    private val _results = MutableStateFlow<List<SearchResult>>(emptyList())
    val results: StateFlow<List<SearchResult>> = _results.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 当前模式 (可选，用于调试或 UI 提示)
    private val _currentMode = MutableStateFlow<SearchMode>(SearchMode.ITEMS)
    val currentMode: StateFlow<SearchMode> = _currentMode.asStateFlow()

    /**
     * 核心搜索方法
     * @param query 搜索关键词
     * @param mode 搜索模式 (ITEM 或 CATEGORY)
     */
    fun search(query: String, mode: SearchMode) {
        _currentMode.value = mode

        if (query.isBlank()) {
            _results.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                when (mode) {
                    SearchMode.ITEMS -> {
                        // 如果 itemRepository.searchByName 也返回 Flow
                        itemRepository.searchByName(query).first()
                            .map { SearchResult.ItemInfoItems(it) }
                            .let { _results.value = it }
                    }
                    SearchMode.COMIC_TYPE -> {
                        comicTypeRepository.getComicTypeByName(query).first()
                            .map { SearchResult.ComicTypeItems(it) }
                            .let { _results.value = it }
                    }
                }
            } catch (e: Exception) {
                _results.value = emptyList()
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clear() {
        _results.value = emptyList()
        _isLoading.value = false
    }
}