package com.example.mucproject.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("event")
    suspend fun getEvent(): EventResponse

    @POST("fastforward")
    suspend fun fastForward(): EventResponse

    companion object {
        private const val BASE_URL = "http://10.164.59.25:3000/" 

        fun create(): ApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}

data class EventResponse(
    val day: Int,
    val meal: Int
)
