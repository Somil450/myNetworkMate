package com.signalsense.ai.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

// ------- MLS Request Models -------

data class MLSRequest(
    @SerializedName("cellTowers") val cellTowers: List<MLSCellTower>,
    @SerializedName("considerIp") val considerIp: Boolean = false
)

data class MLSCellTower(
    @SerializedName("radioType") val radioType: String,  // "lte", "nr", "gsm", "wcdma"
    @SerializedName("mobileCountryCode") val mcc: Int,
    @SerializedName("mobileNetworkCode") val mnc: Int,
    @SerializedName("locationAreaCode") val lac: Int,
    @SerializedName("cellId") val cellId: Int,
    @SerializedName("signalStrength") val signalStrength: Int
)

// ------- MLS Response Models -------

data class MLSResponse(
    @SerializedName("location") val location: MLSLocation?,
    @SerializedName("accuracy") val accuracy: Double = 0.0
)

data class MLSLocation(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)

// ------- OpenCelliD Area Response (fallback, no key needed for basic use) -------

data class OpenCelliDResponse(
    @SerializedName("count") val count: Int = 0,
    @SerializedName("cells") val cells: List<OpenCelliDCell> = emptyList()
)

data class OpenCelliDCell(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("mcc") val mcc: Int = 0,
    @SerializedName("mnc") val mnc: Int = 0,
    @SerializedName("lac") val lac: Int = 0,
    @SerializedName("cellid") val cellId: Int = 0,
    @SerializedName("averageSignal") val averageSignal: Int = -90,
    @SerializedName("radio") val radio: String = "LTE",
    @SerializedName("samples") val samples: Int = 0
) {
    val carrierName: String get() = when {
        mnc == 10 || mnc == 67 -> "Airtel"
        mnc == 88 || mnc == 883 -> "Jio"
        mnc == 20 || mnc == 50 -> "Vi"
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

    val id: String get() = "$mcc-$mnc-$lac-$cellId"

    val estimatedSpeedMbps: Int get() {
        val baseSpeed = when (networkType) {
            "5G" -> 300
            "4G" -> 50
            "3G" -> 10
            "2G" -> 1
            else -> 10
        }
        val signalFactor = (averageSignal + 110).coerceIn(0, 40)
        val variation = (cellId % 20) - 10
        return (baseSpeed * (signalFactor / 20.0)).toInt().coerceAtLeast(1) + variation
    }
}

// ------- Retrofit Interface: Mozilla Location Services (No API Key!) -------

interface MLSApi {
    @POST("v1/geolocate")
    suspend fun geolocate(
        @Query("key") key: String = "test",    // "test" works for development
        @Body request: MLSRequest
    ): MLSResponse
}

interface OpenCelliDApi {
    @retrofit2.http.GET("cell/getInArea")
    suspend fun getTowersInArea(
        @Query("key") apiKey: String,
        @Query("BBOX") bbox: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 100
    ): OpenCelliDResponse
}
