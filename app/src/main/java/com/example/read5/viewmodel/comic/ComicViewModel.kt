package com.example.read5.viewmodel.comic
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.read5.bean.ComicPage
import com.example.read5.utils.comic.LazyZipComicUtils
import com.example.read5.utils.comic.LoadFile
import com.example.read5.utils.comic.NaturalOrderComparator
import com.example.read5.utils.comic.SafComicLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ComicViewModel @Inject constructor() : ViewModel() {

    val TAG = "ComicViewModel"

    // 状态 Flow，用于通知 UI 更新
    // ✅ 新增：页面列表（只读暴露）
    private val _currentPage = MutableStateFlow<ImageBitmap?>(null)
    val currentPage: StateFlow<ImageBitmap?> = _currentPage

    // 内部 LazyZipPageLoader 实例
    private lateinit var lazyLoader: LazyZipComicUtils
    private lateinit var safLoader: SafComicLoader

    private lateinit var loadFile: LoadFile

    private var isSaf = false;



    // 初始化 Loader
    suspend fun initLoader(context: Context, path: String) {
        if(path.startsWith("content://")){
            isSaf = true;
        }
        loadFile = LoadFile(context, path)
        val comicPages  = loadFile.loadComic()
        val sortedPages = comicPages.sortedWith(
            compareBy(NaturalOrderComparator.naturalOrderComparator) { it.name }
        )
        if(isSaf){
            safLoader = SafComicLoader(
                context = context,
                imageFiles = sortedPages
            )
        }else{
            // ✅ 对页面按文件名进行「自然排序」（支持 1.jpg, 2.jpg, ..., 10.jpg）
            lazyLoader = LazyZipComicUtils(
                zipPath = path,
                pageNames = sortedPages,
                cacheDir = File(context.cacheDir, "comic_cache_${path.hashCode()}"),
                scope = viewModelScope
            )
        }
        Log.d(TAG, " initLoader_success")
        loadPage(0)
    }

    // 加载指定页
    fun loadPage(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            if(isSaf){
                val imageBitmap = safLoader.loadPage(index)
                _currentPage.value = imageBitmap
            }else{
                val imageBitmap = lazyLoader.loadPage(index)
                _currentPage.value = imageBitmap
            }
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