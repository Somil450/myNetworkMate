package com.signalsense.ai.core.di

import android.content.Context
import androidx.room.Room
import com.signalsense.ai.data.local.SignalDao
import com.signalsense.ai.data.local.SignalDatabase
import com.signalsense.ai.data.location.LocationTracker
import com.signalsense.ai.data.remote.MLSApi
import com.signalsense.ai.data.remote.OpenCelliDApi
import com.signalsense.ai.data.telephony.TelephonyTracker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTelephonyTracker(
        @ApplicationContext context: Context
    ): TelephonyTracker = TelephonyTracker(context)

    @Provides
    @Singleton
    fun provideLocationTracker(
        @ApplicationContext context: Context
    ): LocationTracker = LocationTracker(context)

    @Provides
    @Singleton
    fun provideSignalDatabase(
        @ApplicationContext context: Context
    ): SignalDatabase {
        return Room.databaseBuilder(
            context,
            SignalDatabase::class.java,
            "signal_sense_db"
        ).build()
    }

    @Provides
    fun provideSignalDao(database: SignalDatabase): SignalDao {
        return database.signalDao()
    }

    @Provides
    @Singleton
    fun provideMlsRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://location.services.mozilla.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideMlsApi(retrofit: Retrofit): MLSApi =
        retrofit.create(MLSApi::class.java)

    @Provides
    @Singleton
    fun provideOpenCelliDApi(): OpenCelliDApi =
        Retrofit.Builder()
            .baseUrl("https://opencellid.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenCelliDApi::class.java)
}
