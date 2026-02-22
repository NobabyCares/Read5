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

    // 按 category 分页,只显示isShow =1的
    @Query("SELECT * FROM item_info_table WHERE category = :categoryId AND isShow = 1 " +
            "ORDER BY name COLLATE NOCASE ASC")
    fun searchByCategory(categoryId: Long): PagingSource<Int, ItemInfo>

    // ✅ 新增：按 name 模糊搜索（不区分大小写）
    @Query("SELECT * FROM item_info_table " +
            "WHERE name LIKE '%' || :query || '%' ESCAPE '\\' AND isShow = 1 " +
            "ORDER BY name COLLATE NOCASE ASC")
    fun searchByName(query: String): PagingSource<Int, ItemInfo>


    @Query("SELECT * FROM item_info_table " +
            "WHERE  isShow = 1 AND category = :category " +
            "ORDER BY name COLLATE NOCASE ASC")
    fun sortByNameASC(category: Long): PagingSource<Int, ItemInfo>
    @Query("SELECT * FROM item_info_table " +
            "WHERE  isShow = 1 AND category = :category " +
            "ORDER BY name COLLATE NOCASE DESC")
    fun sortByNameDesc(category: Long): PagingSource<Int, ItemInfo>

    @Query("""
        SELECT * FROM item_info_table 
        WHERE isShow = 1 AND category = :category
        ORDER BY lastReadTime ASC
    """)
    fun sortByLastReadTimeASC(category: Long): PagingSource<Int, ItemInfo>
    @Query("""
        SELECT * FROM item_info_table 
        WHERE isShow = 1 AND category = :category
        ORDER BY lastReadTime DESC""")
    fun sortByLastReadTimeDESC(category: Long): PagingSource<Int, ItemInfo>


    @Query("""
        SELECT * FROM item_info_table 
        WHERE isShow = 1 AND category = :category
        ORDER BY schedule ASC
    """)
    fun sortByScheduleASC(category: Long): PagingSource<Int, ItemInfo>
    @Query("""
        SELECT * FROM item_info_table 
        WHERE isShow = 1 AND category = :category
        ORDER BY schedule DESC
    """)
    fun sortByScheduleDESC(category: Long): PagingSource<Int, ItemInfo>

    @Query("""
        SELECT * FROM item_info_table 
        WHERE isShow = 1 AND category = :category
        ORDER BY totalReadTime ASC
    """)
    fun sortByTotalReadTimeASC(category: Long): PagingSource<Int, ItemInfo>
    @Query("""
        SELECT * FROM item_info_table 
        WHERE isShow = 1 AND category = :category
        ORDER BY totalReadTime DESC
    """)
    fun sortByTotalReadTimeDESC(category: Long): PagingSource<Int, ItemInfo>


    @Query("""
        SELECT * FROM item_info_table 
        WHERE isShow = 1 AND category = :category
        ORDER BY fileSize ASC
    """)
    fun sortByFileSizeASC(category: Long): PagingSource<Int, ItemInfo>
    @Query("""
        SELECT * FROM item_info_table 
        WHERE isShow = 1 AND category = :category
        ORDER BY fileSize DESC
    """)
    fun sortByFileSizeDESC(category: Long): PagingSource<Int, ItemInfo>

    @Query("""
        SELECT * FROM item_info_table 
        WHERE isShow = 1 AND category = :category
        ORDER BY createTime ASC
    """)
    fun sortByCreateTimeASC(category: Long): PagingSource<Int, ItemInfo>
    @Query("""
        SELECT * FROM item_info_table 
        WHERE isShow = 1 AND category = :category
        ORDER BY createTime DESC
    """)
    fun sortByCreateTimeDESC(category: Long): PagingSource<Int, ItemInfo>


    //根据id进行查询
    // ✅ 核心：使用 IN (:ids) 语法
    @Query("SELECT * FROM item_info_table WHERE id IN (:query) AND isShow = 1")
    fun searchById(query: List<Long>): PagingSource<Int, ItemInfo>

    @Query("SELECT * FROM item_info_table where isShow = 0 "+
            "ORDER BY name COLLATE NOCASE ASC")
    fun searchByIsShow(): PagingSource<Int, ItemInfo>

    @Query("SELECT * FROM item_info_table where isCollect = 1 "+
            "ORDER BY name COLLATE NOCASE ASC")
    fun searchByIsCollect(): PagingSource<Int, ItemInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(itemInfo: List<ItemInfo>): LongArray

    // 只更新收藏状态
    @Query("UPDATE item_info_table SET isCollect = :isCollect WHERE path = :path AND hash = :hash AND androidId = :androidId")
    suspend fun updateByCollect(path: String, hash: String, androidId: String, isCollect: Boolean)

    // 只更新阅读位置
    @Query("UPDATE item_info_table SET currentPage = :currentPage WHERE path = :path AND hash = :hash AND androidId = :androidId")
    suspend fun updateByCurrentPage(path: String, hash: String, androidId: String, currentPage: Int)
    @Query("UPDATE item_info_table SET schedule = :schedule WHERE path = :path AND hash = :hash AND androidId = :androidId")
    suspend fun updateBySchedule(path: String, hash: String, androidId: String, schedule: Int)

    @Query("UPDATE item_info_table SET isShow = :isShow WHERE path = :path AND hash = :hash AND androidId = :androidId")
    suspend fun updateByIsShow(path: String, hash: String, androidId: String, isShow: Boolean): Int

    @Query("UPDATE item_info_table SET lastReadTime = :lastReadTime WHERE path = :path AND hash = :hash AND androidId = :androidId ")
    suspend fun updateByLastReadTime(path: String, hash: String, androidId: String, lastReadTime: Long)
    //总的阅读时间
    @Query("UPDATE item_info_table SET totalReadTime = :totalReadTime WHERE path = :path AND hash = :hash AND androidId = :androidId ")
    suspend fun updateByTotalReadTime(path: String, hash: String, androidId: String, totalReadTime: Long)
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