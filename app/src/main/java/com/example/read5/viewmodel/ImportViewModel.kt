package com.example.read5.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.read5.bean.ItemInfo
import com.example.read5.bean.StoreHouse
import com.example.read5.utils.FileScanner
import com.example.read5.repository.ItemInfoRepository
import com.example.read5.repository.StoreHouseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val storeHouseViewModel: StoreHouseRepository, // ✅ 注入 Repository
    private val itemInfoViewModel: ItemInfoRepository        // ✅ 注入 Repository
) : ViewModel() {

    suspend fun importStoreHouse(
        context: Context,
        name: String,
        type: String,
        uriPath: Uri?
    ): Long = withContext(Dispatchers.IO) {
        val storeHouse = StoreHouse(name = name, type = type)
        val id = storeHouseViewModel.insert(storeHouse)

        val allBooks = mutableListOf<ItemInfo>()

        // SAF 漫画扫描（已在 IO 线程）
        if (uriPath != null) {
            allBooks.addAll(FileScanner.findComicBooksBySaf(context, uriPath, id))
        }

        // 传统文件扫描
        val extensions = type.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()

        if (extensions.isNotEmpty()) {
            allBooks.addAll(FileScanner.findBooksByExtensions(extensions, id))
        }

        if (allBooks.isNotEmpty()) {
            itemInfoViewModel.insert(allBooks) // 假设它是 suspend 函数
        }

        return@withContext id
    }
}