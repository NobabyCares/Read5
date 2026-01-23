package com.example.read5.screens.pdfview

import com.github.barteksc.pdfviewer.PDFView
import com.shockwave.pdfium.PdfDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// 扩展函数：跳转到指定页面
fun PDFView.goToPage(page: Int) {
    this.jumpTo(page, true)
}

// 扩展函数：获取PDF文档信息
suspend fun PDFView.getDocumentInfo(): PdfDocument.Meta? {
    return withContext(Dispatchers.IO) {
        try {
            this@getDocumentInfo.documentMeta
        } catch (e: Exception) {
            null
        }
    }
}