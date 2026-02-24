package com.example.read5.bean

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index


/**
 * 中间表：记录 Item 和 ComicType 的多对多关系
 */
@Entity(
    tableName = "item_comic_type_cross_ref",
    primaryKeys = ["itemId", "typeId"], // 联合主键
    foreignKeys = [
        ForeignKey(
            entity = ItemInfo::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ComicType::class,
            parentColumns = ["id"],
            childColumns = ["typeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("itemId"),
        Index("typeId")
    ]
)
data class ItemComicTypeCrossRef(
    val itemId: Long,
    val typeId: Int
)