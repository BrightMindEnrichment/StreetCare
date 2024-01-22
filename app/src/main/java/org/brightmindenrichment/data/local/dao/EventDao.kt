package org.brightmindenrichment.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.brightmindenrichment.street_care.ui.community.model.DatabaseEvent
import org.brightmindenrichment.street_care.util.Constants.EVENT_TABLE

@Dao
interface EventDao {

    @Query("SELECT * FROM $EVENT_TABLE")
    suspend fun getAllEvents(): List<DatabaseEvent>

    @Query("SELECT * FROM $EVENT_TABLE ORDER BY date DESC")
    suspend fun getAllEventsDesc(): List<DatabaseEvent>

    @Query("SELECT * FROM $EVENT_TABLE WHERE id=:id")
    suspend fun getEventById(id: String): DatabaseEvent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addEvent(event: DatabaseEvent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addEvents(events: List<DatabaseEvent>)

    @Update
    suspend fun updateUsers(event: DatabaseEvent): Int

    @Query("DELETE FROM $EVENT_TABLE WHERE id=:id")
    suspend fun deleteEvent(id: String): Int

    @Query("DELETE FROM $EVENT_TABLE")
    suspend fun deleteAllEvents(): Int
}