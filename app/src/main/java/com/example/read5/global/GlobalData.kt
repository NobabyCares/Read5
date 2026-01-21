package com.example.read5.global


import android.content.Context
import android.provider.Settings

object GlobalData {

    // 私有可变上下文引用（仅用于初始化）
    private var initContext: Context? = null


    // 延迟初始化，线程安全
    val androidId: String by lazy {
        initContext?.let { ctx ->
            Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID)
        } ?: "unknown"
    }


    /**
     * 在 Application.onCreate() 中调用一次
     */
    fun initialize(context: Context) {
        // 只初始化一次，避免内存泄漏（用 applicationContext）
        initContext = context.applicationContext
    }
}