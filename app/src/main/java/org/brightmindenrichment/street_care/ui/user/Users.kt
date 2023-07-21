package org.brightmindenrichment.street_care.ui.user

import java.util.*

class Users {
    var email: String? = null
    var organization: String? = null
    var username: String? = null
    var uid:String?=null
    var role: String? = null
    var dateCreated: Date = Date()
    var deviceType : String = "Android"

    constructor(username: String?, uid: String?,email: String?) {
        this.username = username
        this.uid = uid
        this.email = email
    }

    constructor()
}

