package com.example.read5.bean

// ItemWithTypes.kt
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * 这不是一个表！只是一个“查询结果包装类”
 */
data class ItemWithTypes(
    // 1. 把 ItemInfo 的所有字段“嵌入”进来
    @Embedded
    val item: ItemInfo,

    // 2. 自动关联多个 ComicType
    @Relation(
        parentColumn = "id",          // ItemInfo.id
        entityColumn = "id",          // ComicType.id
        associateBy = Junction(       // 通过中间表关联
            value = ItemComicTypeCrossRef::class,
            parentColumn = "itemId",  // 中间表的 itemId
            entityColumn = "typeId"   // 中间表的 typeId
        )
    )
    val types: List<ComicType>
)