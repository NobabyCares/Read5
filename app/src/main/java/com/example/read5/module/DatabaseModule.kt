package com.example.read5.module

import android.content.Context
import com.example.read5.dao.ItemInfoDao
import com.example.read5.dao.StoreHouseDao
import com.example.read5.db.AppDatabase
import com.example.read5.repository.StoreHouseRepository
import com.example.read5.repository.StoreHouseRepositoryImpl
import com.example.read5.repository.iteminfo.ItemInfoRepository
import com.example.read5.repository.iteminfo.ItemInfoRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule{
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideStoreHouseDao(database: AppDatabase): StoreHouseDao {
        return database.storeHouseDao()
    }

    @Provides
    @Singleton
    fun provideStoreHouseRepository(storeHouseDao: StoreHouseDao): StoreHouseRepository {
        return StoreHouseRepositoryImpl(storeHouseDao)
    }

    @Provides
    @Singleton
    fun provideItemInfoDao(database: AppDatabase): ItemInfoDao {
        return database.itemInfoDao()
    }

    @Provides
    @Singleton
    fun provideItemInfoRepository(itemInfoDao: ItemInfoDao): ItemInfoRepository {
        return ItemInfoRepositoryImpl(itemInfoDao)
    }

}