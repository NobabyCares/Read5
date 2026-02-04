package com.example.read5.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.Query
import com.example.read5.bean.ItemInfo
import com.example.read5.bean.ItemKey
import com.example.read5.bean.StoreHouse
import com.example.read5.dao.ItemInfoDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface ItemInfoRepository {
    // ❌ 移除旧的 getAll()、searchByCategory()
    // ✅ 新增：返回可参数化的分页流
    fun getItemsPager(categoryId: Long): Flow<PagingData<ItemInfo>>

     fun searchByName(name: String): Flow<PagingData<ItemInfo>>

    suspend fun insert(itemInfo: ItemInfo): Long
    suspend fun insert(item: List<ItemInfo>)

    suspend fun updateCollectStatus(key: ItemKey, isCollect: Boolean)

    // 只更新阅读进度
    suspend fun updateCurrentPage(key: ItemKey, currentPage: Int)
}

class ItemInfoRepositoryImpl @Inject constructor(
    private val itemInfoDao: ItemInfoDao
) : ItemInfoRepository {

    override fun getItemsPager(categoryId: Long): Flow<PagingData<ItemInfo>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                if (categoryId == 1L ) {
                    itemInfoDao.getAllPaged()
                } else {
                    itemInfoDao.getPagedByCategory(categoryId)
                }
            }
        ).flow
    }

    override  fun searchByName(name: String): Flow<PagingData<ItemInfo>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                    itemInfoDao.searchByName(name.trim())
            }
        ).flow
    }

    override suspend fun insert(itemInfo: ItemInfo): Long {
        return itemInfoDao.insert(itemInfo)
    }

    override suspend fun insert(item: List<ItemInfo>) {
        itemInfoDao.insertAll(item)
    }

    override suspend fun updateCollectStatus(key: ItemKey, isCollect: Boolean) {
        itemInfoDao.updateCollectStatus(path = key.path, hash = key.hash, androidId = key.androidId, isCollect)
    }

    override suspend fun updateCurrentPage(key: ItemKey, currentPage: Int) {
        itemInfoDao.updateCurrentPage(path = key.path, hash = key.hash, androidId = key.androidId, currentPage)
    }
}

