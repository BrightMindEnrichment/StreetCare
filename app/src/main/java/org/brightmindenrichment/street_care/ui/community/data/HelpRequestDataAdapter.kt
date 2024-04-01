package org.brightmindenrichment.street_care.ui.community.data

import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.ui.community.model.CommunityPageName
import org.brightmindenrichment.street_care.util.Extensions
import org.brightmindenrichment.street_care.util.Extensions.Companion.requiredSkills
import java.util.Arrays

/**
// example addEvent
//eventDataAdapter.addEvent("Food for Androids", "Feed all the android of the world.", Date()) {
//    Log.d("BME", "Event added")
//}

//example setLiked
//eventDataAdapter.setLikedEvent("2r9Z5TKnQYFC96iMAB1i", true) {
//    Log.d("BME", "done")
//}

// example refresh
//eventDataAdapter.refresh {
//    for (event in this.eventDataAdapter.events) {
//        Log.d("BME", "${event.title} ${event.date} ${event.liked}")
//    }
//}
 * */


class HelpRequestDataAdapter(
    private val scope: CoroutineScope,
    private val context: Context,
    private val navController: NavController,
) {
    //var events: MutableList<Event> = mutableListOf()
    private var helpRequestDataList: MutableList<HelpRequestData> = mutableListOf()
    val storage = Firebase.storage
    val size: Int
        get() {
            return helpRequestDataList.size
        }

    fun getHelpRequestAtPosition(position: Int): HelpRequestData? {

        if ((position >= 0) && (position < helpRequestDataList.size)) {
            return helpRequestDataList[position]
        }

        return null
    }

    private fun checkQuery(helpRequest: HelpRequest, inputText: String): Boolean {
        val title = helpRequest.title?.lowercase()?.trim() ?: "unknown"
        val description = helpRequest.description?.lowercase()?.trim() ?: "unknown"
        val location = helpRequest.location?.lowercase()?.trim() ?: "unknown"
        val identification = helpRequest.identification?.lowercase()?.trim() ?: "unknown"
        val skills = helpRequest.skills?.map { it.lowercase() } ?: emptyList()
        var checkSkills = false
        for(skill in skills) {
            if(skill.contains(inputText.lowercase().trim())) {
                checkSkills = true
                break
            }
        }
        return  inputText.isEmpty() ||
                title.contains(inputText.lowercase().trim()) ||
                description.contains(inputText.lowercase().trim()) ||
                location.contains(inputText.lowercase().trim()) ||
                checkSkills ||
                identification.contains(inputText.lowercase().trim())
    }


    fun refresh(
        inputText: String,
        query: Query,
        showProgressBar: () -> Unit,
        onNoResults: () -> Unit,
        onComplete: () -> Unit,
    ) {
        showProgressBar()
        // make sure somebody is logged in
        val user = Firebase.auth.currentUser ?: return
        var prevMonth: String? = null
        var prevDay: String? = null
        //val db = Firebase.firestore
        //val query = db.collection("events").orderBy("date", Query.Direction.DESCENDING)
        query.get()
            .addOnSuccessListener { result ->
                this.helpRequestDataList.clear()
                Log.d("loadProfileImg", "before, communityDataList size: ${helpRequestDataList.size}")
                Log.d("query", "successfully refresh: ${result.size()}")

                scope.launch {
                    for (document in result) {
                        yield()
                        var helpRequest = HelpRequest()
                        helpRequest.id = document.id
                        helpRequest.title = document.get("title")?.toString() ?: "Unknown"
                        helpRequest.description = document.get("description")?.toString() ?: "Unknown"
                        val location = document.get("location") as? Map<*, *>
                        if(location != null) {
                            helpRequest.location = "${location["street"]}, ${location["city"]}, ${location["state"]} ${location["zipcode"]}"
                            helpRequest.street = "${location["street"]}"
                            helpRequest.city = "${location["city"]}"
                            helpRequest.state = "${location["state"]}"
                            helpRequest.zipcode = "${location["zipcode"]}"
                        }
                        else helpRequest.location = "Unknown"

                        if(!checkQuery(helpRequest, inputText)) continue

                        helpRequest.identification = document.get("identification").toString()
                        helpRequest.status = document.get("status").toString()
                        helpRequest.uid = document.get("uid").toString()
                        helpRequest.createdAt = document.get("createdAt").toString()
                        helpRequest.skills = document.get("skills") as ArrayList<String> // List<String>
                        helpRequest.skills?.forEach { skill ->
                            val index = requiredSkills.indexOf(skill)
                            if(index != -1) {
                                helpRequest.skillsBooleanArray[index] = true
                            }

                        }

                        if(helpRequest.status == HelpRequestStatus.NeedHelp.status && user.uid == helpRequest.uid) {
                            helpRequest.status = HelpRequestStatus.HelpOnTheWay.status
                        }

                        val helpRequestData = HelpRequestData(helpRequest)
                        this@HelpRequestDataAdapter.helpRequestDataList.add(helpRequestData)

                    }

                    if(helpRequestDataList.isEmpty()) onNoResults()
                    else onComplete()
                }

            }.addOnFailureListener { exception ->
                Log.d("query", "refresh failed: $exception")
                onComplete()
            }
    }

    fun setBtnAction(helpRequest: HelpRequest, onComplete: (HelpRequest) -> Unit) {

        // make sure somebody is logged in
        val user = Firebase.auth.currentUser ?: return

        val db = Firebase.firestore
        val helpRequestStatus = helpRequest.status!!
        val helpRequestsDocRef = db.collection("helpRequestsAndroid").document(helpRequest.id!!)
        when(helpRequestStatus) {
            HelpRequestStatus.NeedHelp.status -> {
                // show alert dialog, either
                // create a new outreach event or
                // RSVP existing outreach related to the help request
                val helpRequestDialog = createHelpRequestDialog(helpRequest)
                helpRequestDialog.show()
                helpRequestDialog.apply {
                    getButton(Dialog.BUTTON_POSITIVE).apply {
                        setTextSize(TypedValue.COMPLEX_UNIT_SP,15.0f)
                        backgroundTintList = ColorStateList.valueOf(
                            context.resources.getColor(R.color.dark_green, null)
                        )
                        setTextColor(context.resources.getColor(R.color.accent_yellow, null))
                    }
                    getButton(Dialog.BUTTON_NEGATIVE).apply {
                        backgroundTintList = ColorStateList.valueOf(
                            context.resources.getColor(R.color.dark_green, null)
                        )
                        setTextColor(context.resources.getColor(R.color.accent_yellow, null))
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.0f)
                    }
                    getButton(Dialog.BUTTON_NEUTRAL).apply {
                        backgroundTintList = ColorStateList.valueOf(
                            context.resources.getColor(R.color.dark_green, null)
                        )
                        setTextColor(context.resources.getColor(R.color.accent_yellow, null))
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.0f)
                    }
                }
            }
            HelpRequestStatus.HelpOnTheWay.status -> {
                // only for the user who created this help request:
                // set the value of status field in firebase to "Help Received"
                if(user.uid == helpRequest.uid) {
                    val updateHelpRequestStatus = helpRequestsDocRef.update("status", HelpRequestStatus.HelpReceived.status)
                        .addOnSuccessListener { Log.d("syncWebApp", "HelpRequestStatus successfully updated! status: ${helpRequest.status}") }
                        .addOnFailureListener { e -> Log.w("syncWebApp", "Error update HelpRequestStatus", e) }

                    val tasks = Tasks.whenAll(listOf(updateHelpRequestStatus))

                    tasks.addOnCompleteListener {
                        onComplete(helpRequest)
                    }

                }
            }
            HelpRequestStatus.HelpReceived.status -> {
                // only for the user who created this help request:
                // reopen the help request
                if(user.uid == helpRequest.uid) {
                    val updateHelpRequestStatus = helpRequestsDocRef.update("status", HelpRequestStatus.NeedHelp.status)
                        .addOnSuccessListener { Log.d("syncWebApp", "HelpRequestStatus successfully updated! status: ${helpRequest.status}") }
                        .addOnFailureListener { e -> Log.w("syncWebApp", "Error update HelpRequestStatus", e) }

                    val tasks = Tasks.whenAll(listOf(updateHelpRequestStatus))

                    tasks.addOnCompleteListener {
                        onComplete(helpRequest)
                    }
                }
            }
        }

    }

    private fun createHelpRequestDialog(
        helpRequest: HelpRequest
    ): AlertDialog {
        // initialise the alert dialog builder
        val builder = AlertDialog.Builder(this.context).apply {
            // set the title for the alert dialog
            setTitle("Make sure you are not going alone")

            setMessage("Group presence offers security and effectiveness in engaging with unfamiliar situations and individuals, benefiting both volunteers and the homeless. How outreach on Street Care works? We post the outreach for you and other volunteers can sign up to go with you.")

            // set the icon for the alert dialog
            //setIcon(R.drawable.streetcare_logo)

            // now this is the function which sets the alert dialog for multiple item selection ready
            /*
            setMultiChoiceItems(requiredSkills, checkedItems) { dialog, which, isChecked ->
                checkedItems[which] = isChecked
                val currentItem = requiredSkills[which]
            }
             */

            // alert dialog shouldn't be cancellable
            setCancelable(true)

            // handle the positive button of the dialog
            setPositiveButton("RSVP Existing Outreach") { dialog, which ->
                navController.navigate(R.id.communityEventFragment, Bundle().apply {
                    putString("pageTitle", "Upcoming Events")
                    putString("helpRequestId", helpRequest.id)
                    putSerializable("communityPageName", CommunityPageName.HELP_REQUESTS)
                })
            }

            // handle the negative button of the alert dialog
            setNegativeButton("CANCEL") { dialog, which -> }

            // handle the neutral button of the dialog to clear the selected items boolean checkedItem
            setNeutralButton("Create an Outreach") { dialog: DialogInterface?, which: Int ->
                navController.popBackStack()
                navController.navigate(R.id.nav_add_event, Bundle().apply {
                    putSerializable("communityPageName", CommunityPageName.HELP_REQUESTS)
                    putString("helpRequestId", helpRequest.id)
                    putString("title", helpRequest.title?:"unknown")
                    putString("street", helpRequest.street?:"street")
                    putString("city", helpRequest.city?:"city")
                    putString("state", helpRequest.state?:"state")
                    putString("zipcode", helpRequest.zipcode?:"zipcode")
                    putBooleanArray("skillsBooleanArray", helpRequest.skillsBooleanArray)
                    putString("description", helpRequest.description?:"unknown")
                })
            }

        }

        // create the alert dialog with the alert dialog builder instance
        return builder.create()
    }


} // end class

sealed class HelpRequestStatus(val status: String) {
    object NeedHelp: HelpRequestStatus("Need Help")
    object HelpOnTheWay: HelpRequestStatus("Help is on the way")
    object HelpReceived: HelpRequestStatus("Help Received")
}