package com.example.read5.utils.comic

import com.example.read5.bean.VirtualCanvas

object PixelTranslationIndex {
    fun offsetYConvertIndex(offsetY: Int, canvas: VirtualCanvas): Int {
        return run {
            //这数据库存储的是offsetY的偏差，他本身是负数，但是canvas是按照正数排序的，所以转换一下
            val visibleTop = -1 * offsetY
            canvas.pageLayouts
                .indexOfFirst { it.top >= visibleTop }
                .takeIf { it >= 0 }
                ?: (canvas.pageLayouts.size - 1)
        }
    }
    //反向计算
    fun indexConvertOffsetY(pageIndex: Int, canvas: VirtualCanvas): Int {
        // 边界保护
        if (canvas.pageLayouts.isEmpty()) return 0

        val clampedIndex = pageIndex.coerceIn(0, canvas.pageLayouts.size - 1)
        val targetTop = canvas.pageLayouts[clampedIndex].top

        // 因为 offsetY = -visibleTop，而 visibleTop = targetTop
        return -targetTop
    }
}
