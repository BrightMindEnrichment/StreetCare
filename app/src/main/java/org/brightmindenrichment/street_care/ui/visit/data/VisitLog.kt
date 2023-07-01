package org.brightmindenrichment.street_care.ui.visit.data

import java.util.*

 class VisitLog {

    var location: String = ""
    var date: Date = Calendar.getInstance().time
    var hours: Long = 0L
    var food_drink: String = "N"
    var clothes: String = "N"
    var hygine: String = "N"
    var names: String = "NA"
    var wellness: String = "N"
    var other: String = "N"
    var otherDetail:String =""
    var peopleCount: Long = 0L
    var experience: String = ""
    var comments: String = "NA"
    var visitAgain : String =""

}