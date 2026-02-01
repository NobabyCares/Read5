package com.example.read5.viewmodel.comic
import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.read5.bean.ComicPage
import com.example.read5.utils.comic.LazyZipComicUtils
import com.example.read5.utils.comic.loadComicPages
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ComicViewModel @Inject constructor() : ViewModel() {

    // 状态 Flow，用于通知 UI 更新
    private val _currentPage = MutableStateFlow<ImageBitmap?>(null)
    val currentPage: StateFlow<ImageBitmap?> = _currentPage

    // 内部 LazyZipPageLoader 实例
    private lateinit var lazyLoader: LazyZipComicUtils

    // 初始化 Loader
    suspend fun initLoader(context: Context, path: String) {
        val comicPages = loadComicPages(context, path)
        lazyLoader = LazyZipComicUtils(
            zipPath = path,
            pageNames = comicPages.map{ it.name},
            cacheDir = File(context.cacheDir, "comic_cache_${path.hashCode()}"),
            scope = viewModelScope
        )
    }

    // 加载指定页
    fun loadPage(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val imageBitmap = lazyLoader.loadPage(index)
            _currentPage.value = imageBitmap
        }
    }

    // 预加载前后 N 页
    fun preloadPages(currentIndex: Int, range: Int = 2) {
        lazyLoader.preloadPages(currentIndex, range)
    }

    // 清理缓存
    fun clearCache() {
        lazyLoader.clearCache()
    }
}