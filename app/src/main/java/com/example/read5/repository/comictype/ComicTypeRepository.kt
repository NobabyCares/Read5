// ComicTypeRepository.kt
package com.example.read5.repository.comictype

import androidx.room.Query
import com.example.read5.bean.ComicType
import com.example.read5.bean.ItemComicTypeCrossRef
import com.example.read5.bean.ItemWithTypes
import com.example.read5.dao.ComicTypeDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


interface  ComicTypeRepositoryApi{
    suspend fun updateComicTypeName(id: Int, name: String)

    suspend fun updateComicTypeCover(ids: List<Int>, cover: String)

    suspend fun updateComicTypeCount(id: Int, count: Int)


     fun getAllTypesFlow(): Flow<List<ComicType>>
    suspend fun getComicTypeByName(query: String): Flow<List<ComicType>>


    suspend fun insertType(type: ComicType): Long

    suspend fun insertItemToTypes(itemId: Long, typeIds: List<Int>)

    suspend fun getItemsByTypeId(typeId: Int): List<ItemWithTypes>

    suspend fun getTypeIdByItemId(id: Long): List<Int>

    suspend fun deleteById(id: Int): Int
}
class ComicTypeRepository @Inject constructor(
    private val comicTypeDao: ComicTypeDao
): ComicTypeRepositoryApi {
    override suspend fun updateComicTypeName(id: Int, name: String) {
        comicTypeDao.updateComicTypeName(id, name)
    }

    override suspend fun updateComicTypeCover(ids: List<Int>, cover: String) {
        comicTypeDao.updateComicTypeCover(ids, cover)
    }

    override suspend fun updateComicTypeCount(id: Int, count: Int) {
        comicTypeDao.updateComicTypeCount(id, count)
    }



    override  fun getAllTypesFlow(): Flow<List<ComicType>> {
        return comicTypeDao.getAllTypesFlow()
    }

    override suspend fun getComicTypeByName(query: String): Flow<List<ComicType>> {
        return comicTypeDao.getComicTypeByName(query)
    }


    // ─── 2. 插入新分类 ──────────────────────
    override suspend fun insertType(type: ComicType): Long {
        return comicTypeDao.insertType(type)
    }

    // ─── 3. 批量插入 Item 与分类的关联 ──────────
    override suspend fun insertItemToTypes(itemId: Long, typeIds: List<Int>) {
        if (typeIds.isEmpty()) return
        val crossRefs = typeIds.map { typeId ->
            ItemComicTypeCrossRef(itemId = itemId, typeId = typeId)
        }
        comicTypeDao.insertCrossRefs(crossRefs)
    }

    // ─── 4. 根据分类 ID 查询所有相关 Item（带完整数据）──
    override suspend fun getItemsByTypeId(typeId: Int): List<ItemWithTypes> {
        return comicTypeDao.getItemsByTypeId(typeId)
    }

    override suspend fun getTypeIdByItemId(id: Long): List<Int> {
        return comicTypeDao.getTypeIdByItemId(id)
    }

    override suspend fun deleteById(id: Int): Int {
        return comicTypeDao.deleteById(id)
    }
}