package com.example.read5.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.read5.bean.ItemInfo
import com.example.read5.bean.ItemKey
import com.example.read5.bean.StoreHouse
import kotlinx.coroutines.flow.Flow


@Dao
interface ItemInfoDao {

    @Query("SELECT MAX(id) FROM item_info_table")
    suspend fun getMaxId(): Long?

    // 按 category 分页
    @Query("SELECT * FROM item_info_table WHERE category = :categoryId")
    fun searchByCategory(categoryId: Long): PagingSource<Int, ItemInfo>

    // ✅ 新增：按 name 模糊搜索（不区分大小写）
    @Query("SELECT * FROM item_info_table WHERE name LIKE '%' || :query || '%' ESCAPE '\\'")
    fun searchByName(query: String): PagingSource<Int, ItemInfo>

    //根据id进行查询
    // ✅ 核心：使用 IN (:ids) 语法
    @Query("SELECT * FROM item_info_table WHERE id IN (:query)")
    fun searchById(query: List<Long>): PagingSource<Int, ItemInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(itemInfo: List<ItemInfo>): LongArray

    // 只更新收藏状态
    @Query("UPDATE item_info_table SET isCollect = :isCollect WHERE path = :path AND hash = :hash AND androidId = :androidId")
    suspend fun updateCollect(path: String, hash: String, androidId: String, isCollect: Boolean)

    // 只更新阅读进度
    @Query("UPDATE item_info_table SET currentPage = :currentPage WHERE path = :path AND hash = :hash AND androidId = :androidId")
    suspend fun updateCurrentPage(path: String, hash: String, androidId: String, currentPage: Int)

    // 👇 新增：带 ID 分配的批量插入（关键！）
    @Transaction
    suspend fun insertAllWithAutoId(items: List<ItemInfo>): LongArray { // 👈 返回 LongArray
        val currentMax = getMaxId()?: 0L
        val newItems = items.mapIndexed { index, item ->
            item.copy(id = currentMax + 1 + index)
        }
        val rowIds = insertAll(newItems) // 这已经是 LongArray！
        return rowIds // ✅ 直接返回，无需 .toList()
    }

}