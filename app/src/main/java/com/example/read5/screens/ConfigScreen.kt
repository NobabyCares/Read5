package com.example.read5.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.read5.bean.Config
import com.example.read5.global.ConfigManager.loadConfig
import com.example.read5.global.ConfigManager.saveConfig

@Composable
fun ConfigScreen() {
    val context = LocalContext.current // 👈 获取 Context

    var config by remember { mutableStateOf(loadConfig(context)) }

    Button(onClick = {
        // 修改配置并保存
        config = Config(recentStoreHouse = 123L, recentItem = listOf(1L, 2L))
        saveConfig(context, config) // 👈 传入 context
    }) {
        Text("保存配置")
    }

    Text("当前书库ID: ${config.recentStoreHouse}")
}