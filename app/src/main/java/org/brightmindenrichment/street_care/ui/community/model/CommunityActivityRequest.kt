package org.brightmindenrichment.street_care.ui.community.model

class CommunityActivityRequest private constructor(
    val time: String,
    val user: User?,
    val location: String?,
    val title: String,
    val description: String
) {

    class Builder {
        private var time: String = ""
        private var user: User? = null
        private var location: String = ""
        private var description: String = ""
        private var title:String = ""

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

        fun setDescription(description: String): Builder {
            this.description = description
            return this
        }

        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }
        override fun toString(): String {
            val stringBuilder = StringBuilder()
            stringBuilder.append("Description: $description")
            if (!location.isNullOrEmpty()) {
                stringBuilder.append(" at $location")
            }
            return stringBuilder.toString()
        }

        fun build(): CommunityActivityRequest {
            require(time.isNotEmpty()) { "Time must be set" }
//            require(user != null) { "User must be set" }
            require(description.isNotEmpty()) { "Description must be set" }
            require(title.isNotEmpty()){ "Title must be set"}
            return CommunityActivityRequest(time, user, location, description, title)
        }

    }
}