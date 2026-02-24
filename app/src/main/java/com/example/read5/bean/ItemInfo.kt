package com.example.read5.bean

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.read5.global.DeviceIdentification
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
@Entity(
    tableName = "item_info_table",
    // ✅ 移除 primaryKeys，改用单字段主键 id
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
        // ✅ 新增：用 path + androidId 保证唯一性（核心！）
        Index(
            value = ["path", "androidId"],
            unique = true
        )
    ]
)
data class ItemInfo(
    @PrimaryKey(autoGenerate = true) // Room 会映射到 rowid，插入时传 0 即可
    var id: Long = 0,

    var name: String = "",
    var baseCode: String = "",
    var author: String = "",
    var path: String = "",
    var androidId: String = DeviceIdentification.androidId,

    var totalPage: Int = 0,
    var currentPage: Int = 0,
    var cover: Boolean = false,

    var createTime: Long = 0L,
    var lastReadTime: Long = 0L,
    var totalReadTime: Long = 0L,
    var schedule: Int = 0,
    var isShow: Boolean = true,

    var hash: String = "", // hash 保留，但不参与唯一性
    var category: Long,

    // ⚠️ 注意：comicType 是单分类，未来建议用中间表支持多分类
    var comicType: Int = -1,

    var isCollect: Boolean = false,
    var fileSize: Long = 0,
    var fileType: String = ""
) {
    val lastReadTimeFormatted: String
        get() = if (lastReadTime > 0) {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(lastReadTime))
        } else {
            ""
        }

    val createTimeFormatted: String
        get() = if (createTime > 0) {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(createTime))
        } else {
            ""
        }
}