package com.example.read5.screens.myview


sealed interface FeatureAction {
    data object ShowHiddenItems : FeatureAction
    data object ShowIsCollectItems : FeatureAction
    data class NavigateTo(val route: String) : FeatureAction
    data object OpenSettings : FeatureAction
    // 未来可轻松添加新类型
}