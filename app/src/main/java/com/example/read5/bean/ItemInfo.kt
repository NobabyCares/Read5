package com.example.read5.bean

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.read5.global.DeviceIdentification
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
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
        Index("id")
    ]
)
data class ItemInfo(
//    id，不做主键，已经有联合主键，用于查询
    var id: Long = 0,
    var name: String = "",
    var baseCode: String = "",
    var author: String = "",
    var path: String = "",
    var androidId: String = DeviceIdentification.androidId,
    //    总页数
    var totalPage: Int = 0,
    //    阅读的页数
    var currentPage: Int = 0,
    // 如果为false 就是默认渲染第一页,如果为true, 就在应用文件夹cover选择名为"id.jpg"的图片
    var cover: Boolean =false,
    //    创建时间
    var createTime: Long = 0L,
    //    最后阅读时间
    var lastReadTime: Long = 0L,
    //    总的阅读时间，单位秒
    var totalReadTime: Long = 0L,
    //    进度
    var schedule:Int = 0,
    //    黑名单,默认true 显示, 反之隐藏
    var isShow:Boolean = true,
    //    hash
    var hash: String = "",
    //    分类
    var category: Long,
    //    是否收藏
    var isCollect: Boolean = false,
    //    文件大小
    var fileSize: Long = 0,
    var fileType : String = ""
){
    // ✅ 添加一个可读时间字符串（基于 lastReadTime）
    val lastReadTimeFormatted: String
        get() = if (lastReadTime > 0) {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(lastReadTime))
        } else {
            ""
        }

    // ✅ 同样可以为 createTime 添加
    val createTimeFormatted: String
        get() = if (createTime > 0) {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(createTime))
        } else {
            ""
        }
}


