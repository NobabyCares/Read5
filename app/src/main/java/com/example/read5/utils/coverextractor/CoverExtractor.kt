package com.example.read5.utils.coverextractor

import android.graphics.Bitmap
import java.io.File

interface CoverExtractor {
    suspend fun extractCover(path: String, coverFile: File): Boolean
}