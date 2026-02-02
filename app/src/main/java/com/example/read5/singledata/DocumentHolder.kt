package com.example.read5.singledata

import com.example.read5.bean.ItemInfo

// PdfDocumentHolder.kt
object DocumentHolder {
    var currentItem: ItemInfo? = null
        private set // 可选：禁止外部直接修改

    fun setCurrentItem(item: ItemInfo) {
        currentItem = item
    }

    fun clear() {
        currentItem = null
    }
}