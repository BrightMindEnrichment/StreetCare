package org.brightmindenrichment.street_care.ui.community.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import org.brightmindenrichment.street_care.util.Constants.EVENT_TABLE

@Entity(tableName = EVENT_TABLE)
data class DatabaseEvent(
    @PrimaryKey(autoGenerate = false) val id: String,
    var date: String?,
    var description: String?,
    var interest: Int?,
    var location: String?,
    var status: String?,
    var title: String?,
)
