package com.example.read5.bean

import androidx.compose.ui.graphics.vector.ImageVector
import com.example.read5.screens.myview.FeatureAction


//这里是设置选项
data class Feature(
    val id: Long,
    val title: String,
    val icon: ImageVector? = null, // 可选图标
    val action: FeatureAction
)