package com.example.read5.viewmodel.comic
import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.copyAndClose
import com.example.read5.bean.ComicPage
import com.example.read5.bean.ItemInfo
import com.example.read5.bean.ItemKey
import com.example.read5.bean.VirtualCanvas
import com.example.read5.cache.ComicPageCache
import com.example.read5.repository.iteminfo.ItemInfoRepository
import com.example.read5.utils.comic.BuilderVirtualCanvas
import com.example.read5.utils.comic.ComicLoader
import com.example.read5.utils.comic.ComicLoaderFolder
import com.example.read5.utils.comic.ComicLoaderZip
import com.example.read5.utils.comic.ZipOrFolderLoad
import com.example.read5.viewmodel.iteminfo.UpdateItemInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ComicViewModel @Inject constructor(
    private val itemInfoRepository: ItemInfoRepository
) : ViewModel() {

    val TAG = "ComicViewModel"


    // ✅ 直接缓存 <index, bitmap>，用 immutable map 触发重组
    private val _pageCache = MutableStateFlow<Map<Int, ImageBitmap>>(emptyMap())
    val pageCache: StateFlow<Map<Int, ImageBitmap>> = _pageCache
    //这里是
    private val _viewportEvents = MutableSharedFlow<ViewportEvent>(replay = 0, extraBufferCapacity = 5)
    //    key,用于数据库更新，因为数据库是联合主键
    var key: ItemKey? = null
    // 在 initLoader 成功后启动,定时保存任务
    private var saveJob: Job? = null

    private var currentOffsetY: Int = 0



    // 改为普通属性（或直接不存）
    private var virtualCanvas: VirtualCanvas? = null
    // 内部 LazyZipPageLoader 实例
    private  var folderOrZipLoader: ComicLoader? = null
//    加载图片
    private  var loadFile: ZipOrFolderLoad? = null

    // 2. 再初始化缓存，并传入回调
    // 先创建缓存实例，再设置回调（避免初始化时自引用）
    private val comicPageCache: ComicPageCache = run {
        val cache = ComicPageCache(maxPages = ComicPageCache.MAX_SIZE_IN_PIXELS)
        cache.onChange = {
            viewModelScope.launch(Dispatchers.Main) {
                _pageCache.value = cache.snapshot() // 👈 用局部变量 `cache`
            }
        }
        cache
    }

    init {
        // Start collecting viewport events in a single place.
        viewModelScope.launch {
            _viewportEvents
                .conflate() // Crucial: Drop intermediate scroll events, only process the latest.
                .collect { event ->
                    when (event) {
                        is ViewportEvent.Scroll -> {
                            preloadPages(event.offsetY, event.currentCanvasHeight)
                        }
                        is ViewportEvent.Slide -> {
                            // 👇 触发加载
                            loadPage(event.index)
                        }
                    }
                }
        }
    }

    // 初始化 Loader
    suspend fun initLoader(context: Context, itemInfo: ItemInfo): VirtualCanvas?{
        //把需要的信息都获取了,路径, 和key
        val path = itemInfo.path
        key = ItemKey(androidId = itemInfo.androidId, path = itemInfo.path, hash = itemInfo.hash)
        //获取所有图片信息,名称等
        loadFile = ZipOrFolderLoad(context, path)
        val sortedPages = loadFile?.loadComic()?: return null
        //获取到漫画加载器,是zip还是文件夹
        loadComicLoader(context, path, sortedPages)
        //加载虚拟画布
        virtualCanvas = BuilderVirtualCanvas.builderVirtualCanvas(sortedPages)
        // ✅ 立即预加载第一页及下一页
        viewModelScope.launch(Dispatchers.IO) {
            preloadPages(itemInfo.currentPage.toFloat())
        }
        // 👇 激活 saveJob
        onViewportScrolled(itemInfo.currentPage.toFloat(), 2000)
        startAutoSave()
        return virtualCanvas
    }

    // 加载指定页（外部调用）
    private fun loadPage(index: Int) {
        if (comicPageCache.contains(index)) return

        viewModelScope.launch(Dispatchers.IO) {
            folderOrZipLoader?.loadPage(index)?.let { bitmap ->
                comicPageCache.put(index, bitmap)
            }
        }
    }


    // 新增函数：预加载附近页面
    private fun preloadPages(currentOffsetY: Float, viewportHeight: Int = 2000) {
        val canvas = virtualCanvas ?: return // 👈 安全退出
        val totalPages = canvas.pageLayouts.size

        // ✅ 1. 使用真实的屏幕高度（应从 UI 传入，或通过其他方式获取）
        val screenHeight = viewportHeight // 这里应该是一个参数，比如从 Composable 传入

        val visibleTop = -currentOffsetY.toInt()
        val visibleBottom = visibleTop + screenHeight

        // ✅ 2. 找出可见页面
        val basePages = canvas.pageLayouts
            .filter { page ->
                page.bottom > visibleTop && page.top < visibleBottom
            }
            .map { it.index }

        // ✅ 3. 安全地扩展范围
        val pagesToLoad = basePages.flatMap { idx ->
            listOfNotNull(
                if (idx > 0) idx - 1 else null, // 前一页
                idx,                            // 当前页
                if (idx < totalPages-1 ) idx + 1 else null // 后一页 (安全!)
            )
        }.distinct()

        // ✅ 4. 并发加载（可选，如果你的 loadPage 是 suspend 函数）
        pagesToLoad.forEach { index ->
            loadPage(index) // 假设内部已处理去重和线程
        }
    }


    private fun loadComicLoader(context: Context, path: String, sortedPages: List<ComicPage>){
        folderOrZipLoader = if(path.substringAfterLast( ".", "") == "zip"){
            ComicLoaderZip(
                zipPath = path,
                pageNames = sortedPages,
                cacheDir = File(context.cacheDir, "comic_cache_${path.hashCode()}"),
                scope = viewModelScope
            )
        }else{
            ComicLoaderFolder(
                path = path,
                pageNames = sortedPages,
            )
        }
    }


    override fun onCleared() {
        _pageCache.value = emptyMap()
        comicPageCache.clear()
        virtualCanvas = null
        folderOrZipLoader = null
        loadFile = null
    }
    //监听滚动
    fun onViewportScrolled(offsetY: Float, currentCanvasHeight: Int) {
        currentOffsetY = offsetY.toInt()
        viewModelScope.launch {
            _viewportEvents.emit(ViewportEvent.Scroll(offsetY, currentCanvasHeight))
        }
    }

    fun onViewportSlide(index: Int){
        viewModelScope.launch {
            _viewportEvents.emit(ViewportEvent.Slide(index))
        }
    }

    fun updateCurrentPage(currentPage: Int) {
        currentOffsetY = currentPage
    }


    //自动保存的定时任务
    private fun startAutoSave() {
        key ?: return // 👈 如果 key 还没设置，直接退出
        saveJob?.cancel()
        saveJob = _viewportEvents
            .map { Unit }
            .sample(5000) // 每5秒
            .onEach { offsetY ->
                key?.let { key ->
                    // 调用你的数据库更新方法
                    itemInfoRepository.updateByCurrentPage(currentPage = currentOffsetY, key = key)
                    itemInfoRepository.updateByLastReadTime(key = key, System.currentTimeMillis())
                }
            }
            .launchIn(viewModelScope)
    }
}