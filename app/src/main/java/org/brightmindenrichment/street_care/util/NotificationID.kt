package org.brightmindenrichment.street_care.util

import java.util.concurrent.atomic.AtomicInteger

class NotificationID {
    companion object {
        private val id = AtomicInteger(0)
        fun getNotificationID(): Int {
            return id.incrementAndGet()
        }
    }
}