package com.example.read5.utils.comic

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.read5.bean.ComicPage
import com.example.read5.bean.PageLayout
import com.example.read5.bean.VirtualCanvas
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipFile


object BuilderVirtualCanvas {

    /*
        这里没必要构建出class, 因为这个函数基本上只会被使用一次, 构建类多余
    */
//构建虚拟页面zip格式
    fun builderVirtualCanvas(pages: List<ComicPage>): VirtualCanvas {
        var cumulativeTop = 0
        val layouts = mutableListOf<PageLayout>()

        // 遍历时获取索引位置
        pages.forEachIndexed { index, item ->

            layouts.add(
                PageLayout(
                    index = index,
                    top = cumulativeTop,
                    width = item.width,
                    height = item.height  // ← 不再是 0！
                )
            )

            cumulativeTop += item.height // 下一页的 top = 当前页 bottom
        }

        val totalWidth = layouts.maxOfOrNull { it.width } ?: 0
        val totalHeight = cumulativeTop // 所有页高度累加

        return VirtualCanvas(
            totalWidth = totalWidth,
            totalHeight = totalHeight,
            pageLayouts = layouts
        )
    }



}