package com.entain.nextraces.races.di

import com.entain.nextraces.races.ticker.DefaultRacesTicker
import com.entain.nextraces.races.ticker.RacesTicker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RacesModule {

    @Binds
    @Singleton
    fun bindRacesTicker(
        impl: DefaultRacesTicker
    ): RacesTicker
}
