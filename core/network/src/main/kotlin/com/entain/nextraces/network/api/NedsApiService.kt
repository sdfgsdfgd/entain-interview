package com.entain.nextraces.network.api

import retrofit2.http.GET
import retrofit2.http.Query

interface NedsApiService {
    @GET("rest/v1/racing/")
    suspend fun getNextRaces(
        @Query("method") method: String = "nextraces",
        @Query("count") count: Int = 10
    ): NextRacesResponseDto
}
