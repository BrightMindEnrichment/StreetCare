package org.brightmindenrichment.street_care.ui.community.model

class CommunityActivityHelp
private constructor(
    val time: String,
    val user: User?,
    val contact: String?,
    val location: String?,
    val description: String,
    val title: String
) {
    //for parsing object from firebase
    constructor() : this(
        "", null, null, "", "", ""
    )


    class Builder {
        private var time: String = ""
        private var user: User? = null
        private var contact: String? = null
        private var location: String = ""
        private var description: String = ""
        private var title: String = ""

        fun setTime(time: String): Builder {
            this.time = time
            return this
        }

        fun setUser(user: User): Builder {
            this.user = user
            return this
        }

        fun setLocation(location: String): Builder {
            this.location = location
            return this
        }

        fun setContact(contact: String): Builder {
            this.contact = contact
            return this
        }

        fun setDescription(description: String): Builder {
            this.description = description
            return this
        }

        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        fun build(): CommunityActivityHelp {
            require(time.isNotEmpty()) { "Time must be set" }
            require(description.isNotEmpty()) { "Description must be set" }
            require(title.isNotEmpty()) { "Title must be set" }

            return CommunityActivityHelp(time, user, contact, location, description, title)
        }
    }
}