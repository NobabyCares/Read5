package com.example.read5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.read5.global.DeviceIdentification
import com.example.read5.global.GlobalSettings
import com.example.read5.global.SimplePasswordManager
import com.example.read5.screens.MainBookApp
import com.example.read5.screens.auth.SimplePasswordScreen
import com.example.read5.ui.theme.Read5Theme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // 是否显示密码覆盖层
    private var showPasswordOverlay by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化密码管理器
        SimplePasswordManager.init(this)

        // AndroidId初始化
        DeviceIdentification.initialize(this)

        // 全局设置初始化
        GlobalSettings.init(this)

        setContent {
            Read5Theme {
                // 主应用内容始终在后台运行
                Box(modifier = Modifier.fillMaxSize()) {
                    // 主应用界面（始终存在，状态不会丢失）
                    MainBookApp()

                    // 密码覆盖层（需要时显示在上面）
                    if (showPasswordOverlay) {
                        SimplePasswordScreen(
                            onSuccess = {
                                SimplePasswordManager.recordUnlock(this@MainActivity)
                                showPasswordOverlay = false
                            },
                            onCancel = {
                                finish()
                            }
                        )
                    }
                }
            }
        }

        // 初始检查是否需要显示密码
        checkPasswordStatus()
    }

    private fun checkPasswordStatus() {
        lifecycleScope.launch {
            // 检查是否需要显示密码覆盖层
            val shouldLock = SimplePasswordManager.shouldLock(this@MainActivity)
            showPasswordOverlay = shouldLock
        }
    }

    override fun onStart() {
        super.onStart()
        // 每次启动时检查是否需要锁定
        checkPasswordStatus()
    }

    override fun onPause() {
        super.onPause()
        showPasswordOverlay = true
    }

    override fun onResume() {
        super.onResume()
        showPasswordOverlay = true
    }
}