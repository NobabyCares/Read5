package com.example.read5.viewmodel.comic
import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.read5.bean.VirtualCanvas
import com.example.read5.cache.ComicPageCache
import com.example.read5.utils.comic.BuilderVirtualCanvas
import com.example.read5.utils.comic.ComicLoader
import com.example.read5.utils.comic.ComicLoaderFolder
import com.example.read5.utils.comic.ComicLoaderZip
import com.example.read5.utils.comic.ZipOrFolderLoad
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ComicViewModel @Inject constructor() : ViewModel() {

    val TAG = "ComicViewModel"

    // ✅ 直接缓存 <index, bitmap>，用 immutable map 触发重组
    private val _pageCache = MutableStateFlow<Map<Int, ImageBitmap>>(emptyMap())
    val pageCache: StateFlow<Map<Int, ImageBitmap>> = _pageCache


    private val _virtualCanvas = MutableStateFlow<VirtualCanvas?>(null)
    val virtualCanvas: StateFlow<VirtualCanvas?> = _virtualCanvas



    // 内部 LazyZipPageLoader 实例
    private lateinit var folderOrZipLoader: ComicLoader
//    加载图片
    private lateinit var loadFile: ZipOrFolderLoad

    // 初始化 Loader
    suspend fun initLoader(context: Context, path: String) {
        // ✅ 关键：切换漫画时，清空旧缓存！
        // ✅ 1. 清空旧缓存（关键！）
        _pageCache.value = emptyMap()

        loadFile = ZipOrFolderLoad(context, path)
        val sortedPages = loadFile.loadComic()
        if(path.substringAfterLast( ".", "") == "zip"){
            folderOrZipLoader = ComicLoaderZip(
                zipPath = path,
                pageNames = sortedPages,
                cacheDir = File(context.cacheDir, "comic_cache_${path.hashCode()}"),
                scope = viewModelScope
            )
        }else{
            folderOrZipLoader = ComicLoaderFolder(
                path = path,
                pageNames = sortedPages,
            )
        }
        _virtualCanvas.value = BuilderVirtualCanvas.builderVirtualCanvas(sortedPages)
        Log.d(TAG, " initLoader_success")
    }

    // 加载指定页（外部调用）
    fun loadPage(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = folderOrZipLoader.loadPage(index)
            if (bitmap != null) {
                // 回到主线程更新 StateFlow
                viewModelScope.launch(Dispatchers.Main) {
                    _pageCache.value += mapOf(index to bitmap)
                }
            }
        }
    }

    // 获取 bitmap（供 Composable 使用）
    fun getPageBitmap(pageIndex: Int): ImageBitmap? {
        val bitmap =_pageCache.value[pageIndex]
        if(bitmap == null){
            loadPage(pageIndex)
        }
        return _pageCache.value[pageIndex]
    }


    // 清理缓存
    suspend fun clearCache() {
    }
}