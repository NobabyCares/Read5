package com.example.read5.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.PixelCopy
import android.view.View
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.ViewRootForInspector
import androidx.core.view.drawToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ScreenshotUtils {
    val TAG = "ScreenshotUtils"

    private const val COVER_DIR_NAME = "covers"

    private  var COVER_FILE_NAME = ""


    /**
     * 尝试多种截图方法（移除废弃 API）
     */
     suspend fun tryCaptureScreenshot(
        context: Context,
        view: View,
        name: String = "screenshot_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpeg"
    ): Pair<Boolean, String> = withContext(Dispatchers.Main) {
        COVER_FILE_NAME = name
        // 方法1: PixelCopy（Android 8.0+，官方推荐）
        val activity = context as? Activity
        if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val result = captureWithPixelCopy(activity, view, context)
                return@withContext Pair(true, result)
            } catch (e: Exception) {
                // 继续尝试其他方法
                Log.e(TAG, "PixelCopy 失败: ${e.message}")
            }
        }

        // 方法2: 安全的 Canvas 绘制（临时关闭硬件加速）
        try {
            val result = captureWithSafeCanvas(view, context)
            return@withContext Pair(true, result)
        } catch (e: Exception) {
            return@withContext Pair(false, "截图失败: ${e.message}")
        }
    }

    /**
     * PixelCopy 方法（保持不变，官方安全方案）
     */
    @SuppressLint("NewApi")
    private suspend fun captureWithPixelCopy(
        activity: Activity,
        view: View,
        context: Context
    ): String = suspendCancellableCoroutine { continuation ->
        try {
            val bitmap = Bitmap.createBitmap(
                view.width,
                view.height,
                Bitmap.Config.ARGB_8888
            )
            val location = IntArray(2)
            view.getLocationInWindow(location)
            val srcRect = android.graphics.Rect(
                location[0],
                location[1],
                location[0] + view.width,
                location[1] + view.height
            )

            PixelCopy.request(
                activity.window,
                srcRect,
                bitmap,
                { copyResult ->
                    if (copyResult == PixelCopy.SUCCESS) {
                        val result = saveScreenshot(context, bitmap)
                        bitmap.recycle()
                        continuation.resumeWith(Result.success(result))
                    } else {
                        bitmap.recycle()
                        continuation.resumeWith(Result.success("PixelCopy 错误: $copyResult"))
                    }
                },
                android.os.Handler(android.os.Looper.getMainLooper())
            )
        } catch (e: Exception) {
            continuation.resumeWith(Result.failure(e))
        }
    }

    /**
     * 安全的 Canvas 绘制（替代废弃的 DrawingCache）
     * 通过临时关闭硬件加速确保兼容性
     */
    private fun captureWithSafeCanvas(view: View, context: Context): String {
        // 保存原始 layer type
        val originalLayerType = view.layerType
        // 临时切换到软件渲染
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        val bitmap = Bitmap.createBitmap(
            view.width,
            view.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        // 可选：设置背景色（避免透明区域）
        canvas.drawColor(android.graphics.Color.WHITE)
        view.draw(canvas)

        // 恢复原始 layer type
        view.setLayerType(originalLayerType, null)

        val result = saveScreenshot(context, bitmap)
        bitmap.recycle()
        return result
    }

    /**
     * 保存截图到私有目录（保持不变）
     */
    private fun saveScreenshot(context: Context, bitmap: Bitmap,): String {
        val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val filesDir = context.getExternalFilesDir(null) ?: throw IllegalStateException("无法访问私有目录")
        val screenshotsDir = File(filesDir, COVER_DIR_NAME).apply { if (!exists()) mkdirs() }
        val file = File(screenshotsDir, COVER_FILE_NAME)
        FileOutputStream(file).use { fos ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, fos)
        }
        return "截图保存到: ${file.absolutePath}"
    }
}