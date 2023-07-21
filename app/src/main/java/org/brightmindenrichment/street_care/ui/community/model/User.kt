package org.brightmindenrichment.street_care.ui.community.model

import java.util.Date

data class User(var email: String?,
                var organization: String?,
                var username: String?,
                var uid:String?,
                var role: String?,
                var dateCreated: Date = Date(),
                var deviceType : String = "Android",
                var profileImageUrl: String? = null){
    fun randomName(){
        username = RandomName.randomNameGen()
    }
}
