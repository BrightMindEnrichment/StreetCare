package org.brightmindenrichment.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import org.brightmindenrichment.data.local.dao.EventDao
import org.brightmindenrichment.street_care.ui.community.model.DatabaseEvent


@Database(entities = [DatabaseEvent::class], version = 1)
abstract class EventsDatabase: RoomDatabase() {
    abstract fun eventDao(): EventDao
}
