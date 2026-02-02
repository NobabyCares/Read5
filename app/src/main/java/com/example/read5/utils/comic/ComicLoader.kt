package com.example.read5.utils.comic

import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap
import com.example.read5.bean.ComicPage

interface ComicLoader {
    suspend fun loadPage(index: Int): ImageBitmap?
    suspend fun clearCache()
}