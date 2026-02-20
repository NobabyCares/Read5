package com.example.read5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.read5.global.DeviceIdentification
import com.example.read5.global.GlobalSettings
import com.example.read5.global.SimplePasswordManager
import com.example.read5.screens.MainBookApp
import com.example.read5.screens.auth.SimplePasswordScreen
import com.example.read5.ui.theme.Read5Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var isUnlocked by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AndroidId初始化
        DeviceIdentification.initialize(this)

        // 全局设置初始化
        GlobalSettings.init(this)

        setContent {
            Read5Theme {
                if (isUnlocked) {
                    MainBookApp()
                } else {
                    SimplePasswordScreen(
                        onSuccess = {
                            SimplePasswordManager.recordUnlock(this@MainActivity)
                            isUnlocked = true
                        },
                        onCancel = {
                            finish()
                        }
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // 应用切到后台时锁定
        isUnlocked = false
    }

    override fun onResume() {
        super.onResume()
        // 回到前台时，保持锁定状态，强制重新输入密码
        // 不需要调用 checkPasswordStatus，直接保持 false
    }
}