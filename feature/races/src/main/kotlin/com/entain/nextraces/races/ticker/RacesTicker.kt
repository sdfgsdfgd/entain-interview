package com.entain.nextraces.races.ticker

import com.entain.nextraces.common.DispatcherProvider
import com.entain.nextraces.common.tickerFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface RacesTicker {
    fun refreshTicks(): Flow<Unit>
    fun countdownTicks(): Flow<Unit>
}

private val REFRESH_INTERVAL: Duration = 15.seconds
private val COUNTDOWN_INTERVAL: Duration = 1.seconds

class DefaultRacesTicker @Inject constructor(
    private val dispatcherProvider: DispatcherProvider
) : RacesTicker {
    override fun refreshTicks(): Flow<Unit> = tickerFlow(
        interval = REFRESH_INTERVAL,
        dispatcher = dispatcherProvider.default,
        emitImmediately = true
    )

    override fun countdownTicks(): Flow<Unit> = tickerFlow(
        interval = COUNTDOWN_INTERVAL,
        dispatcher = dispatcherProvider.default,
        emitImmediately = false
    )
}
