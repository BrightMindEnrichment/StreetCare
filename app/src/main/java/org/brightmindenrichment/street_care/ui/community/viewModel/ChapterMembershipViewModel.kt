package org.brightmindenrichment.street_care.ui.community.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.brightmindenrichment.street_care.ui.community.data.ChapterMembershipData

class ChapterMembershipViewModel : ViewModel() {

    val formData = MutableLiveData(ChapterMembershipData())
    private val _formSubmissionStatus = MutableLiveData<String>()
    val formSubmissionStatus: LiveData<String> get() = _formSubmissionStatus

    fun updateField(fieldName: String, value: Any) {
        val currentData = formData.value ?: ChapterMembershipData()
        when (fieldName) {
            "firstName" -> currentData.firstName = value as String
            "lastName" -> currentData.lastName = value as String
            "email" -> currentData.email = value as String
            "phone" -> currentData.phone = value as String
            "address1" -> currentData.address1 = value as String
            "address2" -> currentData.address2 = value as String
            "city" -> currentData.city = value as String
            "state" -> currentData.state = value as String
            "zip" -> currentData.zip = value as String
            "availability" -> currentData.availability = value as String
            "consent" -> currentData.consent = value as Boolean
            "hours" -> currentData.hours = value as String
            "reason" -> currentData.reason = value as String
            "signature" -> currentData.signature = value as String
        }
        formData.value = currentData
    }

    fun submitForm() {
        val data = formData.value
        if (data != null && validateForm(data)) {
            _formSubmissionStatus.value = "Form submitted successfully!"
        } else {
            _formSubmissionStatus.value = "Please fill in all required fields."
        }
    }

    private fun validateForm(data: ChapterMembershipData): Boolean {
        return data.firstName.isNotEmpty() &&
                data.lastName.isNotEmpty() &&
                data.email.isNotEmpty() &&
                data.signature.isNotEmpty()
    }
}