// util/DbHelper.kt
package com.example.read5.screens.myview

import android.annotation.SuppressLint
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.read5.bean.ItemInfo
import java.io.File
import java.util.Base64

object DbHelper {

    private const val TAG = "DbHelper"


    /**
     * 根据关键词对 name 字段进行模糊查询（%keyword%）
     */
    fun queryItemsByName(dbPath: String, keyword: String? = null): List<ItemInfo> {
        return try {
            val dbFile = File(dbPath)
            if (!dbFile.exists()) {
                Log.e(TAG, "Database not found: $dbPath")
                return emptyList()
            }

            val whereClause = if (!keyword.isNullOrBlank()) {
                "name LIKE ?"
            } else {
                null
            }
            val whereArgs = if (!keyword.isNullOrBlank()) {
                arrayOf("%$keyword%") // 安全参数化
            } else {
                null
            }

            SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
                .use { db ->
                    db.query("item_info_table", null, whereClause, whereArgs, null, null, null)
                        .use { cursor ->
                            val items = mutableListOf<ItemInfo>()
                            while (cursor.moveToNext()) {
                                try {
                                    // 注意：totalPages → totalPage
                                    val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                                    val name =
                                        cursor.getString(cursor.getColumnIndexOrThrow("name"))
                                    val baseCode =
                                        cursor.getString(cursor.getColumnIndexOrThrow("baseCode"))
                                    val author =
                                        cursor.getString(cursor.getColumnIndexOrThrow("author"))
                                    val path =
                                        cursor.getString(cursor.getColumnIndexOrThrow("path"))
                                    val androidId =
                                        cursor.getString(cursor.getColumnIndexOrThrow("androidId"))
                                    val totalPage =
                                        cursor.getInt(cursor.getColumnIndexOrThrow("totalPage")) // 👈 关键
                                    val currentPage =
                                        cursor.getInt(cursor.getColumnIndexOrThrow("currentPage"))
                                    val cover =
                                        cursor.getInt(cursor.getColumnIndexOrThrow("cover")) == 1
                                    val createTime =
                                        cursor.getLong(cursor.getColumnIndexOrThrow("createTime"))
                                    val lastReadTime =
                                        cursor.getLong(cursor.getColumnIndexOrThrow("lastReadTime"))
                                    val totalReadTime =
                                        cursor.getLong(cursor.getColumnIndexOrThrow("totalReadTime"))
                                    val schedule =
                                        cursor.getInt(cursor.getColumnIndexOrThrow("schedule"))
                                    val isShow =
                                        cursor.getInt(cursor.getColumnIndexOrThrow("isShow")) == 1
                                    val hash =
                                        cursor.getString(cursor.getColumnIndexOrThrow("hash"))
                                    val category =
                                        cursor.getLong(cursor.getColumnIndexOrThrow("category"))
                                    val isCollect =
                                        cursor.getInt(cursor.getColumnIndexOrThrow("isCollect")) == 1
                                    val fileSize =
                                        cursor.getLong(cursor.getColumnIndexOrThrow("fileSize"))
                                    val fileType =
                                        cursor.getString(cursor.getColumnIndexOrThrow("fileType"))

                                    items.add(
                                        ItemInfo(
                                            id = id,
                                            name = name,
                                            baseCode = baseCode,
                                            author = author,
                                            path = path,
                                            androidId = androidId,
                                            totalPage = totalPage,
                                            currentPage = currentPage,
                                            cover = cover,
                                            createTime = createTime,
                                            lastReadTime = lastReadTime,
                                            totalReadTime = totalReadTime,
                                            schedule = schedule,
                                            isShow = isShow,
                                            hash = hash,
                                            category = category,
                                            isCollect = isCollect,
                                            fileSize = fileSize,
                                            fileType = fileType
                                        )
                                    )
                                } catch (e: Exception) {
                                    Log.w(TAG, "Skip row", e)
                                }
                            }
                            items
                        }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Query failed", e)
            emptyList()
        }
    }
}

