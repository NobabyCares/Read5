package com.example.read5.singledata

import com.example.read5.bean.ItemInfo
import com.example.read5.bean.StoreHouse

// 定义一个“文档状态”的密封接口
sealed interface DocumentState {

    // 状态 1：未初始化（object 表示单例）
    data object Uninitialized : DocumentState

    // 状态 2：已加载（携带数据）
    data class Loaded(val item: ItemInfo) : DocumentState

}

object DocumentHolder {
    private var state: DocumentState = DocumentState.Uninitialized

    fun setCurrentItem(item: ItemInfo) {
        state = DocumentState.Loaded(item)
    }

    fun clear() {
        state = DocumentState.Uninitialized
    }

    // 安全获取非空值（如果未初始化就抛异常）
    fun requireItem(): ItemInfo {
        return when (val s = state) {
            is DocumentState.Loaded -> s.item
            DocumentState.Uninitialized -> error("Document not initialized!")
        }
    }

    // 检查是否已加载
    val isLoaded: Boolean
        get() = state is DocumentState.Loaded
}