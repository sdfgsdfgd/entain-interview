package com.entain.nextraces.common

import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class CountdownFormatterTest {

    private val now = Instant.fromEpochSeconds(1_700_000_000)

    @Test
    fun `formats minutes and seconds`() {
        val target = now + 2.minutes + 15.seconds

        val display = CountdownFormatter.format(now, target)

        assertEquals("2m 15s", display.text)
        assertEquals(CountdownStatus.STARTING_SOON, display.status)
    }

    @Test
    fun `formats hours`() {
        val target = now + 3.hours + 10.minutes

        val display = CountdownFormatter.format(now, target)

        assertEquals("3h 10m", display.text)
        assertEquals(CountdownStatus.UPCOMING, display.status)
    }

    @Test
    fun `indicates started when time passed`() {
        val target = now - 5.seconds

        val display = CountdownFormatter.format(now, target)

        assertEquals("Started", display.text)
        assertEquals(CountdownStatus.STARTED, display.status)
    }
}
