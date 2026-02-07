package com.example.read5.viewmodel.storehouse

sealed interface ItemDataSource {
    data class searchByCategory(val categoryId: Long) : ItemDataSource
    data class searchById(val id: List<Long>) : ItemDataSource
    data class searchByName(val query: String) : ItemDataSource
}