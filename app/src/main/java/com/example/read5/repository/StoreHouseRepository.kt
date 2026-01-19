package com.example.read5.repository

import com.example.read5.bean.StoreHouse
import com.example.read5.dao.StoreHouseDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


interface StoreHouseRepository {
    fun getAll(): Flow<List<StoreHouse>>
    suspend fun insert(storeHouse: StoreHouse): Long
}

class StoreHouseRepositoryImpl @Inject constructor(
    private val dao: StoreHouseDao
) : StoreHouseRepository {
    override fun getAll() = dao.getAll()
    override suspend fun insert(storeHouse: StoreHouse) = dao.insert(storeHouse)
}