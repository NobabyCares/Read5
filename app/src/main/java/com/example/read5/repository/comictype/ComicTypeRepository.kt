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
    // ✅ 新增：更新 item 的分类（先删除再插入）
    suspend fun updateItemTypes(itemId: Long, newTypeIds: List<Int>)

    // ✅ 新增：删除指定关联
    suspend fun deleteItemFromTypes(itemId: Long, typeIds: List<Int>)

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
    // ✅ 新增：删除指定关联
    override suspend fun deleteItemFromTypes(itemId: Long, typeIds: List<Int>) {
        if (typeIds.isEmpty()) return
        comicTypeDao.deleteItemFromTypes(itemId, typeIds)
    }

    // ✅ 新增：更新 item 的分类（核心方法）
    override suspend fun updateItemTypes(itemId: Long, newTypeIds: List<Int>) {
        // 1. 获取当前关联
        val currentTypeIds = comicTypeDao.getTypeIdByItemId(itemId)

        // 2. 找出需要删除和需要添加的
        val toDelete = currentTypeIds.filter { it !in newTypeIds }
        val toAdd = newTypeIds.filter { it !in currentTypeIds }

        // 3. 执行删除
        if (toDelete.isNotEmpty()) {
            comicTypeDao.deleteItemFromTypes(itemId, toDelete)
        }

        // 4. 执行添加
        if (toAdd.isNotEmpty()) {
            val crossRefs = toAdd.map { typeId ->
                ItemComicTypeCrossRef(itemId = itemId, typeId = typeId)
            }
            comicTypeDao.insertCrossRefs(crossRefs)
        }
    }

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