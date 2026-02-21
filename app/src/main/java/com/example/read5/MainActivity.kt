package com.example.read5

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.read5.global.DeviceIdentification
import com.example.read5.global.GlobalSettings
import com.example.read5.global.SimplePasswordManager
import com.example.read5.screens.MainBookApp
import com.example.read5.screens.auth.SimplePasswordScreen
import com.example.read5.screens.readview.comic.SimpleProgressBar
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
//                    SimpleProgressBar()

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


@Composable
fun SliderDemo() {
    var progress by remember { mutableStateOf(0.5f) }
    val TAG = "SliderDemo"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "当前进度: ${String.format("%.2f", progress)}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 👇 核心：Slider 用法
        Slider(
            value = progress,                          // 当前值（必须是 State）
            onValueChange = { newValue ->
                progress = newValue                    // 更新状态
                Log.d(TAG, "Dragging: $newValue")      // 拖动中日志
            },
            onValueChangeFinished = {
                Log.d(TAG, "Drag finished at: $progress") // 拖动结束日志
            },
            valueRange = 0f..1f,                       // 可选：限制范围
            steps = 9,                                 // 可选：分 10 段（9 个刻度）
            modifier = Modifier.fillMaxWidth()
        )
    }
}