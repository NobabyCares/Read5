package com.example.read5.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.read5.bean.ItemInfo
import com.example.read5.bean.StoreHouse
import com.example.read5.bean.Tag
import com.example.read5.dao.ItemInfoDao
import com.example.read5.dao.StoreHouseDao
import com.example.read5.dao.TagDao


@Database(
    entities = [
        StoreHouse::class,
        ItemInfo::class,
        Tag::class,
               ], // 👈 告诉 Room 有哪些表
    version = 1,                    // 初始版本
    exportSchema = false            // 开发阶段可关掉 schema 导出
)
abstract class AppDatabase : RoomDatabase() {

    // 👇 提供 DAO 的访问入口
    abstract fun storeHouseDao(): StoreHouseDao
    abstract fun itemInfoDao(): ItemInfoDao
    abstract fun tagDao(): TagDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "read_app_database.db" // 数据库文件名
                )
                    .setJournalMode(JournalMode.TRUNCATE)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}