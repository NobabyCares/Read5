package com.example.read5.viewmodel.iteminfo

sealed interface SearchItemDataSource {
    data class searchByCategory(val categoryId: Long) : SearchItemDataSource
    data class searchById(val id: List<Long>) : SearchItemDataSource
    data class searchByName(val query: String) : SearchItemDataSource
}