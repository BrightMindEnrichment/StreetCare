package org.brightmindenrichment.street_care.ui.community.model

import java.util.Date

data class CommunityPostReply(
    var description: String? ,
    var user: User,
    var isVerified: Boolean,
    var dateCreated: Date = Date(),
    var profileImageUrl: String? = null)
