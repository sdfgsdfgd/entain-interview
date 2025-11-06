package com.entain.nextraces.model

import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

const val MAX_RACES_DISPLAY = 5

val STALE_RACE_THRESHOLD: Duration = 1.minutes

enum class RaceCategory(val id: String) {
    HORSE("4a2788f8-e825-4d36-9894-efd4baf1cfae"),
    HARNESS("161d9be2-e909-4326-8c2c-35ed71fb460b"),
    GREYHOUND("9daef0d7-bf3c-4f50-921d-8e818c60fe61");

    companion object {
        private val byId: Map<String, RaceCategory> = entries.associateBy { it.id }

        fun fromId(id: String): RaceCategory? = byId[id]
    }
}

data class Race(
    val id: String,
    val meetingName: String,
    val raceNumber: Int,
    val category: RaceCategory,
    val advertisedStart: Instant
)
