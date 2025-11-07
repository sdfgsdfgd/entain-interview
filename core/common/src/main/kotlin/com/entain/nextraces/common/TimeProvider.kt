@file:OptIn(ExperimentalTime::class)

package com.entain.nextraces.common

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

fun interface TimeProvider {
    fun now(): Instant
}

object DefaultTimeProvider : TimeProvider {
    override fun now(): Instant = Clock.System.now()
}
