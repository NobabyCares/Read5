package com.example.read5.screens.sortbar


enum class SortField {
    NAME,
    LAST_READ_TIME,
    READ_PROGRESS,
    TOTAL_READ_TIME
}

// 排序选项（扁平化，直接代表一个可选动作）
sealed class SortOption(
    val label: String,
    val field: SortField,
    val ascending: Boolean,
) {
    object NameAsc : SortOption("名称 (A→Z)", SortField.NAME, true)
    object NameDesc : SortOption("名称 (Z→A)", SortField.NAME, false)

    object TimeAsc : SortOption("阅读时间 (旧→新)", SortField.LAST_READ_TIME, true)
    object TimeDesc : SortOption("阅读时间 (新→旧)", SortField.LAST_READ_TIME, false)

    object ProgressDesc : SortOption("阅读进度 (高→低)", SortField.READ_PROGRESS, false)
    object ProgressAsc : SortOption("阅读进度 (低→高)", SortField.READ_PROGRESS, true)

    object TotalReadTimeAsc : SortOption("阅读时间 (低→高)", SortField.TOTAL_READ_TIME, true)
    object TotalReadTimeDesc : SortOption("阅读时间 (高→低)", SortField.TOTAL_READ_TIME, false)

    companion object {
        val default = NameAsc
        val all = listOf(NameAsc, NameDesc, TimeDesc, TimeAsc, ProgressDesc, ProgressAsc, TotalReadTimeDesc, TotalReadTimeAsc)
    }
}

