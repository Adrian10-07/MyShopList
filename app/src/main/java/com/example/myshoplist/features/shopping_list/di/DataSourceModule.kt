package com.example.myshoplist.features.shopping_list.di

import com.example.myshoplist.features.shopping_list.framework.fcm.FirestoreListDataSource
import com.example.myshoplist.features.shopping_list.domain.repository.RemoteListDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    abstract fun bindRemoteListDataSource(
        firestoreDataSource: FirestoreListDataSource
    ): RemoteListDataSource
}