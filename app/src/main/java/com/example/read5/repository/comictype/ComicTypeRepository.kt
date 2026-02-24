// ComicTypeRepository.kt
package com.example.read5.repository.comictype

import com.example.read5.bean.ComicType
import com.example.read5.bean.ItemComicTypeCrossRef
import com.example.read5.bean.ItemWithTypes
import com.example.read5.dao.ComicTypeDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


interface  ComicTypeRepositoryApi{
    suspend fun getAllTypes(): List<ComicType>

    fun getAllTypesFlow(): Flow<List<ComicType>>

    suspend fun insertType(type: ComicType): Long

    suspend fun insertItemToTypes(itemId: Long, typeIds: List<Int>)

    suspend fun getItemsByTypeId(typeId: Int): List<ItemWithTypes>

    suspend fun getTypeIdByItemId(id: Long): List<Int>
}
class ComicTypeRepository @Inject constructor(
    private val comicTypeDao: ComicTypeDao
): ComicTypeRepositoryApi {

    // ─── 1. 查询所有分类（用于显示）────────────
    override suspend fun getAllTypes(): List<ComicType> {
        return comicTypeDao.getAllTypes()
    }

    override fun getAllTypesFlow(): Flow<List<ComicType>> {
        return comicTypeDao.getAllTypesFlow()
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
}