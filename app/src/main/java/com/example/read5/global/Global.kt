package com.example.read5.global

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.Settings
import com.example.read5.bean.Config
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File


// 检查是否已有权限
fun hasAllFilesPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        // Android 10 及以下，只需 READ_EXTERNAL_STORAGE（这里简化处理）
        true
    }
}
// 跳转到系统设置页面请求权限
fun requestAllFilesPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        context.startActivity(intent)
    }
}
//json 文件管理
object ConfigManager {
    private const val CONFIG_FILE_NAME = "config.json"

    // 保存配置
    fun saveConfig(context: Context, config: Config) {
        try {
            val jsonString = Json.encodeToString(config)
            val file = File(context.filesDir, CONFIG_FILE_NAME)
            file.writeText(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 读取配置（带默认值）
    fun loadConfig(context: Context): Config {
        return try {
            val file = File(context.filesDir, CONFIG_FILE_NAME)
            if (file.exists()) {
                val jsonString = file.readText()
                Json.decodeFromString(jsonString)
            } else {
                Config() // 返回默认配置
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Config() // 出错时返回默认
        }
    }
}