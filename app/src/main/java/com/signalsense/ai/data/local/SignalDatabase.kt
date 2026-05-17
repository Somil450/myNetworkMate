package com.signalsense.ai.data.local

import androidx.room.*
import com.signalsense.ai.domain.model.NetworkType
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "signal_history")
data class SignalReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val towerId: String,
    val carrier: String,
    val networkType: NetworkType,
    val dbm: Int,
    val level: Int,
    val snr: Double? = null,
    val lat: Double,
    val lng: Double
)

@Entity(tableName = "tower_locations")
data class TowerMapEntity(
    @PrimaryKey val towerId: String,
    val lat: Double,
    val lng: Double,
    val carrier: String,
    val lastSeen: Long = System.currentTimeMillis()
)

@Entity(tableName = "speed_tests")
data class SpeedTestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val downloadSpeed: Double,
    val uploadSpeed: Double,
    val latency: Int,
    val jitter: Int,
    val towerId: String,
    val carrier: String
)

@Dao
interface SignalDao {
    @Insert
    suspend fun insertSignal(reading: SignalReadingEntity)

    @Query("SELECT * FROM signal_history ORDER BY timestamp DESC LIMIT 100")
    suspend fun getRecentSignals(): List<SignalReadingEntity>

    @Query("SELECT AVG(dbm) as avgDbm, carrier FROM signal_history GROUP BY carrier")
    suspend fun getCarrierComparison(): List<CarrierAvgSignal>

    @Insert
    suspend fun insertSpeedTest(test: SpeedTestEntity)

    @Query("SELECT * FROM speed_tests ORDER BY timestamp DESC")
    suspend fun getSpeedTestHistory(): List<SpeedTestEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTowerLocation(tower: TowerMapEntity)

    @Query("SELECT * FROM tower_locations")
    fun getAllTowers(): Flow<List<TowerMapEntity>>

    @Query("SELECT * FROM signal_history WHERE lat BETWEEN :minLat AND :maxLat AND lng BETWEEN :minLng AND :maxLng")
    suspend fun getSignalsInArea(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double): List<SignalReadingEntity>
}

data class CarrierAvgSignal(
    val carrier: String,
    val avgDbm: Double
)

class Converters {
    @TypeConverter
    fun fromNetworkType(value: NetworkType) = value.name
    @TypeConverter
    fun toNetworkType(value: String) = NetworkType.valueOf(value)
}

@Database(entities = [SignalReadingEntity::class, SpeedTestEntity::class, TowerMapEntity::class], version = 2)
@TypeConverters(Converters::class)
abstract class SignalDatabase : RoomDatabase() {
    abstract fun signalDao(): SignalDao
}
