package com.example.read5.repository.comictype

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.read5.bean.ComicType
import com.example.read5.bean.ItemInfo
import com.example.read5.dao.ComicTypeDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


interface ComicTypeRepositoryApi {
    suspend fun getAll(): Flow<PagingData<ComicType>>

    suspend fun insert(comicTypes: List<ComicType>)
    suspend fun insert(comicTypes: ComicType)

}


class ComicTypeRepository @Inject constructor(
    private val comicTypeDao: ComicTypeDao,
  ): ComicTypeRepositoryApi {

    override suspend fun getAll(): Flow<PagingData<ComicType>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                comicTypeDao.getAll()
            }
        ).flow
    }

    override suspend fun insert(comicTypes: List<ComicType>) {
        comicTypeDao.insert(comicTypes)
    }

    override suspend fun insert(comicTypes: ComicType) {
        comicTypeDao.insert(comicTypes)
    }
}