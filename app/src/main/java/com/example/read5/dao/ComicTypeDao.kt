package com.example.read5.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.read5.bean.ComicType
import com.example.read5.bean.ItemComicTypeCrossRef
import com.example.read5.bean.ItemInfo
import com.example.read5.bean.ItemWithTypes
import kotlinx.coroutines.flow.Flow

@Dao
interface ComicTypeDao {
    @Query("UPDATE comic_type_table SET name = :name WHERE id = :id")
    suspend fun updateComicTypeName(id: Int, name: String)
    @Query("UPDATE comic_type_table SET cover = :cover WHERE id IN (:ids)")
    suspend fun updateComicTypeCover(ids: List<Int>, cover: String)
    @Query("UPDATE comic_type_table SET count = :count WHERE id = :id")
    suspend fun updateComicTypeCount(id: Int, count: Int)


    @Query("""
        SELECT * FROM comic_type_table 
        WHERE name LIKE '%' || :query || '%' 
        ORDER BY name COLLATE NOCASE ASC
    """)
      fun getComicTypeByName(query: String): Flow<List<ComicType>>

    // ComicTypeDao.kt
    @Query("SELECT * FROM comic_type_table ORDER BY name")
      fun getAllTypesFlow(): Flow<List<ComicType>>  // 👈 不是 suspend，返回 Flow！

    // ─── 查询：根据 Item ID 获取完整信息 + 分类 ──
    @Transaction
    @Query("SELECT * FROM item_info_table WHERE id = :itemId")
    suspend fun getItemWithTypes(itemId: Long): ItemWithTypes?

    // ─── 查询：获取所有 Item + 分类（用于列表）──
    @Transaction
    @Query("SELECT * FROM item_info_table")
    suspend fun getAllItemsWithTypes(): List<ItemWithTypes>

    // ─── 查询：根据分类 ID 获取所有相关 Item ───
    @Transaction
    @Query("""
        SELECT DISTINCT i.* 
        FROM item_info_table i
        JOIN item_comic_type_cross_ref c ON i.id = c.itemId
        WHERE c.typeId = :typeId
    """)
    suspend fun getItemsByTypeId(typeId: Int): List<ItemWithTypes>


    // 👇 新增：根据 itemId 获取它关联的所有 typeId 列表
    @Query("SELECT typeId FROM item_comic_type_cross_ref WHERE itemId = :itemId")
    suspend fun getTypeIdByItemId(itemId: Long): List<Int>


    // ─── 插入分类 ────────────────────────
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertType(type: ComicType): Long

    // ─── 插入关联（中间表）────────────────
    @Insert
    suspend fun insertCrossRef(crossRef: ItemComicTypeCrossRef)

    // ─── 批量插入关联（推荐）──────────────
    @Insert
    suspend fun insertCrossRefs(crossRefs: List<ItemComicTypeCrossRef>)

    @Query("DELETE FROM comic_type_table WHERE id = :id")
    suspend fun deleteById(id: Int): Int



}