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
    // 2. 再初始化缓存，并传入回调
    // 先创建缓存实例，再设置回调（避免初始化时自引用）
    private val comicPageCache: ComicPageCache = run {
        val cache = ComicPageCache(maxPages = ComicPageCache.MAX_SIZE_IN_PIXELS)
        cache.onChange = {
            viewModelScope.launch(Dispatchers.Main) {
                Log.d(TAG, "Updating _pageCache with size: ${cache.snapshot().size}")
                _pageCache.value = cache.snapshot() // 👈 用局部变量 `cache`
            }
        }
        cache
    }


    // 改为普通属性（或直接不存）
    private var virtualCanvas: VirtualCanvas? = null



    // 内部 LazyZipPageLoader 实例
    private lateinit var folderOrZipLoader: ComicLoader
//    加载图片
    private lateinit var loadFile: ZipOrFolderLoad

    // 初始化 Loader
    suspend fun initLoader(context: Context, path: String): VirtualCanvas {
        // ✅ 关键：切换漫画时，清空旧缓存！
        // ✅ 1. 清空旧缓存（关键！）
        _pageCache.value = emptyMap()
        comicPageCache.clear()

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
        virtualCanvas = BuilderVirtualCanvas.builderVirtualCanvas(sortedPages)
        // ✅ 立即预加载第一页及下一页
        if(virtualCanvas != null){
            viewModelScope.launch(Dispatchers.Main) {
                preloadPages(currentOffsetY = 0f, virtualCanvas = virtualCanvas!!)
            }
        }
        return virtualCanvas!! // 直接返回
    }

    // 加载指定页（外部调用）
    fun loadPage(index: Int) {
        if (_pageCache.value.containsKey(index)) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = folderOrZipLoader.loadPage(index)
            if (bitmap != null) {
                // 1. 放入 LRU 缓存（长期存储）
                comicPageCache.put(index, bitmap)

            }
        }
    }


    // 新增函数：预加载附近页面
    fun preloadPages(currentOffsetY: Float, virtualCanvas: VirtualCanvas) {
        val visibleTop = -currentOffsetY.toInt()
        val visibleBottom = visibleTop + 2000 // 屏幕高度+缓冲

        val pagesToPreload = virtualCanvas.pageLayouts
            .filter { it.bottom > visibleTop && it.top < visibleBottom }
            .map { it.index }
            .distinct()

        // 额外预加载前后一页（防滑动卡顿）
        val extendedPages = mutableSetOf<Int>()
        pagesToPreload.forEach { idx ->
            extendedPages += idx
            if (idx > 0) extendedPages += idx - 1
            extendedPages += idx + 1
        }

        extendedPages.forEach { loadPage(it) }
    }




}