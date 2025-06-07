package org.brightmindenrichment.street_care.ui.visit.data

import java.util.Calendar.getInstance
import java.util.Date
import android.os.Build.VERSION_CODES.O
import android.os.Parcelable
import android.widget.TimePicker
import kotlinx.parcelize.Parcelize
import java.sql.Time
import java.util.*
import java.util.Calendar.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Parcelize
data class VisitLog(
    var id: String = "",
    var location: String = "",
    var date: Date = getInstance().time,
    var lastEditedTime: Date = getInstance().time,
    var createdTime: Date = getInstance().time,
    var food_drink: String = "N",
    var clothes: String = "N",
    var hygiene: String = "N",
    var names: String = "NA",
    var peopleHelpedDescription: String = "NA",
    var description:String = "NA",
    var itemQtyDescription:String  ="NA",
    var wellness: String = "N",
    var other: String = "N",
    var otherDetail:String = "NA",
    var peopleCount: Long = 0L,
    var experience: Int = 0,
    var comments: String = "",
    var visitAgain : String ="NA",
    var outreach :Long = 0L,
    var numberOfHelpersComment: String ="NA",
    var share: Boolean=false,
    var timeZone: String? = TimeZone.getDefault().id,

    var isFlagged: Boolean?= false,
    var flaggedByUser: String?= null,
    // var comments: String = ""
    var whereVisit: String = "",
    var whenVisit: String? = null,
    var whenVisitTime: String? = null,
    var userId: String? = null,

    var helpTime: String? = "NA",
    var followupDate: Date = getInstance().time,
    var addnames: String = "NA",
    var address: String = "NA",

    var add_food_drink: String = "N",
    var add_clothes: String = "N",
    var add_hygine: String = "N",
    var add_wellness: String = "N",
    var add_medicalhelp: String = "N",
    var add_socialWorker: String = "N",
    var add_lawyerLegal: String = "N",
    var add_other: String = "N",
    var add_otherDetail:String = "NA",
    var add_volunteerDetail: String = "NA",
    var number_of_items: Long = 0L,

    var medicalhelp: String = "N",
    var socialWorker: String = "N",
    var lawyerLegal: String = "N",
    var whattogive: ArrayList<String> = arrayListOf(),
    var whatrequired: ArrayList<String> = arrayListOf(),
    //var locationmap: HashMap<String, String> = hashMapOf(),
    var typeofdevice: String = "Android",
    var outreachHours: Int = 0,
    var outreachMinutes: Int =0,
    var locationDescription: String = "NA",
    var visitedHours: Int =0,
    var visitedMinutes: Int=0,
    var whoJoined: Int=0,
    var stillNeedSupport: Int=0,
    var supportTypeNeeded: String ="NA",
    var peopleNeedFurtherHelpLocation: String ="NA",
    var futureNotes: String ="NA",
    //document ID for updating
    var documentId: String? = null,

    var peopleHelped: Int = 0,
    var whatGiven: String? = null,
    var whatGivenFurther: String? = null


    ) : Parcelable {




}