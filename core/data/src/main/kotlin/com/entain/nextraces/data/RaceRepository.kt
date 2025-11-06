package com.entain.nextraces.data

import com.entain.nextraces.common.AppResult
import com.entain.nextraces.model.Race
import com.entain.nextraces.model.RaceCategory

interface RaceRepository {
    suspend fun getNextRaces(
        categoryFilter: Set<RaceCategory>
    ): AppResult<List<Race>>
}
