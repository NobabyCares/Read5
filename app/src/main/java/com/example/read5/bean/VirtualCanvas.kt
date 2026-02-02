package com.example.read5.bean

data class VirtualCanvas(
    val totalWidth: Int,   // 所有页的最大宽度
    val totalHeight: Int,  // 所有页高度累加
    val pageLayouts: List<PageLayout> // 每页的位置信息
)

data class PageLayout(
    val index: Int,   // 第几页（0, 1, 2...）
    val top: Int,     // 这一页从“长图”的哪个 Y 像素开始
    val width: Int,   // 这一页有多宽
    val height: Int   // 这一页有多高
) {
    val bottom get() = top + height  // 结束位置 = 起始 + 高度
}