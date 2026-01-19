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
)