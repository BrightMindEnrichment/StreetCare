package org.brightmindenrichment.street_care.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

fun isAppLanguageSpanish(): Boolean {
    val currentLanguage = Locale.getDefault().language
    return currentLanguage.startsWith("es")
}

data class BannerData(val header: String = "", val subHeader: String = "", val body: String = "")

class HomeViewModel : ViewModel() {

    private val _bannerData = MutableLiveData<BannerData?>()
    val bannerData: MutableLiveData<BannerData?> = _bannerData
    fun fetchBannerData() {
        FirebaseFirestore.getInstance().collection("page_content")
            .document(if (isAppLanguageSpanish()) "banner_data_es" else "banner_data").get()
            .addOnSuccessListener { documentSnapshot ->
                val bannerBody = documentSnapshot.data?.get("body") as String?
                if (documentSnapshot.data != null && !bannerBody.isNullOrEmpty()) {
                    with(documentSnapshot.data!!) {
                        _bannerData.value = BannerData(
                            header = this["header"] as String? ?: "",
                            subHeader = this["sub_header"] as String? ?: "",
                            body = bannerBody
                        )
                    }
                }
            }.addOnFailureListener { exception ->
                _bannerData.value = null
                Log.w("BME", "Banner data failed to fetch data from Database $exception")
            }

    }
}