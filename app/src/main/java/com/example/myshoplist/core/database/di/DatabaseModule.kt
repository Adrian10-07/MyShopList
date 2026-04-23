package com.example.myshoplist.core.database.di

import android.content.Context
import androidx.room.Room
import com.example.myshoplist.core.database.AppDatabase
import com.example.myshoplist.core.database.MIGRATION_1_2
import com.example.myshoplist.core.database.MIGRATION_2_3
import com.example.myshoplist.core.database.MIGRATION_3_4
import com.example.myshoplist.core.database.PurchaseHistory.dao.PurchaseHistoryDao
import com.example.myshoplist.core.database.PurchaseHistory.dao.PurchaseLocationDao
import com.example.myshoplist.core.database.product.dao.ProductDao
import com.example.myshoplist.core.database.profile.dao.UserProfileDao
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "myshoplist_db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()

    @Provides
    fun provideProductDao(db: AppDatabase): ProductDao = db.productDao()

    @Provides
    @Singleton
    fun providePurchaseLocationDao(db: AppDatabase): PurchaseLocationDao =
        db.purchaseLocationDao()

    @Provides
    @Singleton
    fun provideUserProfileDao(db: AppDatabase): UserProfileDao =
         db.userProfileDao()

    @Provides
    @Singleton
    fun providePurchaseHistoryDao(db: AppDatabase): PurchaseHistoryDao =
        db.purchaseHistoryDao()

}