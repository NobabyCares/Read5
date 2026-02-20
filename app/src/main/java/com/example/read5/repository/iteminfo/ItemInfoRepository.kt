package com.example.read5.repository.iteminfo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.example.read5.bean.ItemInfo
import com.example.read5.bean.ItemKey
import com.example.read5.dao.ItemInfoDao
import com.example.read5.screens.sortbar.SortField
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
interface ItemInfoRepository {
    // ❌ 移除旧的 getAll()、searchByCategory()
    // ✅ 新增：返回可参数化的分页流
    fun searchByCategory(categoryId: Long): Flow<PagingData<ItemInfo>>

    fun searchByName(name: String): Flow<PagingData<ItemInfo>>

    fun searchById(id: List<Long>): Flow<PagingData<ItemInfo>>

    fun searchByIsShow(): Flow<PagingData<ItemInfo>>

    fun searchByIsCollect(): Flow<PagingData<ItemInfo>>


    suspend fun insert(item: List<ItemInfo>)
    //更新收藏状态
    suspend fun updateByCollect(key: ItemKey, isCollect: Boolean)
    // 只更新阅读位置
    suspend fun updateByCurrentPage(key: ItemKey, currentPage: Int)
    //更新隐藏状态
    suspend fun updateByIsShow(key: ItemKey, isShow: Boolean): Int
    //更新阅读时间
    suspend fun updateByLastReadTime(key: ItemKey, lastReadTime: Long)
    //更新进度
    suspend fun updateBySchedule(key: ItemKey, schedule: Int)
    //更新总的阅读时间
    suspend fun updateByTotalReadTime(key: ItemKey, totalReadTime: Long)
    //排序方法
    fun sortBySortField(sortField: SortField, ascending: Boolean, category: Long): Flow<PagingData<ItemInfo>>





}

class ItemInfoRepositoryImpl @Inject constructor(
    private val itemInfoDao: ItemInfoDao
) : ItemInfoRepository {

    override fun searchByCategory(categoryId: Long): Flow<PagingData<ItemInfo>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                itemInfoDao.searchByCategory(categoryId)
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

    override fun searchById(id: List<Long>): Flow<PagingData<ItemInfo>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                itemInfoDao.searchById(id)
            }
        ).flow
    }

    override fun searchByIsShow(): Flow<PagingData<ItemInfo>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                itemInfoDao.searchByIsShow()
            }
        ).flow
    }

    override fun searchByIsCollect(): Flow<PagingData<ItemInfo>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                itemInfoDao.searchByIsCollect()
            }
        ).flow
    }

    override fun sortBySortField(
        sortField: SortField,
        ascending: Boolean,
        category: Long
    ): Flow<PagingData<ItemInfo>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                when(sortField){
                    SortField.NAME -> if (ascending) itemInfoDao.sortByNameASC(category) else itemInfoDao.sortByNameDesc(category)
                    SortField.LAST_READ_TIME -> if(ascending) itemInfoDao.sortByLastReadTimeASC(category) else itemInfoDao.sortByLastReadTimeDESC(category)
                    SortField.READ_PROGRESS -> if(ascending) itemInfoDao.sortByScheduleASC(category) else itemInfoDao.sortByScheduleDESC(category)
                    SortField.TOTAL_READ_TIME -> if(ascending) itemInfoDao.sortByTotalReadTimeASC(category) else itemInfoDao.sortByTotalReadTimeDESC(category)
                }
            }
        ).flow
    }


    override suspend fun insert(item: List<ItemInfo>) {
        itemInfoDao.insertAllWithAutoId(item)
    }

    override suspend fun updateByCollect(key: ItemKey, isCollect: Boolean) {
        itemInfoDao.updateByCollect(path = key.path, hash = key.hash, androidId = key.androidId, isCollect)
    }

    override suspend fun updateByCurrentPage(key: ItemKey, currentPage: Int) {
        itemInfoDao.updateByCurrentPage(path = key.path, hash = key.hash, androidId = key.androidId, currentPage)
    }

    override suspend fun updateByIsShow(key: ItemKey, isShow: Boolean): Int {
        return itemInfoDao.updateByIsShow(isShow = isShow,path = key.path, hash = key.hash, androidId = key.androidId)
    }

    override suspend fun updateByLastReadTime(key: ItemKey, lastReadTime: Long) {
        itemInfoDao.updateByLastReadTime(path = key.path, hash = key.hash, androidId = key.androidId, lastReadTime)
    }

    override suspend fun updateBySchedule(key: ItemKey, schedule: Int) {
        itemInfoDao.updateBySchedule(path = key.path, hash = key.hash, androidId = key.androidId, schedule =  schedule)
    }

    override suspend fun updateByTotalReadTime(key: ItemKey, totalReadTime: Long) {
        itemInfoDao.updateByTotalReadTime(path = key.path, hash = key.hash, androidId = key.androidId, totalReadTime)
    }
}

