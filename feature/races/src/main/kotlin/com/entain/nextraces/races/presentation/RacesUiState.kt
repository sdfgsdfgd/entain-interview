package com.entain.nextraces.races.presentation

import androidx.annotation.StringRes
import com.entain.nextraces.common.CountdownDisplay
import com.entain.nextraces.model.RaceCategory
import kotlinx.datetime.Instant

data class RacesUiState(
    val items: List<RaceListItem> = emptyList(),
    val selectedFilters: Set<RaceCategory> = emptySet(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val message: UiMessage? = null,
    val lastUpdated: Instant? = null
)

data class UiMessage(@get:StringRes val messageRes: Int)

sealed interface RaceListItem {
    val key: String

    data class RaceCard(
        val id: String,
        val meetingName: String,
        val raceNumber: Int,
        val category: RaceCategory,
        val countdown: CountdownDisplay,
        val advertisedStart: Instant
    ) : RaceListItem {
        override val key: String = id
    }

    data class Placeholder(
        override val key: String
    ) : RaceListItem
}
