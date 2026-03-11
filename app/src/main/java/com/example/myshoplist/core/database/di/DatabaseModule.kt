package com.example.myshoplist.core.database.di

import android.content.Context
import androidx.room.Room
import com.example.myshoplist.core.database.AppDatabase
import com.example.myshoplist.core.database.product.dao.ProductDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "myshoplist_db"
        ).build()
    }

    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao {
        return database.productDao()
    }
}