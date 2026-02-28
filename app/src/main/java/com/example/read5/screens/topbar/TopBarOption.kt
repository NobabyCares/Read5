package com.example.read5.screens.topbar

import com.example.read5.screens.sortbar.SortOption
import com.example.read5.screens.sortbar.SortOption.CreateTime
import com.example.read5.screens.sortbar.SortOption.FileSize
import com.example.read5.screens.sortbar.SortOption.LastReadTime
import com.example.read5.screens.sortbar.SortOption.Name
import com.example.read5.screens.sortbar.SortOption.Progress
import com.example.read5.screens.sortbar.SortOption.TotalReadTime

sealed class TopBarOption(
    val title: String,
    val key: Int
){

    object Home : TopBarOption("首页", 1)
    object History : TopBarOption("历史记录", 2)
    object BookDesk : TopBarOption("书架", 3)
    object ComicType : TopBarOption("分类", 4)


    companion object {
        val default: TopBarOption by lazy { Home }

        // ✅ 修复点 2: 使用 lazy
        val all: List<TopBarOption> by lazy {
            listOf(
                Home,
                History,
                BookDesk,
                ComicType
            )
        }
    }
}

fun getOptionRoute(option: TopBarOption): String {
    return when (option.key){
        1 -> {
            "bookshelf/bookdesk"
        }
        2 -> "bookshelf/history"
        3 -> "bookshelf/bookshelf"
        4 -> "bookshelf/comicType"
        else -> {"bookshelf/bookdesk"}
    }
}