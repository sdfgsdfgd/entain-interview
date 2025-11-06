package com.entain.nextraces.testing

import com.entain.nextraces.common.TimeProvider
import kotlinx.datetime.Instant
import kotlin.time.Duration

fun TestTimeProvider.advanceBy(duration: Duration) {
    current += duration
}

class TestTimeProvider(initial: Instant) : TimeProvider {
    var current: Instant = initial

    override fun now(): Instant = current
}
