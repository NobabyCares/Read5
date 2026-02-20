package com.example.read5.global

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File

object SimplePasswordManager {

    private const val CONFIG_FILE_NAME = "app_config.json"
    private const val DEFAULT_PASSWORD = "123"
    private val json = Json { prettyPrint = true }

    @Serializable
    data class PasswordConfig(
        var isEnabled: Boolean = true,
        var password: String = DEFAULT_PASSWORD,
        var lastUnlockTime: Long = 0L,
        var autoLockMinutes: Int = 5
    )

    private var currentConfig: PasswordConfig? = null

    fun init(context: Context) {
        // 不需要额外初始化
    }

    private fun getConfigFile(context: Context): File {
        return File(context.filesDir, CONFIG_FILE_NAME)
    }

    fun getConfig(context: Context): PasswordConfig {
        currentConfig?.let { return it }

        val file = getConfigFile(context)
        return if (file.exists()) {
            try {
                val jsonString = file.readText()
                json.decodeFromString<PasswordConfig>(jsonString)
            } catch (e: Exception) {
                PasswordConfig()
            }
        } else {
            PasswordConfig().also { saveConfig(context, it) }
        }.also { currentConfig = it }
    }

    private fun saveConfig(context: Context, config: PasswordConfig) {
        currentConfig = config
        val file = getConfigFile(context)
        val jsonString = json.encodeToString(config)
        file.writeText(jsonString)
    }

    fun setPassword(context: Context, password: String) {
        val config = getConfig(context)
        config.password = password
        config.isEnabled = true
        saveConfig(context, config)
    }

    fun verifyPassword(context: Context, input: String): Boolean {
        val config = getConfig(context)
        return config.password == input
    }

    fun disablePassword(context: Context) {
        val config = getConfig(context)
        config.isEnabled = false
        config.password = ""
        saveConfig(context, config)
    }

    fun enablePassword(context: Context) {
        val config = getConfig(context)
        config.isEnabled = true
        if (config.password.isEmpty()) {
            config.password = DEFAULT_PASSWORD
        }
        saveConfig(context, config)
    }

    fun recordUnlock(context: Context) {
        val config = getConfig(context)
        config.lastUnlockTime = System.currentTimeMillis()
        saveConfig(context, config)
    }

    fun shouldLock(context: Context): Boolean {
        val config = getConfig(context)
        if (!config.isEnabled) return false

        // 检查是否超过自动锁定时间
        if (config.lastUnlockTime == 0L) return true
        val elapsedMinutes = (System.currentTimeMillis() - config.lastUnlockTime) / (1000 * 60)
        return elapsedMinutes >= config.autoLockMinutes
    }

    fun setAutoLockMinutes(context: Context, minutes: Int) {
        val config = getConfig(context)
        config.autoLockMinutes = minutes
        saveConfig(context, config)
    }

    fun resetToDefaultPassword(context: Context) {
        val config = getConfig(context)
        config.password = DEFAULT_PASSWORD
        config.isEnabled = true
        saveConfig(context, config)
    }

    fun hasPassword(context: Context): Boolean {
        val config = getConfig(context)
        return config.isEnabled && config.password.isNotEmpty()
    }
}