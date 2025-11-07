@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package com.entain.nextraces.races

import com.entain.nextraces.common.AppError
import com.entain.nextraces.common.AppResult
import com.entain.nextraces.data.RaceRepository
import com.entain.nextraces.model.Race
import com.entain.nextraces.model.RaceCategory
import com.entain.nextraces.races.presentation.RaceListItem.Placeholder
import com.entain.nextraces.races.presentation.RaceListItem.RaceCard
import com.entain.nextraces.races.presentation.RacesViewModel
import com.entain.nextraces.races.ticker.RacesTicker
import com.entain.nextraces.testing.TestDispatcherRule
import com.entain.nextraces.testing.TestTimeProvider
import com.entain.nextraces.testing.advanceBy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class RacesViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val timeProvider = TestTimeProvider(Instant.fromEpochSeconds(1_700_000_000))
    private val repository = FakeRaceRepository()
    private lateinit var ticker: FakeRacesTicker

    @Test
    fun `loads races on init and fills placeholders`() = runTest(dispatcherRule.dispatcher) {
        repository.races = buildRaces()

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(5, state.items.size)
        assertEquals(3, state.items.count { it is RaceCard })
        assertEquals(2, state.items.count { it is Placeholder })
    }

    @Test
    fun `toggles filters and requests repository with selection`() = runTest(dispatcherRule.dispatcher) {
        repository.races = buildRaces()
        val viewModel = createViewModel()

        advanceUntilIdle()
        viewModel.onFilterToggled(RaceCategory.GREYHOUND)
        advanceUntilIdle()

        assertTrue(repository.lastFilter!!.contains(RaceCategory.GREYHOUND))
        assertEquals(1, repository.lastFilter!!.size)
        assertTrue(viewModel.state.value.selectedFilters.contains(RaceCategory.GREYHOUND))
    }

    @Test
    fun `shows error message when repository fails`() = runTest(dispatcherRule.dispatcher) {
        repository.result = AppResult.Error(AppError.Network())

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.message != null)
    }

    @Test
    fun `updates countdown text as time advances`() = runTest(dispatcherRule.dispatcher) {
        repository.races = buildRaces()
        val viewModel = createViewModel()

        advanceUntilIdle()
        val initialText = viewModel.state.value.items.first().let {
            (it as RaceCard).countdown.text
        }

        timeProvider.advanceBy(30.seconds)
        ticker.emitCountdown()
        advanceUntilIdle()

        val updatedText = viewModel.state.value.items.first().let {
            (it as RaceCard).countdown.text
        }
        assertTrue(initialText != updatedText)
    }

    private fun createViewModel(): RacesViewModel {
        ticker = FakeRacesTicker()
        return RacesViewModel(
            repository = repository,
            dispatcherProvider = dispatcherRule,
            timeProvider = timeProvider,
            racesTicker = ticker
        )
    }

    private fun buildRaces(): List<Race> {
        val base = timeProvider.now()
        return listOf(
            Race(
                id = "r1",
                meetingName = "Meeting 1",
                raceNumber = 1,
                category = RaceCategory.HORSE,
                advertisedStart = base + 5.minutes
            ),
            Race(
                id = "r2",
                meetingName = "Meeting 2",
                raceNumber = 2,
                category = RaceCategory.GREYHOUND,
                advertisedStart = base + 2.minutes
            ),
            Race(
                id = "r3",
                meetingName = "Meeting 3",
                raceNumber = 3,
                category = RaceCategory.HARNESS,
                advertisedStart = base + 30.minutes
            )
        )
    }

}

private class FakeRaceRepository : RaceRepository {
    var races: List<Race> = emptyList()
    var result: AppResult<List<Race>>? = null
    var lastFilter: Set<RaceCategory>? = null

    override suspend fun getNextRaces(categoryFilter: Set<RaceCategory>): AppResult<List<Race>> {
        lastFilter = categoryFilter
        return result ?: AppResult.Success(races)
    }
}

private class FakeRacesTicker : RacesTicker {
    private val refreshFlow = MutableSharedFlow<Unit>(replay = 1).apply {
        tryEmit(Unit)
    }
    private val countdownFlow = MutableSharedFlow<Unit>()

    override fun refreshTicks() = refreshFlow
    override fun countdownTicks() = countdownFlow

    suspend fun emitCountdown() {
        countdownFlow.emit(Unit)
    }
}
