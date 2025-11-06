package com.entain.nextraces.data

import android.util.Log
import com.entain.nextraces.common.AppError
import com.entain.nextraces.common.AppResult
import com.entain.nextraces.common.DispatcherProvider
import com.entain.nextraces.common.TimeProvider
import com.entain.nextraces.data.mapper.RaceMapper
import com.entain.nextraces.model.MAX_RACES_DISPLAY
import com.entain.nextraces.model.Race
import com.entain.nextraces.model.RaceCategory
import com.entain.nextraces.model.STALE_RACE_THRESHOLD
import com.entain.nextraces.network.api.NedsApiService
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import java.io.IOException
import javax.inject.Inject

private const val DEFAULT_COUNT = 10
private const val FETCH_INCREMENT = 10
private const val MAX_FETCH_COUNT = 50

class RaceRepositoryImpl @Inject constructor(
    private val apiService: NedsApiService,
    private val raceMapper: RaceMapper,
    private val dispatcherProvider: DispatcherProvider,
    private val timeProvider: TimeProvider
) : RaceRepository {

    override suspend fun getNextRaces(
        categoryFilter: Set<RaceCategory>
    ): AppResult<List<Race>> = withContext(dispatcherProvider.io) {
        logDebug("XXX", "Requested filters: $categoryFilter")
        val now = timeProvider.now()
        var fetchCount = DEFAULT_COUNT

        while (fetchCount <= MAX_FETCH_COUNT) {
            val response = runCatching { apiService.getNextRaces(count = fetchCount) }
                .getOrElse { throwable ->
                    val error = when (throwable) {
                        is IOException -> AppError.Network(throwable)
                        is SerializationException -> AppError.Serialization(throwable)
                        else -> AppError.Unknown(throwable)
                    }
                    return@withContext AppResult.Error(error)
                }

            if (response.status != 200) {
                return@withContext AppResult.Error(AppError.Server(response.status))
            }

            val domainRaces = raceMapper.map(response.data)
            logDebug(
                "XXX",
                "Fetched ($fetchCount) races: ${
                    domainRaces.joinToString { race ->
                        "${race.meetingName} (${race.category})"
                    }
                }"
            )

            val filtered = domainRaces
                .filter { race -> race.advertisedStart + STALE_RACE_THRESHOLD > now }
                .let { races ->
                    if (categoryFilter.isEmpty()) races else races.filter { it.category in categoryFilter }
                }
                .sortedBy(Race::advertisedStart)

            logDebug(
                "XXX",
                "After filtering: ${
                    filtered.joinToString { race ->
                        "${race.meetingName} (${race.category})"
                    }
                }"
            )

            if (filtered.size >= MAX_RACES_DISPLAY || fetchCount >= MAX_FETCH_COUNT) {
                val result = filtered.take(MAX_RACES_DISPLAY)
                logDebug("XXX", "Returning ${result.size} races (requested count=$fetchCount)")
                return@withContext AppResult.Success(result)
            }

            fetchCount += FETCH_INCREMENT
        }

        logDebug("XXX", "No races returned after reaching max fetch count")
        AppResult.Success(emptyList())
    }
}

private fun logDebug(tag: String, message: String) {
    try {
        Log.d(tag, message)
    } catch (_: RuntimeException) {
        println("$tag: $message")
    }
}
