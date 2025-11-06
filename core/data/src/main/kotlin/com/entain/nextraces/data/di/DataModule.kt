package com.entain.nextraces.data.di

import com.entain.nextraces.common.DefaultDispatcherProvider
import com.entain.nextraces.common.DefaultTimeProvider
import com.entain.nextraces.common.DispatcherProvider
import com.entain.nextraces.common.TimeProvider
import com.entain.nextraces.data.RaceRepository
import com.entain.nextraces.data.RaceRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindRaceRepository(
        impl: RaceRepositoryImpl
    ): RaceRepository

    companion object {
        @Provides
        @Singleton
        fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider

        @Provides
        @Singleton
        fun provideTimeProvider(): TimeProvider = DefaultTimeProvider
    }
}
