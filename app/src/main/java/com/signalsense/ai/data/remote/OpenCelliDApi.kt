package com.signalsense.ai.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

// ------- Response Models -------

data class OpenCelliDResponse(
    @SerializedName("count") val count: Int = 0,
    @SerializedName("cells") val cells: List<OpenCelliDCell> = emptyList()
)

data class OpenCelliDCell(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("mcc") val mcc: Int,
    @SerializedName("mnc") val mnc: Int,
    @SerializedName("lac") val lac: Int,
    @SerializedName("cellid") val cellId: Int,
    @SerializedName("averageSignal") val averageSignal: Int = -90,
    @SerializedName("radio") val radio: String = "LTE",
    @SerializedName("samples") val samples: Int = 0
) {
    val carrierName: String get() = when {
        // India MCC = 404, 405
        mnc == 10 || mnc == 67 || mnc == 874 -> "Airtel"
        mnc == 88 || mnc == 883 || mnc == 884 -> "Jio"
        mnc == 20 || mnc == 50 || mnc == 860 -> "Vi"
        mnc == 16 || mnc == 70 -> "BSNL"
        else -> "Unknown"
    }

    val networkType: String get() = when (radio.uppercase()) {
        "NR" -> "5G"
        "LTE" -> "4G"
        "UMTS", "HSPA" -> "3G"
        "GSM", "EDGE" -> "2G"
        else -> "4G"
    }
}

// ------- Retrofit Interface -------

interface OpenCelliDApi {
    @GET("cell/getInArea")
    suspend fun getTowersInArea(
        @Query("key") apiKey: String,
        @Query("BBOX") bbox: String,       // "minLat,minLon,maxLat,maxLon"
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 100
    ): OpenCelliDResponse

    @GET("cell/get")
    suspend fun getCellLocation(
        @Query("key") apiKey: String,
        @Query("mcc") mcc: Int,
        @Query("mnc") mnc: Int,
        @Query("lac") lac: Int,
        @Query("cellid") cellId: Int,
        @Query("format") format: String = "json"
    ): OpenCelliDCell
}
