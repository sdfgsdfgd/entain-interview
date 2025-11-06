package com.entain.nextraces.common

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

fun interface TimeProvider {
    fun now(): Instant
}

object DefaultTimeProvider : TimeProvider {
    override fun now(): Instant = Clock.System.now()
}
