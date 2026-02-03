package com.example.read5.viewmodel.comic
import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.read5.bean.VirtualCanvas
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

//缓存
    private val _pageCache = MutableStateFlow<Map<Int, ImageBitmap?>>(mapOf())
    val pageCache: StateFlow<Map<Int, ImageBitmap?>> = _pageCache

    private val _virtualCanvas = MutableStateFlow<VirtualCanvas?>(null)
    val virtualCanvas: StateFlow<VirtualCanvas?> = _virtualCanvas


    // 内部 LazyZipPageLoader 实例
    private lateinit var folderOrZipLoader: ComicLoader
//    加载图片
    private lateinit var loadFile: ZipOrFolderLoad

    // 初始化 Loader
    suspend fun initLoader(context: Context, path: String) {
        // ✅ 关键：切换漫画时，清空旧缓存！
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

    // 加载指定页
    fun loadPage(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
                // 假设这是在一个 suspend 函数或协程作用域中
                val cachedBitmap = _pageCache.value[index]
                if (cachedBitmap == null) {
                    // 缓存未命中，加载并填充
                    val bitmap = withContext(Dispatchers.IO) {
                        folderOrZipLoader.loadPage(index)
                    }
                    if (bitmap != null) {
                        // 更新缓存（在主线程）
                        _pageCache.value += (index to bitmap)
                    }
                }
            }
        }

    // 清理缓存
    suspend fun clearCache() {
    }
}