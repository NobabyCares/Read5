package com.example.read5.viewmodel.comic
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.read5.bean.ComicPage
import com.example.read5.bean.PageLayout
import com.example.read5.bean.VirtualCanvas
import com.example.read5.utils.comic.ComicLoader
import com.example.read5.utils.comic.ImageStitcher
import com.example.read5.utils.comic.ZipComicLoader
import com.example.read5.utils.comic.SafFolderLoad
import com.example.read5.utils.comic.NaturalOrderComparator
import com.example.read5.utils.comic.SAFbuildVirtualCanvas
import com.example.read5.utils.comic.SafComicLoader
import com.example.read5.utils.comic.ZipbuilderVirtualCanvas
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
    private lateinit var safOrZipLoader: ComicLoader
    private lateinit var loadFile: SafFolderLoad

//    判断是不是SAF, false 不是SAF, true 是SAF
    private var isSaf = false;

    // 初始化 Loader
    suspend fun initLoader(context: Context, path: String) {
        // ✅ 关键：切换漫画时，清空旧缓存！
        _pageCache.value = emptyMap()

        if(path.startsWith("content://")){
            isSaf = true;
        }
        loadFile = SafFolderLoad(context, path)
        val sortedPages = loadFile.loadComic().sortedWith(
            compareBy(NaturalOrderComparator.naturalOrderComparator) { it.name }
        )
        if(isSaf){
            safOrZipLoader = SafComicLoader(
                context = context,
                imageFiles = sortedPages
            )
            _virtualCanvas.value = SAFbuildVirtualCanvas(sortedPages)

        }else{
            // ✅ 对页面按文件名进行「自然排序」（支持 1.jpg, 2.jpg, ..., 10.jpg）
            safOrZipLoader = ZipComicLoader(
                zipPath = path,
                pageNames = sortedPages,
                cacheDir = File(context.cacheDir, "comic_cache_${path.hashCode()}"),
                scope = viewModelScope
            )
            _virtualCanvas.value = ZipbuilderVirtualCanvas(sortedPages)
        }
        Log.d(TAG, " initLoader_success")
    }

    // 加载指定页
    fun loadPage(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            if(isSaf){

            }else{
                // 假设这是在一个 suspend 函数或协程作用域中
                val cachedBitmap = _pageCache.value[index]
                if (cachedBitmap == null) {
                    // 缓存未命中，加载并填充
                    val bitmap = withContext(Dispatchers.IO) {
                        safOrZipLoader.loadPage(index)
                    }
                    if (bitmap != null) {
                        // 更新缓存（在主线程）
                        _pageCache.value = _pageCache.value + (index to bitmap)
                    }
                }
            }
        }
    }

    // 清理缓存
    suspend fun clearCache() {
        safOrZipLoader.clearCache()
    }
}