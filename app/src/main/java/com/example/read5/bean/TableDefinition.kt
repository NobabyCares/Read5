/*
package com.example.read5.bean

data class TableDefinition(
    val tableName: String,
    val columns: List<ColumnDefinition>,
    val indexes: List<IndexDefinition> = emptyList(),
    val foreignKeys: List<ForeignKey> = emptyList(), // 👈 新增这一行
    val ifNotExists: Boolean = true
)


data class ColumnDefinition(
    val name: String,
    val type: ColumnType,
//    默认可以为空(true)
    val nullable: Boolean = true,
//    默认不唯一(false)
    val unique: Boolean = false,
    val defaultValue: String?  =null
)


data class IndexDefinition(
    val columnName: String
)
enum class ColumnType{
    INTEGER,
    TEXT,
    REAL,
    BLOB
}

//外键动作
enum class ForeignKeyAction {
    NO_ACTION, RESTRICT, SET_NULL, SET_DEFAULT, CASCADE;

    override fun toString(): String = name.replace("_", " ")
}

data class ForeignKey(
    val columns: List<String>,               // 本表的列（如 ["item_id"]）
    val referencedTable: String,             // 被引用的表（如 "item_info"）
    val referencedColumns: List<String>,     // 被引用的列（如 ["id"]）
    val onDelete: ForeignKeyAction = ForeignKeyAction.NO_ACTION,
    val onUpdate: ForeignKeyAction = ForeignKeyAction.NO_ACTION
)*/
