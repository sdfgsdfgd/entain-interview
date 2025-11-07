@file:OptIn(ExperimentalTime::class)

package com.entain.nextraces.data.mapper

import com.entain.nextraces.model.Race
import com.entain.nextraces.model.RaceCategory
import com.entain.nextraces.network.api.NextRacesDataDto
import com.entain.nextraces.network.api.RaceSummaryDto
import javax.inject.Inject
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class RaceMapper @Inject constructor() {

    fun map(data: NextRacesDataDto): List<Race> {
        return data.nextToGoIds.mapNotNull { raceId ->
            val summary = data.raceSummaries[raceId] ?: return@mapNotNull null
            map(summary)
        }
    }

    private fun map(summary: RaceSummaryDto): Race? {
        val category = RaceCategory.fromId(summary.categoryId) ?: return null
        val advertisedStart = Instant.fromEpochSeconds(summary.advertisedStart.seconds)
        return Race(
            id = summary.raceId,
            meetingName = summary.meetingName,
            raceNumber = summary.raceNumber,
            category = category,
            advertisedStart = advertisedStart
        )
    }
}
