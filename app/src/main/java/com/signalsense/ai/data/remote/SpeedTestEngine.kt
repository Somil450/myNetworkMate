package com.signalsense.ai.data.remote

import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@Singleton
class SpeedTestEngine @Inject constructor() {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val TEST_URL = "https://speed.cloudflare.com/__down?bytes=50000000" // 50MB test file for accurate 5G speeds


    fun runDownloadTest(): Flow<SpeedResult> = flow {
        val startTime = System.currentTimeMillis()
        var bytesRead = 0L
        
        val request = Request.Builder().url(TEST_URL).build()
        
        try {
            val response = client.newCall(request).execute()
            val body = response.body ?: throw IOException("Empty body")
            val inputStream = body.byteStream()
            val buffer = ByteArray(8192)
            var read: Int
            
            while (inputStream.read(buffer).also { read = it } != -1) {
                bytesRead += read
                val currentTime = System.currentTimeMillis()
                val duration = (currentTime - startTime) / 1000.0
                if (duration > 0) {
                    val speedMbps = (bytesRead * 8.0) / (duration * 1_000_000.0)
                    emit(SpeedResult.Progress(speedMbps))
                }
                if (duration > 10) break // Cap test at 10 seconds to save data
            }
            
            val finalDuration = (System.currentTimeMillis() - startTime) / 1000.0
            val finalSpeed = (bytesRead * 8.0) / (finalDuration * 1_000_000.0)
            emit(SpeedResult.Completed(finalSpeed))
        } catch (e: Exception) {
            emit(SpeedResult.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun measureLatency(): LatencyResult = withContext(Dispatchers.IO) {
        val latencies = mutableListOf<Long>()
        repeat(5) {
            val start = System.nanoTime()
            try {
                val request = Request.Builder().url("https://1.1.1.1").head().build()
                client.newCall(request).execute().close()
                latencies.add((System.nanoTime() - start) / 1_000_000)
            } catch (e: Exception) {}
        }
        
        if (latencies.isEmpty()) return@withContext LatencyResult(0, 0)
        
        val avg = latencies.average().toInt()
        val jitter = if (latencies.size > 1) {
            latencies.zipWithNext { a, b -> Math.abs(a - b) }.average().toInt()
        } else 0
        
        LatencyResult(avg, jitter)
    }
}

sealed class SpeedResult {
    data class Progress(val speedMbps: Double) : SpeedResult()
    data class Completed(val speedMbps: Double) : SpeedResult()
    data class Error(val message: String) : SpeedResult()
}

data class LatencyResult(val pingMs: Int, val jitterMs: Int)
