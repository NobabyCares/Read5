package com.example.read5.bean

/*
* 虚拟画板, 用于计算当前可见的页面范围
*
* */
data class VisibleRegion(
    val virtualTop: Int,
    val virtualBottom: Int,
    val scale: Float
)