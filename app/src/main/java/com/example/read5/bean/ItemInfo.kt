package com.example.read5.bean

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.read5.global.GlobalData


@Entity(
    tableName = "item_info_table",
    primaryKeys = ["path", "hash", "androidId"],
    foreignKeys = [
        ForeignKey(
            entity = StoreHouse::class,
            parentColumns = ["id"],
            childColumns = ["category"], // category 是外键
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("category"),
        Index("name"),
    ]
)
data class ItemInfo(
    var name: String = "",
    var secondName: String = "",
    var author: String = "",
    var path: String = "",
//如何去获取这个值?
    val androidId: String = GlobalData.androidId,
//    总页数
    var totalPage: Int = 0,
//    阅读的页数
    var currentPage: Int = 0,
// 如果为false 就是默认渲染第一页,如果为true, 就在应用文件夹cover选择名为"id.webp"的图片
    var cover: Boolean =false,
//    创建时间
    var createTime: String = "",
//    最后阅读时间
    var lastReadTime: String = "",
//    进度
    var schedule:Int = 0,
//    黑名单,默认true 显示, 反之隐藏
    var isShow:Boolean = true,
//    hash
    val hash: String = "",
//    分类
    var category: Long,
//    标签
//    是否收藏
    var isCollect: Boolean = false,
//    文件大小
    var fileSize: Long = 0,
    val fileType : String = ""
)


