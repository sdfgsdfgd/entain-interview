package com.entain.nextraces.data

import com.entain.nextraces.common.AppResult
import com.entain.nextraces.common.AppError
import com.entain.nextraces.data.mapper.RaceMapper
import com.entain.nextraces.model.RaceCategory
import com.entain.nextraces.model.STALE_RACE_THRESHOLD
import com.entain.nextraces.network.api.AdvertisedStartDto
import com.entain.nextraces.network.api.NextRacesDataDto
import com.entain.nextraces.network.api.NextRacesResponseDto
import com.entain.nextraces.network.api.NedsApiService
import com.entain.nextraces.network.api.RaceSummaryDto
import com.entain.nextraces.testing.TestDispatcherRule
import com.entain.nextraces.testing.TestTimeProvider
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class RaceRepositoryImplTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val mapper = RaceMapper()
    private val timeProvider = TestTimeProvider(Instant.fromEpochSeconds(1_700_000_000))

    @Test
    fun `returns fresh races sorted ascending`() = runTest {
        val service = FakeNedsApiService(response = buildResponse())
        val repository = RaceRepositoryImpl(
            apiService = service,
            raceMapper = mapper,
            dispatcherProvider = dispatcherRule,
            timeProvider = timeProvider
        )

        val result = repository.getNextRaces(emptySet())

        assertTrue(result is AppResult.Success)
        val races = (result as AppResult.Success).value
        assertEquals(5, races.size)
        assertTrue(races.zipWithNext().all { (first, second) ->
            first.advertisedStart <= second.advertisedStart
        })
        assertTrue(races.none { race ->
            timeProvider.now() >= race.advertisedStart + STALE_RACE_THRESHOLD
        })
    }

    @Test
    fun `filters races by selected categories`() = runTest {
        val service = FakeNedsApiService(response = buildResponse())
        val repository = RaceRepositoryImpl(
            apiService = service,
            raceMapper = mapper,
            dispatcherProvider = dispatcherRule,
            timeProvider = timeProvider
        )

        val result = repository.getNextRaces(setOf(RaceCategory.GREYHOUND))

        assertTrue(result is AppResult.Success)
        val races = (result as AppResult.Success).value
        assertTrue(races.all { it.category == RaceCategory.GREYHOUND })
    }

    @Test
    fun `maps network exception to AppError_Network`() = runTest {
        val service = FakeNedsApiService(throwable = IOException("network down"))
        val repository = RaceRepositoryImpl(
            apiService = service,
            raceMapper = mapper,
            dispatcherProvider = dispatcherRule,
            timeProvider = timeProvider
        )

        val result = repository.getNextRaces(emptySet())

        assertTrue(result is AppResult.Error)
        val error = (result as AppResult.Error).error
        assertTrue(error is AppError.Network)
    }

    private fun buildResponse(): NextRacesResponseDto {
        val start = timeProvider.now()
        val ids = listOf("1", "2", "3", "4", "5", "6")
        val summaries = ids.associateWith { id ->
            RaceSummaryDto(
                raceId = id,
                raceName = "Race $id",
                raceNumber = id.toInt(),
                meetingId = "meeting$id",
                meetingName = "Meeting $id",
                categoryId = when (id.toInt() % 3) {
                    0 -> RaceCategory.GREYHOUND.id
                    1 -> RaceCategory.HORSE.id
                    else -> RaceCategory.HARNESS.id
                },
                advertisedStart = AdvertisedStartDto(
                    seconds = when (id) {
                        "1" -> (start + 30.seconds).epochSeconds
                        "2" -> (start + 2.minutes).epochSeconds
                        "3" -> (start + 5.minutes).epochSeconds
                        "4" -> (start + 10.minutes).epochSeconds
                        "5" -> (start + 20.minutes).epochSeconds
                        else -> (start - 2.minutes).epochSeconds
                    }
                )
            )
        }
        return NextRacesResponseDto(
            status = 200,
            data = NextRacesDataDto(
                nextToGoIds = ids,
                raceSummaries = summaries
            )
        )
    }
}

private class FakeNedsApiService(
    private val response: NextRacesResponseDto? = null,
    private val throwable: Throwable? = null
) : NedsApiService {
    override suspend fun getNextRaces(method: String, count: Int): NextRacesResponseDto {
        throwable?.let { throw it }
        return response ?: error("response not set")
    }
}
