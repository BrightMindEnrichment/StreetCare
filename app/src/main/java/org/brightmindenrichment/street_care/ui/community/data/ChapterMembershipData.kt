package org.brightmindenrichment.street_care.ui.community.data

data class ChapterMembershipData(
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var phone: String = "",
    var address1: String = "",
    var address2: String = "",
    var city: String = "",
    var state: String = "",
    var zip: String = "",
    var availability: String = "",
    var consent: Boolean = false,
    var hours: String = "",
    var reason: String = "",
    var signature: String = ""
)
