package org.brightmindenrichment.street_care.ui.visit.details

import androidx.lifecycle.ViewModel
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog
import org.brightmindenrichment.street_care.ui.visit.repository.VisitLogRepository
import org.brightmindenrichment.street_care.ui.visit.repository.VisitLogRepositoryImp

class VisitLogDetailsViewModel : ViewModel() {
    private val repository: VisitLogRepository = VisitLogRepositoryImp()

    init {

    }
}