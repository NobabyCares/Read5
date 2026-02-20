package com.example.read5.viewmodel.iteminfo

import com.example.read5.screens.sortbar.SortField
import com.example.read5.screens.sortbar.SortOption

sealed interface SearchItemDataSource {
    data class searchByCategory(val categoryId: Long) : SearchItemDataSource
    data class searchById(val id: List<Long>) : SearchItemDataSource
    data class searchByName(val query: String) : SearchItemDataSource
    data class searchByIsShow(val isShow: Boolean) : SearchItemDataSource
    data object searchByIsCollect : SearchItemDataSource
    data class sortByField(val name: SortField, val ascending: Boolean, val category: Long ) : SearchItemDataSource
}