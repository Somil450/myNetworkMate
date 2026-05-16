package com.signalsense.ai.core.di

import android.content.Context
import androidx.room.Room
import com.signalsense.ai.data.local.SignalDao
import com.signalsense.ai.data.local.SignalDatabase
import com.signalsense.ai.data.telephony.TelephonyTracker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
}
