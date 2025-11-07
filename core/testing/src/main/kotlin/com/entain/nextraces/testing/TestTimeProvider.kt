package com.entain.nextraces.testing

import com.entain.nextraces.common.TimeProvider
import kotlin.time.Duration

fun TestTimeProvider.advanceBy(duration: Duration) {
    current += duration
}

class TestTimeProvider(initial: kotlin.time.Instant) : TimeProvider {
    var current: kotlin.time.Instant = initial

    override fun now(): kotlin.time.Instant = current
}
