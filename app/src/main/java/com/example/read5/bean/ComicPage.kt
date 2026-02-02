package com.example.read5.bean

import android.icu.text.ListFormatter.Width
import android.net.Uri

// ========== 数据加载逻辑（保持不变）==========
data class ComicPage(
    val name: String,
    val width: Int,
    val height: Int,
    val uri: Uri? = null)