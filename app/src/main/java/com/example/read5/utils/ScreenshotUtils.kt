package com.example.read5.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.ViewRootForInspector
import androidx.core.view.drawToBitmap
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ScreenshotUtils {
    private const val COVER_DIR_NAME = "covers"

    // 截图并保存到私有目录
    suspend fun captureAndSave(
        contentResolver: ContentResolver,
        view: View,
        context: Context
    ): String? {
        return try {
            // 1. 截图
            val bitmap = Bitmap.createBitmap(
                view.width,
                view.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            view.draw(canvas)

            // 2. 保存到私有目录
            saveToPrivateStorage(contentResolver, bitmap, context)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 仅修改这一处：在参数列表末尾添加 context: Context
    private fun saveToPrivateStorage(
        contentResolver: ContentResolver,
        bitmap: Bitmap,
        context: Context  // 👈 新增，放在最后，避免破坏调用顺序
    ): String? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        val fileName = "screenshot_$timeStamp.JPEG"  // 改为 WebP

        // 保存到私有目录: /Android/data/包名/files/cover/
        val coverDir = File(
            context.getExternalFilesDir(null) ?: context.filesDir,
            COVER_DIR_NAME
        )
        coverDir.mkdirs()

        val file = File(coverDir, fileName)

        return try {
            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            }
            "图片已保存: ${file.absolutePath}"
        } catch (e: Exception) {
            e.printStackTrace()
            "保存失败"
        }
    }
}