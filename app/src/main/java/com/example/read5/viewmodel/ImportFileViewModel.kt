package com.example.read5.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.read5.bean.ItemInfo
import com.example.read5.bean.StoreHouse
import com.example.read5.global.GlobalSettings
import com.example.read5.repository.StoreHouseRepository
import com.example.read5.repository.iteminfo.ItemInfoRepository
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
    private val storeHouseRepository: StoreHouseRepository, // ✅ 注入 Repository
    private val itemInfoRepository: ItemInfoRepository
) : ViewModel() {
    @SuppressLint("NewApi")
    suspend fun importStoreHouse(
        context: Context,
        name: String,
        type: String,
        contents: List<String>
    ): Long = withContext(Dispatchers.IO) {
        val TAG = "ImportFileViewModel"
        val storeHouse = StoreHouse(name = name, type = type, count = 0, lastUpdateTime = System.currentTimeMillis())
        val id = storeHouseRepository.insert(storeHouse)

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
            val flag: LongArray = itemInfoRepository.insert(allBooks)
        }

        storeHouseRepository.updateByCount(id, allBooks.size.toLong())

        GlobalSettings.setRecentStoreHouse(id)
        GlobalSettings.setitemCount(allBooks.size.toLong())

        return@withContext id
    }
}