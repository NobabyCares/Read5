package com.example.read5.screens.myview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.read5.bean.Feature
import com.example.read5.viewmodel.iteminfo.MyViewOfItemInfoViewModel

@Composable
fun HomeScreen(
    navHostController: NavHostController,
    myViewOfItemInfoViewModel: MyViewOfItemInfoViewModel,
    modifier: Modifier = Modifier,
) {
    // ✅ 配置式声明所有功能（清晰、易读、易改）
    val features = listOf(
        Feature(1,  "隐藏内容", Icons.Default.Home, FeatureAction.ShowHiddenItems),
        Feature(2,  "收藏夹", Icons.Default.Star, FeatureAction.ShowIsCollectItems),
        Feature(3,"设置密码", Icons.Default.Star, FeatureAction.SimplePassword),
        Feature(4, "数据库查询", Icons.Default.Star, FeatureAction.DatabaseQuery),
    )
    // 统一处理点击
    fun handleFeatureClick(action: FeatureAction) {
        when (action) {
            is FeatureAction.ShowHiddenItems -> {
                myViewOfItemInfoViewModel.searchByIsShow() // 触发数据加载
                navHostController.navigate("my_view/item_not_show")
            }
            is FeatureAction.ShowIsCollectItems -> {
                myViewOfItemInfoViewModel.searchByIsCollect() // 触发数据加载
                navHostController.navigate("my_view/is_collect_item")
            }
            is FeatureAction.SimplePassword -> {
                navHostController.navigate("my_view/simple_password")
            }
            is FeatureAction.DatabaseQuery ->{
                navHostController.navigate("my_view/all_data_search")
            }
            is FeatureAction.NavigateTo -> {
                navHostController.navigate(action.route)
            }
            is FeatureAction.OpenSettings -> {
                navHostController.navigate("settings")
            }

        }
    }


    FeatureListScreen(
        features = features,
        onFeatureClick = { handleFeatureClick(it.action) },
        modifier = modifier
    )
}