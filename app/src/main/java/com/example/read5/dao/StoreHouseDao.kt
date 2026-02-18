package com.example.read5.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.read5.bean.StoreHouse
import kotlinx.coroutines.flow.Flow

@Dao
interface  StoreHouseDao {
    // 🔹 查询所有书库（返回 Flow → 自动刷新）
    @Query("SELECT * FROM store_house_table")
    fun getAll(): Flow<List<StoreHouse>>

    // 🔹 插入一个书库
    @Insert
    suspend fun insert(storeHouse: StoreHouse): Long // 返回插入的 id

    // 🔹 插入多个
    @Insert
    suspend fun insertAll(storeHouses: List<StoreHouse>): List<Long>

    // 🔹 更新
    @Query("UPDATE store_house_table SET count = :count WHERE id = :id")
    suspend fun updateByCount(id: Long, count: Long)

    // ✅ 推荐：直接按主键删除（高效、安全）
    @Query("DELETE FROM store_house_table WHERE id = :id")
    suspend fun deleteById(id: Long)

}