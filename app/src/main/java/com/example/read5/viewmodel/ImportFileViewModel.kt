package com.example.read5.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils.split
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
    val TAG = "ImportFileViewModel"
    @SuppressLint("NewApi")
    suspend fun importStoreHouse(
        context: Context,
        name: String,
        type: String,
        folderPath: List<String>
    ): Long = withContext(Dispatchers.IO) {


        // 将 List<String> 转换为字符串，使用逗号加空格分隔

        val storeHouse = StoreHouse(
            name = name, type = type, count = 0,
            lastUpdateTime = System.currentTimeMillis(),
            folderPath = folderPath.joinToString(", ")
        )
        val id = storeHouseRepository.insert(storeHouse)

        var allBooks = mutableListOf<ItemInfo>()

        // 传统文件扫描
        val extensions = type.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()

        if (extensions.isNotEmpty() || folderPath.isNotEmpty()) {
            allBooks = FileScanner.loadFile(extensions, id,  folderPath).toMutableList()
        }

        if (allBooks.isNotEmpty()) {
            val flag: LongArray = itemInfoRepository.insert(allBooks)
        }

        storeHouseRepository.updateByCount(id, allBooks.size.toLong())

        GlobalSettings.setRecentStoreHouse(id)
        GlobalSettings.setitemCount(allBooks.size.toLong())

        return@withContext id
    }


    @SuppressLint("NewApi")
    suspend fun updateStoreHouse(
        context: Context,
        storeHouse: StoreHouse,
    ): Long = withContext(Dispatchers.IO) {
        val id = storeHouse.id
        val folderPath = decodeFolderPaths(storeHouse.folderPath)

        var allBooks = mutableListOf<ItemInfo>()

        // 传统文件扫描
        val extensions = storeHouse.type.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()

        if (extensions.isNotEmpty() || folderPath.isNotEmpty()) {
            allBooks = FileScanner.loadFile(extensions, id,  folderPath).toMutableList()
        }

        if (allBooks.isNotEmpty()) {
            val flag: LongArray = itemInfoRepository.insert(allBooks)
            Log.d(TAG, "flag: ${flag.joinToString(", ")}")
        }

        storeHouseRepository.updateByCount(id, allBooks.size.toLong())

        GlobalSettings.setRecentStoreHouse(id)
        GlobalSettings.setitemCount(allBooks.size.toLong())

        return@withContext id
    }
}

// 假设存储的格式是 "path1, path2, path3"
fun decodeFolderPaths(encoded: String): List<String> {
    return if (encoded.isNotEmpty()) {
        encoded.split(", ").map { it.trim() }
    } else {
        emptyList()
    }
}