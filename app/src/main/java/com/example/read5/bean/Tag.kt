package com.example.read5.bean

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tag_table",
    indices = [
        Index("name")
    ]
)
data class Tag(
    @PrimaryKey()
    val id: Long = 0,
    val name: String = ""
)