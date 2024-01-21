package org.brightmindenrichment.street_care.di

import android.app.PendingIntent
import android.content.Context
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.brightmindenrichment.data.local.EventsDatabase
import org.brightmindenrichment.street_care.MainActivity
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.util.Constants.EVENT_DATABASE
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): EventsDatabase {
        return Room.databaseBuilder(
            context,
            EventsDatabase::class.java,
            EVENT_DATABASE
        ).build()
    }
}