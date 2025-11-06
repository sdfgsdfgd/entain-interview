package com.entain.nextraces.common

import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

data class CountdownDisplay(
    val text: String,
    val status: CountdownStatus
) {
    val isCritical: Boolean
        get() = status != CountdownStatus.UPCOMING
}

enum class CountdownStatus {
    UPCOMING,
    STARTING_SOON,
    STARTED
}

object CountdownFormatter {

    fun format(now: Instant, advertisedStart: Instant): CountdownDisplay {
        val diff: Duration = advertisedStart - now
        return when {
            diff <= Duration.ZERO -> CountdownDisplay(text = "Started", status = CountdownStatus.STARTED)
            diff < 1.minutes -> CountdownDisplay(
                text = "${diff.inWholeSeconds}s",
                status = CountdownStatus.STARTING_SOON
            )
            diff < 1.hours -> {
                val minutes = diff.inWholeMinutes
                val seconds = (diff - minutes.minutes).inWholeSeconds
                CountdownDisplay(
                    text = "${minutes}m ${seconds.toString().padStart(2, '0')}s",
                    status = if (minutes < 5) CountdownStatus.STARTING_SOON else CountdownStatus.UPCOMING
                )
            }

            else -> {
                val hours = diff.inWholeHours
                val minutes = ((diff - hours.hours).inWholeMinutes).toInt()
                CountdownDisplay(
                    text = "${hours}h ${minutes.toString().padStart(2, '0')}m",
                    status = CountdownStatus.UPCOMING
                )
            }
        }
    }
}
