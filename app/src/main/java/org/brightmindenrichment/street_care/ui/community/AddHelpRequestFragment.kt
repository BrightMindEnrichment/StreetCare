package org.brightmindenrichment.street_care.ui.community

import android.app.AlertDialog
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.ui.community.data.HelpRequestStatus
import org.brightmindenrichment.street_care.ui.user.ChapterMembershipFormOneAcitivity
import org.brightmindenrichment.street_care.util.Extensions
import org.brightmindenrichment.street_care.util.Extensions.Companion.requiredSkills
import org.brightmindenrichment.street_care.BuildConfig
import java.time.LocalDateTime
import java.util.*

class AddHelpRequestFragment : Fragment() {
    private lateinit var edtTitle: EditText
    private lateinit var edtDesc: EditText
    private lateinit var edtStreet: EditText
    private lateinit var edtState: EditText
    private lateinit var edtCity: EditText
    private lateinit var edtZipcode: EditText
    private lateinit var edtIdentification: EditText
    private lateinit var checkedItems: BooleanArray

    private val selectedItems = mutableListOf<String>()

    // auto-populate address from Street field
    private lateinit var placesClient: PlacesClient
    companion object {
        private const val AUTOCOMPLETE_REQUEST_CODE = 1
    }

    //private var isPastEvents = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Places
        Places.initialize(requireContext(), BuildConfig.API_KEY_PLACES)
        placesClient = Places.createClient(requireContext())

        arguments?.let {
            //isPastEvents = it.getBoolean("isPastEvents")
            checkedItems = it.getBooleanArray("skillsBooleanArray") ?: BooleanArray(requiredSkills.size)
        }

        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //activity!!.onBackPressedDispatcher.onBackPressed()

                val pageTitle = context?.getString(R.string.help_request)
                findNavController().popBackStack()
                findNavController().navigate(R.id.communityHelpRequestFragment, Bundle().apply {
                    //putBoolean("isPastEvents", isPastEvents)
                    putString("pageTitle", pageTitle)
                })


            }
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_help_request, container, false)
     }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                // the action of back icon
                //activity!!.onBackPressedDispatcher.onBackPressed()
                val pageTitle = context?.getString(R.string.help_request)
                findNavController().popBackStack()
                findNavController().navigate(R.id.communityHelpRequestFragment, Bundle().apply {
                    //putBoolean("isPastEvents", isPastEvents)
                    Log.d("debug", "pageTitle: $pageTitle")
                    putString("pageTitle", pageTitle)
                })
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val current = LocalDateTime.now()
        val currentDateInMillis = System.currentTimeMillis()

        edtTitle = view.findViewById<EditText>(R.id.edtTitle)
        edtDesc = view.findViewById<EditText>(R.id.edtDesc)
        edtStreet = view.findViewById<EditText>(R.id.edtStreet)

        // Function to launch Places autocomplete
        fun launchPlacesAutocomplete() {
            val fields = listOf(
                Place.Field.ADDRESS,
                Place.Field.ADDRESS_COMPONENTS,
                Place.Field.LAT_LNG,
                Place.Field.VIEWPORT
            )

            val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .build(requireContext())
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }

        edtStreet.setOnLongClickListener {
            // Make the field selectable
            edtStreet.setSelectAllOnFocus(true)
            // Enable text selection mode
            edtStreet.selectAll()
            true
        }

        edtStreet.setOnClickListener {
            if (edtStreet.selectionStart != edtStreet.selectionEnd) {
                // Text is selected, don't launch autocomplete
                return@setOnClickListener
            }
            launchPlacesAutocomplete()
        }

        edtStreet.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus && edtStreet.selectionStart == edtStreet.selectionEnd) {
                launchPlacesAutocomplete()
            }
        }

        edtState = view.findViewById<EditText>(R.id.edtState)
        edtCity = view.findViewById<EditText>(R.id.edtCity)
        edtZipcode = view.findViewById<EditText>(R.id.edtZipcode)
        edtIdentification = view.findViewById(R.id.edtIdentification)

        val btnRequiredSkills = view.findViewById<Button>(R.id.btnRequiredSkills)
        val tvRequiredSkills = view.findViewById<TextView>(R.id.tvRequiredSkills)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)
        val btnDiscard = view.findViewById<Button>(R.id.buttonDiscard)

//        val initialDateTimeStamp = Timestamp(Date(myCalendar.timeInMillis))
//        Log.d("date", "initialDateTimeStamp: $initialDateTimeStamp")

        //val checkedItems = BooleanArray(requiredSkills.size)

        // handle the Open Alert Dialog button
        btnRequiredSkills.setOnClickListener {
            val alertDialog = createRequiredSkillsDialog(
                tvRequiredSkills,
                checkedItems,
                requiredSkills,
            )
            alertDialog.show()
        }

        btnSubmit.setOnClickListener {
            if (Firebase.auth.currentUser == null) {
                context?.let { context ->
                    Extensions.showDialog(
                        context,
                        context.getString(R.string.alert),
                        context.getString(R.string.please_login_before_event),
                        context.getString(R.string.ok),
                        context.getString(R.string.cancel)
                    )
                }
            } else {
                val title = edtTitle.text.toString()
                val desc = edtDesc.text.toString()
                val street = edtStreet.text.toString()
                val state = edtState.text.toString()
                val city = edtCity.text.toString()
                val zipcode = edtZipcode.text.toString()
                val identification = edtIdentification.text.toString()
                val currentDateTimestamp = Timestamp(Date(currentDateInMillis))

                if (TextUtils.isEmpty(title)) {
                    edtTitle.error = it.context.getString(R.string.required)
                }
                else if (TextUtils.isEmpty(street)) {
                    edtStreet.error = it.context.getString(R.string.required)
                }
                else if (TextUtils.isEmpty(state)) {
                    edtState.error = it.context.getString(R.string.required)
                }
                else if (TextUtils.isEmpty(city)) {
                    edtCity.error = it.context.getString(R.string.required)
                }
                else if (TextUtils.isEmpty(zipcode)) {
                    edtZipcode.error = it.context.getString(R.string.required)
                }
                else if (TextUtils.isEmpty(identification)) {
                    edtIdentification.error = it.context.getString(R.string.required)
                }
                else if (selectedItems.isEmpty()) {
                    btnRequiredSkills.error = it.context.getString(R.string.required)
                }
                else {
                    addEvent(
                        title = title,
                        description = desc,
                        street = street,
                        state = state,
                        city = city,
                        zipcode = zipcode,
                        identification = identification,
                        currentDateTimestamp = currentDateTimestamp
                    )
                }
            }
        }
        btnDiscard.setOnClickListener{
            clearAllFields()
            requireActivity().onBackPressedDispatcher.onBackPressed()
            //findNavController().popBackStack()
            //findNavController().navigate(R.id.nav_community)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        // Extract just the street address
                        val streetAddress = place.address?.split(',')?.firstOrNull()?.trim() ?: ""
                        edtStreet.setText(streetAddress)

                        // Parse components for city, state, zip
                        place.addressComponents?.asList()?.forEach { component ->
                            when {
                                component.types.contains("locality") -> {
                                    edtCity.setText(component.name)
                                }
                                component.types.contains("administrative_area_level_1") -> {
                                    edtState.setText(component.name)
                                }
                                component.types.contains("postal_code") -> {
                                    edtZipcode.setText(component.name)
                                }
                            }
                        }
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Log.e("AddHelpRequestFragment", "Error: ${status.statusMessage}")
                    }
                }
                Activity.RESULT_CANCELED -> {
                    // User canceled the operation - returns to the previous screen without making any changes to the address fields.
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun createRequiredSkillsDialog(
        tvRequiredSkills: TextView,
        checkedItems:     BooleanArray,
        requiredSkills:   Array<String>,
    ): AlertDialog {
        // initialise the alert dialog builder
        val builder = AlertDialog.Builder(this.context).apply {
            // set the title for the alert dialog
            setTitle(context.getString(R.string.select_required_skills))

            // set the icon for the alert dialog
            setIcon(R.drawable.streetcare_logo)

            // now this is the function which sets the alert dialog for multiple item selection ready
            setMultiChoiceItems(requiredSkills, checkedItems) { dialog, which, isChecked ->
                checkedItems[which] = isChecked
                val currentItem = requiredSkills[which]
            }

            // alert dialog shouldn't be cancellable
            setCancelable(false)

            // handle the positive button of the dialog
            setPositiveButton(context.getString(R.string.done)) { dialog, which ->
                tvRequiredSkills.text = ""
                selectedItems.clear()
                for (i in checkedItems.indices) {
                    if (checkedItems[i]) {
                        tvRequiredSkills.text = "${tvRequiredSkills.text}${requiredSkills[i]}, "
                        selectedItems.add(requiredSkills[i])
                    }
                }
                if(tvRequiredSkills.text == "") tvRequiredSkills.visibility = View.GONE
                else {
                    tvRequiredSkills.visibility = View.VISIBLE
                    tvRequiredSkills.text = tvRequiredSkills.text.substring(0, tvRequiredSkills.text.length - 2)
                }
            }

            // handle the negative button of the alert dialog
            setNegativeButton("CANCEL") { dialog, which -> }

            // handle the neutral button of the dialog to clear the selected items boolean checkedItem
            setNeutralButton(context.getString(R.string.clear_all)) { dialog: DialogInterface?, which: Int ->
                Arrays.fill(checkedItems, false)
                tvRequiredSkills.text = null
                tvRequiredSkills.visibility = View.GONE
            }

        }

        // create the alert dialog with the alert dialog builder instance
        return builder.create()
    }

    private fun clearAllFields() {
        edtTitle.text.clear()
        edtDesc.text.clear()
        edtStreet.text.clear()
        edtState.text.clear()
        edtCity.text.clear()
        edtZipcode.text.clear()
        edtIdentification.text.clear()
    }

    private fun addEvent(
        title: String,
        description: String,
        street: String,
        state: String,
        city: String,
        zipcode: String,
        identification: String,
        currentDateTimestamp: Timestamp,
    ) {
        // make sure somebody is logged in
        val user = Firebase.auth.currentUser ?: return
        // create a map of help request data so we can add to firebase
        val helpRequestData = hashMapOf(
            "createdAt" to currentDateTimestamp,
            "description" to description,
            "location" to mapOf(
                "street" to street,
                "state" to state,
                "city" to city,
                "zipcode" to zipcode
            ), // map: {city: String, state: String, street: String, zipcode: String
            "skills" to selectedItems, // array
            "title" to title,
            "uid" to user.uid,
            "identification" to identification,
            "outreachEvent" to emptyList<String>(),
            "status" to HelpRequestStatus.NeedHelp.status
        )
        // save to firebase
        val db = Firebase.firestore
        db.collection("helpRequests")
            .add(helpRequestData)
            .addOnSuccessListener { documentReference ->
                Log.d("BME", "Saved with id ${documentReference.id}")
//                Extensions.showDialog(
//                    requireContext(),
//                    requireContext().getString(R.string.alert),
//                    requireContext().getString(R.string.event_registered_for_approval),
//                    requireContext().getString(R.string.ok),
//                    requireContext().getString(R.string.cancel)
//                )
                // Assuming you have the necessary resources defined in your strings.xml

                val message = getString(R.string.thank_you_post)
                val approvalMessage = getString(R.string.approval_pending)
                val learnMoreText = getString(R.string.streamline_experience)
                val learnMoreLink = getString(R.string.learn_more)

// Create a custom layout for the dialog
                val dialogView = LayoutInflater.from(context).inflate(R.layout.chapter_membership_signup, null) // Assuming you have a custom_dialog_layout.xml
                dialogView.findViewById<TextView>(R.id.textViewMessage).text = message
                dialogView.findViewById<TextView>(R.id.approvalTextView).text = approvalMessage
                dialogView.findViewById<TextView>(R.id.learnMoreTextView).text = learnMoreText
                dialogView.findViewById<TextView>(R.id.learnMoreLinkTextView).text = learnMoreLink
                dialogView.findViewById<TextView>(R.id.learnMoreLinkTextView).setOnClickListener {
                    val intent = Intent(requireContext(), ChapterMembershipFormOneAcitivity::class.java)
                    context?.startActivity(intent)
                }


                val builder = AlertDialog.Builder(context)
                builder.setView(dialogView)
                builder.setCancelable(true) // Set to false if you don't want the dialog to be dismissed by tapping outside

                val dialog = builder.create()
                dialog.show()
                dialogView.findViewById<ImageView>(R.id.closeIcon).setOnClickListener {
                    dialog.dismiss() // Dismiss the dialog
                }
                clearAllFields()
                Toast.makeText(context, context?.getString(R.string.successfully_registered), Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
                findNavController().navigate(R.id.nav_community)
            }
            .addOnFailureListener { exception ->
                Log.w("BMR", "Error in addEvent ${exception.toString()}")
                Toast.makeText(context, context?.getString(R.string.failed), Toast.LENGTH_LONG).show()
            }
    }
}