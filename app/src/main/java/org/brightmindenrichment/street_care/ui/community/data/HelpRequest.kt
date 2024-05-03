package org.brightmindenrichment.street_care.ui.community.data

import android.content.ContentValues
import android.util.Log
import org.brightmindenrichment.street_care.util.Extensions
import org.brightmindenrichment.street_care.util.Extensions.Companion.requiredSkills

class HelpRequest {
    var id: String? = null
    var description: String? = null
    var identification: String? = null
    var status: String? = null
    var street: String? = null
    var city: String? = null
    var state: String? = null
    var zipcode: String? = null
    var location: String? = null
    var title: String? = null
    var uid: String? = null
    var createdAt: String? = null
    var skills: List<String>? = null
    var skillsBooleanArray = BooleanArray(requiredSkills.size){ false }

}