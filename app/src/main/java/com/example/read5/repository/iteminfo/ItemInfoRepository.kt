package com.example.read5.repository.iteminfo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.read5.bean.ItemInfo
import com.example.read5.bean.ItemKey
import com.example.read5.dao.ItemInfoDao
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

    fun searchByIshow(): Flow<PagingData<ItemInfo>>

    suspend fun insert(item: List<ItemInfo>)
    //更新收藏状态
    suspend fun updateByCollect(key: ItemKey, isCollect: Boolean)
    // 只更新阅读进度
    suspend fun updateByCurrentPage(key: ItemKey, currentPage: Int)
    //更新隐藏状态
    suspend fun updateByIsShow(key: ItemKey, isShow: Boolean): Int


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

    override fun searchByIshow(): Flow<PagingData<ItemInfo>> {
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
}

