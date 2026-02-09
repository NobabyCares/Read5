package com.example.read5.db

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.read5.bean.ItemInfo
import com.example.read5.bean.StoreHouse
import com.example.read5.bean.Tag
import com.example.read5.dao.ItemInfoDao
import com.example.read5.dao.StoreHouseDao
import com.example.read5.dao.TagDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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

        val addStoreHouse: StoreHouse = StoreHouse(name = "+", type = "", count = 0L, lastUpdateTime = 0L)

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "read_app_database.db" // 数据库文件名
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // 直接在IO线程执行
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).storeHouseDao().let { dao ->
                                    listOf(
                                        StoreHouse(name = "+", type = "", count = 0L, lastUpdateTime = 0L)
                                    ).forEach { dao.insert(it) }
                                }
                            }
                        }
                    })
                    .setJournalMode(JournalMode.TRUNCATE)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}