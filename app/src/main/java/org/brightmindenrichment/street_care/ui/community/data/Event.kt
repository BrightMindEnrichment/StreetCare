package org.brightmindenrichment.street_care.ui.community.data

import android.content.ContentValues
import android.util.Log
import org.brightmindenrichment.street_care.util.Extensions

class Event {

    var eventId: String? = null
    lateinit var title: String
    var description: String? = null
    var location: String? = null
    var date: String? = null
    var approved:Boolean?=null
    var interest: Int? = 0
    var signedUp = false
    var time: String? = null
    var uid: String? = null
    var day: String? = null
    var month: String? = null
    var year: String? = null
    var layoutType: Int? = 0
    var itemList: MutableList<String> = mutableListOf()
    var timestamp: String? = null
    // for outreachEvents collection
    var eventStartTime: String? = null
    var eventEndTime: String? = null
    var createdAt: String? = null
    var helpRequest: List<String?>? = null
    var helpType: String? = null
    var participants: MutableList<String?>? = null
    var skills: List<String?>? = null
    var totalSlots: Int? = null
    var requiredSkillsBooleanArray = BooleanArray(Extensions.requiredSkills.size){ false }


    fun addValue(value:String) {
        this.itemList.add(value)
        Log.d(ContentValues.TAG, "event item added")
    }
    fun addInterest(){
        interest= interest?.plus(1)
    }
    fun getLayoutType(): Int{
        return layoutType ?: 0
    }




}