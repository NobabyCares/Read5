package com.example.read5.bean

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "store_house_table",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class StoreHouse (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val type: String,
    // 存储数量
    val count: Long,
    val folderPath: String,
    val lastUpdateTime: Long // 新增：记录最后更新时间的时间戳
)