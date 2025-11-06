@file:SuppressLint("UnsafeOptInUsageError")

package com.entain.nextraces.network.api


import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NextRacesResponseDto(
    val status: Int,
    val data: NextRacesDataDto
)

@Serializable
data class NextRacesDataDto(
    @SerialName("next_to_go_ids")
    val nextToGoIds: List<String>,
    @SerialName("race_summaries")
    val raceSummaries: Map<String, RaceSummaryDto>
)

@Serializable
data class RaceSummaryDto(
    @SerialName("race_id")
    val raceId: String,
    @SerialName("race_name")
    val raceName: String? = null,
    @SerialName("race_number")
    val raceNumber: Int,
    @SerialName("meeting_id")
    val meetingId: String,
    @SerialName("meeting_name")
    val meetingName: String,
    @SerialName("category_id")
    val categoryId: String,
    @SerialName("advertised_start")
    val advertisedStart: AdvertisedStartDto
)

@Serializable
data class AdvertisedStartDto(
    val seconds: Long
)
