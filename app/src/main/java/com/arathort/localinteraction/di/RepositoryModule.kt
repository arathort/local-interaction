package com.arathort.localinteraction.di

import com.arathort.localinteraction.data.repository.AdminRepositoryImpl
import com.arathort.localinteraction.data.repository.ApplicationRepositoryImpl
import com.arathort.localinteraction.data.repository.AuthRepositoryImpl
import com.arathort.localinteraction.data.repository.EventRepositoryImpl
import com.arathort.localinteraction.data.repository.ProfileRepositoryImpl
import com.arathort.localinteraction.data.repository.ReviewRepositoryImpl
import com.arathort.localinteraction.domain.repository.AdminRepository
import com.arathort.localinteraction.domain.repository.ApplicationRepository
import com.arathort.localinteraction.domain.repository.AuthRepository
import com.arathort.localinteraction.domain.repository.EventRepository
import com.arathort.localinteraction.domain.repository.ProfileRepository
import com.arathort.localinteraction.domain.repository.ReviewRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

    @Binds
    @Singleton
    abstract fun bindApplicationRepository(impl: ApplicationRepositoryImpl): ApplicationRepository

    @Binds
    @Singleton
    abstract fun bindReviewRepository(impl: ReviewRepositoryImpl): ReviewRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindAdminRepository(impl: AdminRepositoryImpl): AdminRepository
}
