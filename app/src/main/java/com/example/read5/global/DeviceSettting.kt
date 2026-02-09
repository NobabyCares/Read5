// GlobalSettings.kt
package com.example.read5.global

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

@Serializable
data class Config(
    var history: List<Long> = emptyList(),
    var recentStoreHouse: Long = 1L,
    var scale: Float = 1f
)

object GlobalSettings {
    private lateinit var context: Context
    private val json = Json { ignoreUnknownKeys = true }
    private lateinit var configFile: File

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

    fun getHistory(): List<Long> = read { it.history }
    fun getRecentStoreHouse(): Long = read { it.recentStoreHouse }
    fun getScale(): Float = read { it.scale }

    fun addToHistory(itemId: Long) {
        write { config ->
            // 去重 + 移到最前
            val newHistory = buildList {
                add(itemId)
                addAll(config.history.filter { it != itemId })
                // 可选：限制历史长度（比如最多 20 条）
                if (size > 20) removeAt(20)
            }
            config.history = newHistory
        }
    }

    fun setRecentStoreHouse(storeHouseId: Long) {
        write { it.recentStoreHouse = storeHouseId }
    }

    fun setScale(scale: Float) {
        write { it.scale = scale.coerceIn(0.5f, 5.0f) } // 可选：限制缩放范围
    }

    // 强制立即保存（如退出应用时调用）
    fun forceSave() {
        saveJob?.cancel()
        CoroutineScope(Dispatchers.IO).launch {
            saveToDisk()
        }
    }
}