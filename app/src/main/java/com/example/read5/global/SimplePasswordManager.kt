package com.example.read5.global

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File

object SimplePasswordManager {

    private const val CONFIG_FILE_NAME = "app_config.json"
    private const val DEFAULT_PASSWORD = "123" // 默认密码
    private val json = Json { prettyPrint = true }

    // 密码配置数据类
    @Serializable
    data class PasswordConfig(
        var isEnabled: Boolean = true, // 默认为 true，直接启用
        var password: String = DEFAULT_PASSWORD, // 默认为 "123"
        var lastUnlockTime: Long = 0L,
        var autoLockMinutes: Int = 5 // 自动锁定时间（分钟）
    )

    private var currentConfig: PasswordConfig? = null

    // 获取配置文件
    private fun getConfigFile(context: Context): File {
        return File(context.filesDir, CONFIG_FILE_NAME)
    }

    // 读取配置
    fun getConfig(context: Context): PasswordConfig {
        currentConfig?.let { return it }

        val file = getConfigFile(context)
        return if (file.exists()) {
            try {
                val jsonString = file.readText()
                json.decodeFromString<PasswordConfig>(jsonString)
            } catch (e: Exception) {
                // 如果配置文件损坏，使用默认配置
                PasswordConfig()
            }
        } else {
            // 首次运行，创建默认配置（已启用且密码为123）
            PasswordConfig().also { saveConfig(context, it) }
        }.also { currentConfig = it }
    }

    // 保存配置
    private fun saveConfig(context: Context, config: PasswordConfig) {
        currentConfig = config
        val file = getConfigFile(context)
        val jsonString = json.encodeToString(config)
        file.writeText(jsonString)
    }

    // 设置密码
    fun setPassword(context: Context, password: String) {
        val config = getConfig(context)
        config.password = password
        config.isEnabled = true // 设置密码时自动启用
        saveConfig(context, config)
    }

    // 验证密码
    fun verifyPassword(context: Context, input: String): Boolean {
        val config = getConfig(context)
        // 始终检查密码，因为默认就是启用的
        return config.password == input
    }

    // 禁用密码（注意：这个方法谨慎使用，可能会让用户无法进入）
    fun disablePassword(context: Context) {
        val config = getConfig(context)
        config.isEnabled = false
        config.password = ""
        saveConfig(context, config)
    }

    // 启用密码
    fun enablePassword(context: Context) {
        val config = getConfig(context)
        config.isEnabled = true
        if (config.password.isEmpty()) {
            config.password = DEFAULT_PASSWORD
        }
        saveConfig(context, config)
    }

    // 记录解锁时间
    fun recordUnlock(context: Context) {
        val config = getConfig(context)
        config.lastUnlockTime = System.currentTimeMillis()
        saveConfig(context, config)
    }

    // 检查是否需要重新锁定
    fun shouldLock(context: Context): Boolean {
        val config = getConfig(context)
        if (!config.isEnabled) return false

        // 如果 lastUnlockTime 为 0，表示从未解锁过，需要锁定
        if (config.lastUnlockTime == 0L) return true

        val elapsedMinutes = (System.currentTimeMillis() - config.lastUnlockTime) / (1000 * 60)
        return elapsedMinutes >= config.autoLockMinutes
    }

    // 设置自动锁定时间
    fun setAutoLockMinutes(context: Context, minutes: Int) {
        val config = getConfig(context)
        config.autoLockMinutes = minutes
        saveConfig(context, config)
    }

    // 重置为默认密码
    fun resetToDefaultPassword(context: Context) {
        val config = getConfig(context)
        config.password = DEFAULT_PASSWORD
        config.isEnabled = true
        saveConfig(context, config)
    }

    // 检查是否已设置密码（实际上永远为 true，因为默认就有）
    fun hasPassword(context: Context): Boolean {
        val config = getConfig(context)
        return config.isEnabled && config.password.isNotEmpty()
    }

    // 获取密码状态（用于调试）
    fun getPasswordStatus(context: Context): String {
        val config = getConfig(context)
        return """
            密码启用: ${config.isEnabled}
            密码: ${if (config.password.isNotEmpty()) "已设置 (${config.password})" else "未设置"}
            上次解锁: ${if (config.lastUnlockTime > 0) java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(config.lastUnlockTime)) else "从未"}
            自动锁定: ${config.autoLockMinutes}分钟
        """.trimIndent()
    }
}