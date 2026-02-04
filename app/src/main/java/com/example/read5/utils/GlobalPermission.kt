package com.example.read5.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.Settings
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
