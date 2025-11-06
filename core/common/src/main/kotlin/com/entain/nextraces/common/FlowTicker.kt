package com.entain.nextraces.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlin.time.Duration

fun tickerFlow(
    interval: Duration,
    dispatcher: CoroutineDispatcher,
    emitImmediately: Boolean = true,
    maxTicks: Int? = null
): Flow<Unit> = flow {
    var emitted = 0
    if (emitImmediately) {
        emit(Unit)
        emitted++
        if (maxTicks != null && emitted >= maxTicks) return@flow
    }
    while (currentCoroutineContext().isActive) {
        delay(interval)
        emit(Unit)
        emitted++
        if (maxTicks != null && emitted >= maxTicks) return@flow
    }
}.flowOn(dispatcher)
