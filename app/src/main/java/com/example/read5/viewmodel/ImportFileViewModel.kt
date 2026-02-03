package com.example.read5.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.read5.bean.ItemInfo
import com.example.read5.bean.StoreHouse
import com.example.read5.repository.ItemInfoRepository
import com.example.read5.repository.StoreHouseRepository
import com.example.read5.utils.FileScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


/*
* 这里进行插入操作，与 StoreHouseInputDialog 结合操作
*
* */
@HiltViewModel
class ImportFileViewModel @Inject constructor(
    private val storeHouseViewModel: StoreHouseRepository, // ✅ 注入 Repository
    private val itemInfoViewModel: ItemInfoRepository        // ✅ 注入 Repository
) : ViewModel() {
    suspend fun importStoreHouse(
        context: Context,
        name: String,
        type: String,
        contents: List<String>
    ): Long = withContext(Dispatchers.IO) {
        val storeHouse = StoreHouse(name = name, type = type)
        val id = storeHouseViewModel.insert(storeHouse)

        var allBooks = mutableListOf<ItemInfo>()

        // 传统文件扫描
        val extensions = type.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()

        if (extensions.isNotEmpty() || contents.isNotEmpty()) {
            allBooks = FileScanner.loadFile(extensions, id,  contents).toMutableList()
        }

        if (allBooks.isNotEmpty()) {
            itemInfoViewModel.insert(allBooks) // 假设它是 suspend 函数
        }

        return@withContext id
    }
}