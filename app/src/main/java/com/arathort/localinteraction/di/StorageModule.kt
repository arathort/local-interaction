package com.arathort.localinteraction.di

import com.arathort.localinteraction.data.datasource.local.DataStoreTokenStorage
import com.arathort.localinteraction.data.datasource.local.TokenStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {

    @Binds
    @Singleton
    abstract fun bindTokenStorage(impl: DataStoreTokenStorage): TokenStorage
}
