package com.example.read5.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.read5.bean.ComicType
import com.example.read5.bean.ItemInfo


@Dao
interface ComicTypeDao {

    @Query("SELECT * FROM comic_type_table")
    fun getAll(): PagingSource<Int, ComicType>

    @Insert
    suspend fun insert(comicTypes: List<ComicType>)

    @Insert
    suspend fun insert(comicTypes: ComicType)

}