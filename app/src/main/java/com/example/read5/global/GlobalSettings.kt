// GlobalSettings.kt
package com.example.read5.global

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.read5.bean.ItemInfo
import com.example.read5.screens.sortbar.SortOption
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

@Serializable
data class Config(
    var historyItems: List<ItemInfo> = emptyList(),
    var recentStoreHouse: Long = 1L,
    var scale: Float = 1f,
    var readMode: String = "horizon_comic_view",
    var backgroundColorArgb: Long = 0xFF000000L, // Color.Black 的 ARGB
    var panSmoothing: Float = 1f,
    var sortType: Int = 1,
    var ascOrdesc: Boolean = true,
    var itemCount: Long = 0

)

object GlobalSettings {
    private lateinit var context: Context
    private val json = Json { ignoreUnknownKeys = true }
    private lateinit var configFile: File

    private val MAX_HISTORY_SIZE = 150

    // 内存中的配置（受读写锁保护）
    private var config = Config()
    private val lock = ReentrantReadWriteLock()
    private var saveJob: Job? = null
    private val saveDelayMs = 500L // 防抖 500ms

    fun init(context: Context) {
        if (::context.isInitialized) return
        this.context = context.applicationContext
        configFile = File(context.filesDir, "global_config.json")
        loadFromDisk()
    }

    // ———————— 读操作 ————————
    private inline fun <T> read(block: (Config) -> T): T {
        lock.read { return block(config) }
    }

    // ———————— 写操作 ————————
    private inline fun write(block: (Config) -> Unit) {
        lock.write { block(config) }
        scheduleSave()
    }

    // ———————— 持久化 ————————
    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = CoroutineScope(Dispatchers.IO).launch {
            delay(saveDelayMs)
            saveToDisk()
        }
    }

    private fun loadFromDisk() {
        try {
            if (configFile.exists()) {
                val content = configFile.readText()
                if (content.isNotBlank()) {
                    val loaded = json.decodeFromString(Config.serializer(), content)
                    lock.write { config = loaded }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 文件损坏？保留默认 Config
        }
    }

    private fun saveToDisk() {
        try {
            val jsonText = json.encodeToString(Config.serializer(), config)
            configFile.writeText(jsonText)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ———————— 公共 API ————————
    fun getitemCount(): Long = read { it.itemCount }
    fun setitemCount(itemCount: Long){
        write {
            it.itemCount = itemCount
        }
    }
    fun getSortType(): Int = read { it.sortType }
    fun setSortType(sortType: Int) {
        write {
            it.sortType = sortType
        }
    }
    fun getAscOrdesc(): Boolean = read { it.ascOrdesc }
    fun setAscOrdesc(ascOrdesc: Boolean) {
        write {
            it.ascOrdesc = ascOrdesc
        }
    }
    /**
     * 获取历史记录（最新优先）
     * LinkedHashSet 保持插入顺序，所以最后插入的在最后
     * 反转后就是最新在前
     */
    fun getHistory(): List<ItemInfo> = read {
        config.historyItems
    }
    fun getRecentStoreHouse(): Long = read { it.recentStoreHouse }
    fun getScale(): Float = read { it.scale }

    fun getReadMode(): String = read { it.readMode }

    fun getPanSmoothing(): Float = read { it.panSmoothing }

    // 👇 新增：获取背景色（自动从 Long 转 Color）
    fun getBackgroundColor(): Color = read {
        try {
            Color(it.backgroundColorArgb.toInt()) // Long → Int → Color
        } catch (e: Exception) {
            Color.Black // fallback
        }
    }

    fun setSlidingSpeed(speed: Float) {
        write { it.panSmoothing = speed } // 可选：限制滑动速度范围
    }

    /**
     * 添加项目到历史记录
     * 使用 LinkedHashSet 保持插入顺序 + 自动去重
     */
    fun addToHistory(item: ItemInfo) {
        write { config ->
            // 用 id 作为 key 的 Map
            val map = config.historyItems.associateBy { it.id }.toMutableMap()

            // 放入新记录（自动覆盖相同 id 的）
            map[item.id] = item.copy(lastReadTime = System.currentTimeMillis())

            // 按时间排序并限制数量
            config.historyItems = map.values
                .sortedByDescending { it.lastReadTime }
                .take(MAX_HISTORY_SIZE)
        }
    }

    fun setRecentStoreHouse(storeHouseId: Long) {
        write { it.recentStoreHouse = storeHouseId }
    }

    fun setScale(scale: Float) {
        write { it.scale = scale.coerceIn(0.5f, 5.0f) } // 可选：限制缩放范围
    }

    fun setReadMode(mode: String) {
        write { it.readMode = mode }
    }

    // 👇 新增：设置背景色（Color → Long）
    fun setBackgroundColor(color: Color) {
        write {
            it.backgroundColorArgb = color.toArgb().toLong()
        }
    }

    // 强制立即保存（如退出应用时调用）
    fun forceSave() {
        saveJob?.cancel()
        CoroutineScope(Dispatchers.IO).launch {
            saveToDisk()
        }
    }
}