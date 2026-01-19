package com.example.read5.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.read5.bean.ItemInfo
import com.example.read5.bean.StoreHouse
import com.example.read5.dao.ItemInfoDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface ItemInfoRepository {
    // ❌ 移除旧的 getAll()、searchByCategory()
    // ✅ 新增：返回可参数化的分页流
    fun getItemsPager(categoryId: Long? = null): Flow<PagingData<ItemInfo>>

    suspend fun insert(itemInfo: ItemInfo): Long
    suspend fun insert(item: List<ItemInfo>)
}

class ItemInfoRepositoryImpl @Inject constructor(
    private val itemInfoDao: ItemInfoDao
) : ItemInfoRepository {

    override fun getItemsPager(categoryId: Long?): Flow<PagingData<ItemInfo>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                if (categoryId == null) {
                    itemInfoDao.getAllPaged()
                } else {
                    itemInfoDao.getPagedByCategory(categoryId)
                }
            }
        ).flow
    }

    override suspend fun insert(itemInfo: ItemInfo): Long {
        return itemInfoDao.insert(itemInfo)
    }

    override suspend fun insert(item: List<ItemInfo>) {
        itemInfoDao.insertAll(item)
    }
}

