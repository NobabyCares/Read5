package com.example.read5.bean

import androidx.room.Entity


@Entity(
    tableName = "comic_type_table",
    primaryKeys = ["id"],
    )
data class ComicType(
    var id: Int,
    var name: String,
    var cover: String,
)