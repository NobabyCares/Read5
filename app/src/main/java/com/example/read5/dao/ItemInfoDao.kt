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

    // 适用于数据量 < 2000 的场景，用于排查 Paging 问题
    @Query("SELECT * FROM item_info_table WHERE category = :category ORDER BY name DESC")
    fun getAllItemsByCategoryFlow(category: Long): Flow<List<ItemInfo>>

    // 按 category 分页,只显示isShow =1的
    @Query("SELECT * FROM item_info_table WHERE category = :categoryId AND isShow = 1 " +
            "ORDER BY name COLLATE NOCASE ASC")
    fun searchByCategory(categoryId: Long): PagingSource<Int, ItemInfo>

    // ✅ 新增：按 name 模糊搜索（不区分大小写）
    @Query("""
        SELECT * FROM item_info_table 
        WHERE name LIKE '%' || :query || '%' 
          AND isShow = 1 
        ORDER BY name COLLATE NOCASE ASC
    """)
     fun searchByName(query: String): Flow<List<ItemInfo>>


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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(itemInfo: List<ItemInfo>): LongArray
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(itemInfo: ItemInfo): Long

    // 只更新收藏状态
    @Query("UPDATE item_info_table SET isCollect = :isCollect WHERE id = :id")
    suspend fun updateByCollect(id: Long, isCollect: Boolean)

    // 只更新阅读位置
    @Query("UPDATE item_info_table SET currentPage = :currentPage WHERE id = :id")
    suspend fun updateByCurrentPage(id: Long, currentPage: Int)
    @Query("UPDATE item_info_table SET schedule = :schedule WHERE id = :id")
    suspend fun updateBySchedule(id: Long, schedule: Int)

    @Query("UPDATE item_info_table SET isShow = :isShow WHERE id = :id")
    suspend fun updateByIsShow(id: Long, isShow: Boolean): Int

    @Query("UPDATE item_info_table SET lastReadTime = :lastReadTime WHERE id = :id ")
    suspend fun updateByLastReadTime(id: Long, lastReadTime: Long)
    //总的阅读时间
    @Query("UPDATE item_info_table SET totalReadTime = :totalReadTime WHERE id = :id ")
    suspend fun updateByTotalReadTime(id: Long, totalReadTime: Long)


}