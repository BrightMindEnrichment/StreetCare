package org.brightmindenrichment.street_care.ui.visit.data

import android.os.Build.VERSION_CODES.O
import android.widget.TimePicker
import java.sql.Time
import java.util.*
import java.util.Calendar.*

 class VisitLog {

    var location: String = ""
    var date: Date = getInstance().time
    var food_drink: String = "N"
    var clothes: String = "N"
    var hygine: String = "N"
    var names: String = "NA"
    var wellness: String = "N"
    var other: String = "N"
    var otherDetail:String = "NA"
    var peopleCount: Long = 0L
    var experience: String = ""
    var comments: String = "NA"
    var visitAgain : String =""
    var outreach :Long = 0L
    var peopleHelped: Int = 9
    var share: Boolean=false

   // var comments: String = ""
    var whenVisit: String? = null
    var whereVisit: String? = null
    var userId: String? = null

}