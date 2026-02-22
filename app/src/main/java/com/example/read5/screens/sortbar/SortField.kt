package com.example.read5.screens.sortbar

import com.example.read5.global.GlobalSettings


enum class SortField {
    NAME,
    LAST_READ_TIME,
    READ_PROGRESS,
    TOTAL_READ_TIME,
    FILE_SIZE,
    CREATE_TIME
}
sealed class SortOption(
    val label: String,
    val field: SortField,
    val key: Int
) {
    object Name : SortOption("名称", SortField.NAME, 1)
    object LastReadTime : SortOption("上次阅读", SortField.LAST_READ_TIME, 2)
    object Progress : SortOption("进度", SortField.READ_PROGRESS, 3)
    object TotalReadTime : SortOption("总时长", SortField.TOTAL_READ_TIME, 4)
    object FileSize : SortOption("文件大小", SortField.FILE_SIZE, 5)
    object CreateTime : SortOption("创建时间", SortField.CREATE_TIME, 6)

    companion object {
        val default: SortOption by lazy { Name }

        // ✅ 修复点 2: 使用 lazy
        val all: List<SortOption> by lazy {
            listOf(
                Name,
                LastReadTime,
                Progress,
                TotalReadTime,
                FileSize,
                CreateTime
            )
        }
    }
}
fun getSortOptions(): SortOption {
    val key = GlobalSettings.getSortType()
    return when (key) {
        SortOption.Name.key -> SortOption.Name
        SortOption.LastReadTime.key -> SortOption.LastReadTime
        SortOption.Progress.key -> SortOption.Progress
        SortOption.TotalReadTime.key -> SortOption.TotalReadTime
        SortOption.FileSize.key -> SortOption.FileSize
        SortOption.CreateTime.key -> SortOption.CreateTime
        else -> SortOption.Name
    }
}