package com.example.read5.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.read5.bean.ItemInfo
import com.example.read5.bean.ItemKey
import com.example.read5.bean.StoreHouse
import kotlinx.coroutines.flow.Flow


@Dao
interface ItemInfoDao {
    // 全量分页
    @Query("SELECT * FROM item_info_table")
    fun getAllPaged(): PagingSource<Int, ItemInfo>

    // 按 category 分页
    @Query("SELECT * FROM item_info_table WHERE category = :categoryId")
    fun getPagedByCategory(categoryId: Long): PagingSource<Int, ItemInfo>

    // ✅ 新增：按 name 模糊搜索（不区分大小写）
    @Query("SELECT * FROM item_info_table WHERE name LIKE '%' || :query || '%' ESCAPE '\\'")
    fun searchByName(query: String): PagingSource<Int, ItemInfo>

    // 插入（保持不变）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(itemInfo: ItemInfo): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(itemInfo: List<ItemInfo>): LongArray

    // 只更新收藏状态
    @Query("UPDATE item_info_table SET isCollect = :isCollect WHERE path = :path AND hash = :hash AND androidId = :androidId")
    suspend fun updateCollectStatus(path: String, hash: String, androidId: String, isCollect: Boolean)

    // 只更新阅读进度
    @Query("UPDATE item_info_table SET currentPage = :currentPage WHERE path = :path AND hash = :hash AND androidId = :androidId")
    suspend fun updateCurrentPage(path: String, hash: String, androidId: String, currentPage: Int)

}