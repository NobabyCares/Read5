package com.example.read5.repository

import com.example.read5.bean.StoreHouse
import com.example.read5.dao.StoreHouseDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


interface StoreHouseRepository {
    fun getAll(): Flow<List<StoreHouse>>
    suspend fun searchById(id: Long): StoreHouse
    suspend fun insert(storeHouse: StoreHouse): Long
    //更新总数
    suspend fun updateByCount(id: Long, count: Long)

    suspend fun deleteStoreHouse(id: Long)
}

class StoreHouseRepositoryImpl @Inject constructor(
    private val dao: StoreHouseDao
) : StoreHouseRepository {
    override fun getAll() = dao.getAll()
    override suspend fun searchById(id: Long): StoreHouse {
        return dao.searchById(id)
    }

    override suspend fun insert(storeHouse: StoreHouse) = dao.insert(storeHouse)

    override suspend fun updateByCount(id: Long, count: Long) {
        dao.updateByCount(id = id, count = count)
    }

    override suspend fun deleteStoreHouse(id: Long) {
        if(id != 1L){
            dao.deleteById(id)
        }
    }
}