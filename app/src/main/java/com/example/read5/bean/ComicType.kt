package com.example.read5.bean

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "comic_type_table",
    indices = [
        // ✅ 新增：用 path + androidId 保证唯一性（核心！）
        Index(
            value = ["name"],
            unique = true
        )
    ]
    )
data class ComicType(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var name: String,
    val count: Int = 0,
    var cover: String = "",
)