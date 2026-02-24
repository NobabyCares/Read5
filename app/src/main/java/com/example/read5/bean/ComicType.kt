package com.example.read5.bean

import androidx.room.Entity
import androidx.room.Index


@Entity(
    tableName = "comic_type_table",
    primaryKeys = ["id"],
    indices = [
        // ✅ 新增：用 path + androidId 保证唯一性（核心！）
        Index(
            value = ["name"],
            unique = true
        )
    ]
    )
data class ComicType(
    var id: Int = 0,
    var name: String,
    var cover: String = "",
)