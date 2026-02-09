package com.example.read5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.read5.global.DeviceIdentification
import com.example.read5.global.GlobalSettings
import com.example.read5.screens.MainBookApp
import com.example.read5.screens.iteminfo.SimpleGestureDemo
import com.example.read5.ui.theme.Read5Theme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // AndroidId初始化
        DeviceIdentification.initialize(this)
        //全局设置初始化
        GlobalSettings.init(this)
        setContent {
            Read5Theme {
                MainBookApp()
            }
        }
        // ✅ 添加全局截图按钮（仅在需要时显示，比如调试或正式功能）
    }


}

