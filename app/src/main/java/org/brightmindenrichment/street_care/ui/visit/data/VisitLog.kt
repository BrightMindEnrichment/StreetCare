package org.brightmindenrichment.street_care.ui.visit.data

import java.util.*

 class VisitLog {

    var location: String = ""
    var date: Date = Calendar.getInstance().time
    var hours: Long? = 0L
    var visitAgain: String = ""
    var peopleCount: Long = 0L
    var experience: String = ""
    var comments: String = ""

}