@file:OptIn(ExperimentalTime::class)

package com.entain.nextraces.races.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entain.nextraces.common.AppError
import com.entain.nextraces.common.AppResult
import com.entain.nextraces.common.CountdownFormatter
import com.entain.nextraces.common.DispatcherProvider
import com.entain.nextraces.common.TimeProvider
import com.entain.nextraces.data.RaceRepository
import com.entain.nextraces.model.MAX_RACES_DISPLAY
import com.entain.nextraces.model.Race
import com.entain.nextraces.model.RaceCategory
import com.entain.nextraces.races.R
import com.entain.nextraces.races.ticker.RacesTicker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@HiltViewModel
class RacesViewModel @Inject constructor(
    private val repository: RaceRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val timeProvider: TimeProvider,
    racesTicker: RacesTicker
) : ViewModel() {

    private val selectedFilters = MutableStateFlow<Set<RaceCategory>>(emptySet())
    private val refreshEvents = racesTicker.refreshTicks()
    private val countdownEvents = racesTicker.countdownTicks()

    private val _state = MutableStateFlow(RacesUiState())
    val state = _state.asStateFlow()

    private var latestRaces: List<Race> = emptyList()

    init {
        observeRefreshes()
        observeCountdowns()
    }

    fun onFilterToggled(category: RaceCategory) {
        val updated = selectedFilters.updateAndGet { current ->
            if (category in current) current - category else current + category
        }
        _state.update { it.copy(selectedFilters = updated) }
    }

    fun onClearFilters() {
        selectedFilters.value = emptySet()
        _state.update { it.copy(selectedFilters = emptySet()) }
    }

    fun onMessageShown() {
        _state.update { it.copy(message = null) }
    }

    private fun observeRefreshes() {
        viewModelScope.launch(dispatcherProvider.main) {
            combine(
                selectedFilters,
                refreshEvents
            ) { filters, _ -> filters }
                .collectLatest { filters ->
                    fetchRaces(filters)
                }
        }
    }

    private fun observeCountdowns() {
        viewModelScope.launch(dispatcherProvider.default) {
            countdownEvents.collect {
                val races = latestRaces
                if (races.isEmpty()) return@collect
                updateItems(races)
            }
        }
    }

    private suspend fun fetchRaces(filters: Set<RaceCategory>) {
        markLoading()
        when (val result = repository.getNextRaces(filters)) {
            is AppResult.Success -> {
                latestRaces = result.value
                updateItems(result.value)
                _state.update { state ->
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        message = null,
                        lastUpdated = timeProvider.now()
                    )
                }
            }

            is AppResult.Error -> {
                _state.update { state ->
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        message = UiMessage(result.error.toMessageRes())
                    )
                }
            }
        }
    }

    private fun markLoading() {
        _state.update { state ->
            val initialLoad = state.items.isEmpty()
            state.copy(
                isLoading = initialLoad,
                isRefreshing = !initialLoad
            )
        }
    }

    private fun updateItems(races: List<Race>) {
        val now = timeProvider.now()
        val cards = races.map { race ->
            RaceListItem.RaceCard(
                id = race.id,
                meetingName = race.meetingName,
                raceNumber = race.raceNumber,
                category = race.category,
                countdown = CountdownFormatter.format(now, race.advertisedStart),
                advertisedStart = race.advertisedStart
            )
        }
        val placeholdersNeeded = (MAX_RACES_DISPLAY - cards.size).coerceAtLeast(0)
        val placeholders = List(placeholdersNeeded) { index ->
            RaceListItem.Placeholder(key = "placeholder-$index")
        }
        _state.update { state ->
            state.copy(items = cards + placeholders, selectedFilters = selectedFilters.value)
        }
    }

    private fun AppError.toMessageRes(): Int = when (this) {
        is AppError.Network -> R.string.error_network
        is AppError.Serialization, is AppError.Server -> R.string.error_server
        is AppError.Unknown -> R.string.error_unknown
    }
}
