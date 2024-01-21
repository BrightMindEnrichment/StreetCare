package org.brightmindenrichment.street_care.notification

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import org.brightmindenrichment.data.local.EventsDatabase
import javax.inject.Inject

class NotificationWorkerFactory @Inject constructor(
    private val eventsDatabase: EventsDatabase
):  WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = NotificationWorker(
        eventsDatabase,
        appContext,
        workerParameters,
    )
}