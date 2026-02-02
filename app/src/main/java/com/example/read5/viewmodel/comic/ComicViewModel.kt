package com.example.read5.viewmodel.comic
import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.read5.utils.comic.ComicLoader
import com.example.read5.utils.comic.ImageStitcher
import com.example.read5.utils.comic.ZipComicLoader
import com.example.read5.utils.comic.SafFolderLoad
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
    private lateinit var safOrZipLoader: ComicLoader
    private lateinit var imageStitcher: ImageStitcher


    private lateinit var loadFile: SafFolderLoad

    private var isSaf = false;



    // 初始化 Loader
    suspend fun initLoader(context: Context, path: String) {
        if(path.startsWith("content://")){
            isSaf = true;
        }
        loadFile = SafFolderLoad(context, path)
        val comicPages  = loadFile.loadComic()
        val sortedPages = comicPages.sortedWith(
            compareBy(NaturalOrderComparator.naturalOrderComparator) { it.name }
        )
        if(isSaf){
            safOrZipLoader = SafComicLoader(
                context = context,
                imageFiles = sortedPages
            )
        }else{
            // ✅ 对页面按文件名进行「自然排序」（支持 1.jpg, 2.jpg, ..., 10.jpg）
            safOrZipLoader = ZipComicLoader(
                zipPath = path,
                pageNames = sortedPages,
                cacheDir = File(context.cacheDir, "comic_cache_${path.hashCode()}"),
                scope = viewModelScope
            )
        }
        imageStitcher = ImageStitcher(safOrZipLoader::loadPage)
        loadPage(0)
        Log.d(TAG, " initLoader_success")
    }

    // 加载指定页
    fun loadPage(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            if(isSaf){
                val imageBitmap = imageStitcher.stitchPagesVertically(index, index+3)
                _currentPage.value = imageBitmap
            }else{

                val imageBitmap = imageStitcher.stitchPagesVertically(index, index+3)
                _currentPage.value = imageBitmap
            }
        }
    }


    // 清理缓存
    suspend fun clearCache() {
        safOrZipLoader.clearCache()
    }
}