package com.example.read5.utils.coverextractor

import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

object CoverExtractorUitils {

    fun saveCoverBitmap(bitmap: Bitmap, coverFile: File): Boolean {
        return try {
            FileOutputStream(coverFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG , 90, out)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


}