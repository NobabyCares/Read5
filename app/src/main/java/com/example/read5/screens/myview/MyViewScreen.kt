package com.example.read5.screens.myview

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.read5.R // 如果你有图标资源，可替换；否则用默认图标
import com.example.read5.bean.Feature
import com.example.read5.viewmodel.iteminfo.SearchItemInfo


@Composable
fun MyViewScreen(
    navHostController: NavHostController,
    searchItemInfo: SearchItemInfo,
    modifier: Modifier = Modifier,
) {
    // ✅ 配置式声明所有功能（清晰、易读、易改）
    val features = listOf(
        Feature("hidden", "隐藏内容", Icons.Default.Home, FeatureAction.ShowHiddenItems),
        Feature("favorites", "收藏夹", Icons.Default.Star, FeatureAction.ShowIsCollectItems),
        //供参考
        /*Feature("history", "阅读历史", Icons.Default.Home, FeatureAction.NavigateTo("reading_history")),
        Feature("tags", "标签管理", Icons.Default.Home, FeatureAction.NavigateTo("tags")),
        Feature("settings", "设置", Icons.Default.Settings, FeatureAction.OpenSettings),*/
    )

    // 统一处理点击
    fun handleFeatureClick(action: FeatureAction) {
        when (action) {
            is FeatureAction.ShowHiddenItems -> {
                searchItemInfo.searchByIsShow() // 触发数据加载
                navHostController.navigate("item_not_show")
            }
            is FeatureAction.ShowIsCollectItems -> {
                searchItemInfo.searchByIsCollect() // 触发数据加载
                navHostController.navigate("item_not_show")
            }
            is FeatureAction.OpenSettings -> {
                // TODO: 打开设置
            }

            is FeatureAction.NavigateTo -> TODO()
        }
    }

    FeatureListScreen(
        features = features,
        onFeatureClick = { handleFeatureClick(it.action) },
        modifier = modifier
    )
}