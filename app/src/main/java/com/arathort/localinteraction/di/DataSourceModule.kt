package com.arathort.localinteraction.di

import com.arathort.localinteraction.data.datasource.LocalInteractionDataSource
import com.arathort.localinteraction.data.datasource.mock.MockLocalInteractionDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun bindLocalInteractionDataSource(
        impl: MockLocalInteractionDataSource
    ): LocalInteractionDataSource
}
