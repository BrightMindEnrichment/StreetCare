package org.brightmindenrichment.street_care.ui.community.model

class RandomName {
    companion object {
        private val firstNames = listOf(
            "Liam",
            "Noah",
            "Oliver",
            "Elijah",
            "William",
            "James",
            "Olivia",
            "Emma",
            "Charlotte",
            "Amelia",
            "Ava",
            "Sophia"
        )
        private val lastNames = listOf("Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia")
        fun randomNameGen(): String {
            return firstNames.random() + lastNames.random()
        }
    }
}