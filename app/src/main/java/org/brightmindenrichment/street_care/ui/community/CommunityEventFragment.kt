package org.brightmindenrichment.street_care.ui.community

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.notification.ChangedType
import org.brightmindenrichment.street_care.ui.community.adapter.CommunityRecyclerAdapter
import org.brightmindenrichment.street_care.ui.community.data.Event
import org.brightmindenrichment.street_care.ui.community.data.EventDataAdapter
import org.brightmindenrichment.street_care.util.DebouncingQueryTextListener
import org.brightmindenrichment.street_care.util.Extensions.Companion.getDayInMilliSec
import org.brightmindenrichment.street_care.util.Extensions.Companion.refreshNumOfInterest
import org.brightmindenrichment.street_care.util.Extensions.Companion.replaceButtonInterest
import org.brightmindenrichment.street_care.util.Extensions.Companion.replaceRSVPButton
import org.brightmindenrichment.street_care.util.Extensions.Companion.setButtonInterest
import org.brightmindenrichment.street_care.util.Extensions.Companion.setRSVPButton
import org.brightmindenrichment.street_care.util.Extensions.Companion.setVerifiedAndRegistered
import org.brightmindenrichment.street_care.util.Extensions.Companion.toPx
import org.brightmindenrichment.street_care.util.Queries.getPastEventsQuery
import org.brightmindenrichment.street_care.util.Queries.getQueryToFilterEventsBeforeTargetDate
import org.brightmindenrichment.street_care.util.Queries.getQueryToFilterEventsAfterTargetDate
import org.brightmindenrichment.street_care.util.Queries.getUpcomingEventsQuery
import java.util.Date


class CommunityEventFragment : Fragment(), AdapterView.OnItemSelectedListener {

    lateinit var buttonAdd: ImageButton
    private var scope = lifecycleScope
    private val eventDataAdapter = EventDataAdapter(scope)

    private var userInputText = ""
    private var selectedItemPos = -1
    private var isPastEvents = true
    private var defaultQuery = getPastEventsQuery()

    //private lateinit var fragmentCommunityEventView: View
    private lateinit var bottomSheetView: LinearLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var searchView: SearchView
    private lateinit var menuItems: List<String>


    override fun onDestroy() {
        super.onDestroy()
        Log.d("syncWebApp", "Community Event Fragment onDestroy...")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("syncWebApp", "Community Event Fragment onCreate...")
        Log.d("syncWebApp", "before, isPastEvents: $isPastEvents")
        arguments?.let {
            isPastEvents = it.getBoolean("isPastEvents")
        }
        Log.d("syncWebApp", "after, isPastEvents: $isPastEvents")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Log.d(ContentValues.TAG, "Community onCreateView")
        return inflater.inflate(R.layout.fragment_community_event, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //fragmentCommunityEventView = view
        if(!isPastEvents) defaultQuery = getUpcomingEventsQuery()

//        val pageTitle = if(isPastEvents) "Past Events" else "Upcoming Events"
//        findNavController().currentDestination?.label = pageTitle

        val menuHost: MenuHost = requireActivity()
        Log.d("notification", "associated activity: $menuHost")
        searchView = view.findViewById(R.id.search_view)
        val spinner: Spinner = view.findViewById(R.id.events_filter)
        spinner.onItemSelectedListener = this
        spinner.dropDownHorizontalOffset = (-130).toPx()
        spinner.dropDownVerticalOffset = 40.toPx()
        spinner.dropDownWidth = 180.toPx()
        if(isPastEvents) {
            menuItems = listOf(
                "Select...",
                "Last 7 days",
                "Last 30 days",
                "Last 60 days",
                "Last 90 days",
                "Other past events",
                "Reset"
            )
        }
        else {
            menuItems = listOf(
                "Select...",
                "Next 7 days",
                "Next 30 days",
                "Next 60 days",
                "Next 90 days",
                "Other upcoming events",
                "Reset"
            )
        }

        val dataAdapter: ArrayAdapter<String> =
            object : ArrayAdapter<String>(this.requireContext(), android.R.layout.simple_spinner_item, menuItems) {
                override fun getDropDownView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup
                ): View {
                    //var v: View? = null
                    val v = super.getDropDownView(position, null, parent)
                    // If this is the selected item position
                    if (position == selectedItemPos && selectedItemPos != 0 && selectedItemPos != menuItems.size - 1) {
                        v.setBackgroundColor(resources.getColor(R.color.item_selected_black, null))
                    } else {
                        // for other views
                        v.setBackgroundColor(Color.WHITE)
                    }
                    return v
                }
            }
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = dataAdapter

        /*
        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter.createFromResource(
            this.requireContext()!!,
            R.array.events_filter,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner.
            spinner.adapter = adapter
        }

         */

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
                super.onPrepareMenu(menu)
                val itemEventsFilter = menu.add(Menu.NONE, 0, 0, "events filter").apply {
                    //setIcon(R.drawable.filter_layer)
                    actionView = spinner
                    setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                }

                val itemAddNew = menu.add(Menu.NONE, 1, 1, "add new").apply {
                    setIcon(R.drawable.ic_menu_add)
                    setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                }

                Log.d("filter", "itemEventsFilterId: " + itemEventsFilter.itemId)
                Log.d("filter", "itemAddNewId: " + itemAddNew.itemId)

            }
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                Log.d("menuItem.isVisible", "menuItem.isVisible: " + menuItem.itemId)
                Log.d("filter", "Selected Menu Item: ${menuItem.title}, id: ${menuItem.itemId}")

                when(menuItem.itemId) {
                    0 -> {

                    }
                    1-> {
                        findNavController().popBackStack()
                        findNavController().navigate(R.id.nav_add_event, Bundle().apply {
                            putBoolean("isPastEvents", isPastEvents)
                        })
                    }
                    else -> {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                        //requireActivity().onBackPressed()
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)


        /*val community_toolbar: Toolbar = view.findViewById<Toolbar>(R.id.community_toolbar)
        community_toolbar.inflateMenu(R.menu.community_toolbar_menu)


        val addButton: LinearLayout = community_toolbar.findViewById<LinearLayout>(R.id.action_button)
        addButton.setOnClickListener {
            findNavController().navigate(R.id.nav_add_event)
        }*/

        setUpSearchView(searchView)

        Log.d(ContentValues.TAG, "Community onViewCreated start")
        if (Firebase.auth.currentUser == null) {
            val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
            val textView = view.findViewById<LinearLayout>(R.id.root).findViewById<TextView>(R.id.text_view)
            progressBar?.visibility = View.GONE
            textView.visibility = View.VISIBLE
            textView.text = "Events are only available for logged in Users"
            //val layout = view.findViewById<LinearLayout>(R.id.root)
            //val textView = createTextView("Events are only available for logged in Users")
            //layout?.addView(textView)
        }
        else{
            bottomSheetView = view.findViewById<LinearLayout>(R.id.bottomLayout)
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            val backgroundOverlay: FrameLayout = view.findViewById<FrameLayout>(R.id.backgroundOverlay)
            val mask = view.findViewById<LinearLayout>(R.id.ll_mask)

            bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            mask.visibility = View.VISIBLE
                            backgroundOverlay.visibility = View.VISIBLE
                        }
                        else -> {
                            mask.visibility = View.GONE
                            backgroundOverlay.visibility = View.GONE
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    backgroundOverlay.visibility = View.VISIBLE
                    backgroundOverlay.alpha = slideOffset
                }

            })

            refreshEvents(
                eventDataAdapter,
                this@CommunityEventFragment.resources,
                defaultQuery,
                ""
            )

            searchEvents(
                eventDataAdapter,
                this@CommunityEventFragment.resources,
                defaultQuery
            )

        }

    }



    private fun createTextView(text: String): TextView {
        val textView = TextView(context)
        //setting height and width
        textView.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        textView.text = text
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        textView.setTextColor(Color.GRAY)
        textView.setPadding(20, 20, 20, 20)
        textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        textView.gravity = Gravity.CENTER_VERTICAL
        textView.isAllCaps=false
        return textView
    }

    private fun setUpSearchView(searchView: SearchView) {
        searchView.setIconifiedByDefault(false)
        searchView.isSubmitButtonEnabled = true
        searchView.imeOptions = EditorInfo.IME_ACTION_SEARCH
        searchView.queryHint = "search"

    }

    private fun searchEvents(
        eventDataAdapter: EventDataAdapter,
        resources: Resources,
        query: Query
    ) {
        searchView.setOnQueryTextListener(
            DebouncingQueryTextListener(lifecycle) { inputText ->
                inputText?.let {
                    userInputText = it
                    requestQuery(
                        it,
                        eventDataAdapter,
                        view,
                        bottomSheetView,
                        bottomSheetBehavior,
                        resources,
                        query
                    )
                }
            }
        )

        /*
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(inputText: String?): Boolean {
                requestQuery(
                    inputText,
                    eventDataAdapter,
                    view,
                    bottomSheetView,
                    bottomSheetBehavior,
                    resources
                )
                return false
            }

            override fun onQueryTextChange(inputText: String?): Boolean {
                Log.d("query", "textChanged: $inputText")
                requestQuery(
                    inputText,
                    eventDataAdapter,
                    view,
                    bottomSheetView,
                    bottomSheetBehavior,
                    resources
                )
                return false
            }
        })

         */

    }

    private fun requestQuery(
        inputText: String,
        eventDataAdapter: EventDataAdapter,
        view: View?,
        bottomSheetView: LinearLayout,
        bottomSheetBehavior: BottomSheetBehavior<LinearLayout>,
        resources: Resources,
        query: Query
    ) {
//            val query = if(it.isNotEmpty()) Firebase.firestore
//                .collection("events")
//                .where(Filter.or(
//                    Filter.equalTo("description", it),
//                    Filter.equalTo("location", it),
//                    Filter.equalTo("title", it),
//                ))
//                .orderBy("date", Query.Direction.DESCENDING)
//            else defaultQuery

        refreshEvents(
            eventDataAdapter = eventDataAdapter,
            resources = resources,
            query = query,
            inputText = inputText
        )
    }


    private fun refreshEvents(
        eventDataAdapter: EventDataAdapter,
        resources: Resources,
        query: Query,
        inputText: String
    ) {
        val progressBar = view?.findViewById<ProgressBar>(R.id.progressBar)
        val textView = view?.findViewById<LinearLayout>(R.id.root)?.findViewById<TextView>(R.id.text_view)
        eventDataAdapter.refresh(
            inputText = inputText,
            query = query,
            showProgressBar = {
                progressBar?.visibility = View.VISIBLE
            },
            onNoResults = {
                progressBar?.visibility = View.GONE
                textView?.visibility = View.VISIBLE
                textView?.text = "No results were found"
                /*
                val layout = view?.findViewById<LinearLayout>(R.id.root)
                val textView = createTextView("No results were found")
                val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerCommunity)
                recyclerView?.visibility = View.GONE
                layout?.addView(textView)

                 */
            }
        ) {
            textView?.visibility = View.GONE
            progressBar?.visibility = View.GONE
            val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerCommunity)!!
            val communityRecyclerAdapter = CommunityRecyclerAdapter(eventDataAdapter, isPastEvents)
            recyclerView.visibility = View.VISIBLE
            recyclerView.layoutManager = LinearLayoutManager(view?.context)
            recyclerView.adapter = communityRecyclerAdapter
            recyclerView.addItemDecoration(LinePaint())

            val stickyHeaderItemDecorator = StickyHeaderItemDecorator(communityRecyclerAdapter)
            recyclerView.addItemDecoration(stickyHeaderItemDecorator)

            val textViewTitle: TextView = bottomSheetView.findViewById<TextView>(R.id.textViewCommunityTitle)
            val textViewCommunityLocation: TextView =bottomSheetView.findViewById<TextView>(R.id.textViewCommunityLocation)
            val textViewCommunityTime: TextView =bottomSheetView.findViewById<TextView>(R.id.textViewCommunityTime)
            val textViewCommunityDesc: TextView =bottomSheetView.findViewById<TextView>(R.id.textViewCommunityDesc)
            val relativeLayoutImage: RelativeLayout = bottomSheetView.findViewById<RelativeLayout>(R.id.relativeLayoutImage)
            val buttonRSVP: AppCompatButton = bottomSheetView.findViewById<AppCompatButton>(R.id.btnRSVP)
            val textInterested:TextView = bottomSheetView.findViewById<TextView>(R.id.textInterested)
            val buttonInterested: AppCompatButton = bottomSheetView.findViewById<AppCompatButton>(R.id.buttonInterested)
            val buttonClose: AppCompatButton = bottomSheetView.findViewById<AppCompatButton>(R.id.buttonClose)
            val linearLayoutVerified: LinearLayout = bottomSheetView.findViewById<LinearLayout>(R.id.llVerifiedAndRegistered)
            val textHelpType:TextView = bottomSheetView.findViewById<TextView>(R.id.tvHelpType)
            val linearLayoutVerifiedAndIcon: LinearLayout = bottomSheetView.findViewById(R.id.llVerifiedAndIcon)
            val textViewRegistered: TextView = bottomSheetView.findViewById(R.id.tvRegistered)
            val textViewEventStatus: TextView = bottomSheetView.findViewById(R.id.tvEventStatus)

            (recyclerView?.adapter as CommunityRecyclerAdapter).setRefreshBottomSheet { event ->
                refreshBottomSheet(
                    event = event,
                    relativeLayoutImage = relativeLayoutImage,
                    textInterested = textInterested,
                    buttonRSVP = buttonRSVP,
                    buttonInterested = buttonInterested,
                    textViewEventStatus = textViewEventStatus,
                    linearLayoutVerified = linearLayoutVerified,
                    linearLayoutVerifiedAndIcon = linearLayoutVerifiedAndIcon,
                    textViewRegistered = textViewRegistered,
                )
            }

            (recyclerView.adapter as CommunityRecyclerAdapter).setClickListener(object :
                CommunityRecyclerAdapter.ClickListener {
                @SuppressLint("ResourceAsColor")
                override fun onClick(event: Event, position: Int) {
                    textViewTitle.text = event.title
                    textViewCommunityLocation.text = event.location
                    textViewCommunityTime.text = event.time
                    textViewCommunityDesc.text = event.description

                    val approved = event.approved!!

                    var isSignedUp = event.signedUp
                    //val numOfInterest = event.interest?.minus(event.itemList.size)
                    /*
                    if(!isPastEvents) {
                        if (isSignedUp) {
                            buttonRSVP.setText(R.string.unregister)
                            buttonRSVP.backgroundTintList = null
                            buttonRSVP.setTextColor(Color.BLACK)
                            buttonRSVP.isEnabled = true

                            buttonInterested.backgroundTintList = null
                            buttonInterested.text = resources.getString(R.string.unregister)
                            buttonInterested.setTextColor(Color.BLACK)
                            buttonInterested.isEnabled = true
                        } else {
                            buttonRSVP.setText(R.string.rsvp)
                        }
                    }
                    else {
                        buttonRSVP.setText(R.string.expired)
                        buttonRSVP.backgroundTintList = null
                        buttonRSVP.setTextColor(Color.BLACK)
                        buttonRSVP.isEnabled = false

                        buttonInterested.backgroundTintList = null
                        buttonInterested.text = resources.getString(R.string.expired)
                        buttonInterested.setTextColor(Color.BLACK)
                        buttonInterested.isEnabled = false
                    }
                     */
                    /*
                    if(approved) {
                        linearLayoutVerified.visibility = View.VISIBLE
                        bottomSheetView.background = ContextCompat.getDrawable(this@CommunityEventFragment.requireContext(), R.drawable.verified_shape)
                    }
                    else {
                        linearLayoutVerified.visibility = View.GONE
                        bottomSheetView.background = ContextCompat.getDrawable(this@CommunityEventFragment.requireContext(), R.drawable.round_corner)
                    }
                     */
                    setVerifiedAndRegistered(
                        context = this@CommunityEventFragment.requireContext(),
                        isVerified = approved,
                        isRegistered = isSignedUp,
                        isEventCard = false,
                        isPastEvents = isPastEvents,
                        linearLayoutVerified = linearLayoutVerified,
                        linearLayoutVerifiedAndIcon = linearLayoutVerifiedAndIcon,
                        textViewRegistered = textViewRegistered,
                        cardViewEvent = null,
                        bottomSheetView = bottomSheetView,
                    )

                    textHelpType.text = event.helpType?: "Help Type Required"

                    Log.d("query", "event.interest: ${event.interest}")
                    Log.d("query", "event.itemList.size: ${event.itemList.size}")

                    refreshBottomSheet(
                        event,
                        relativeLayoutImage,
                        textInterested,
                        buttonRSVP,
                        buttonInterested,
                        textViewEventStatus,
                        linearLayoutVerified,
                        linearLayoutVerifiedAndIcon,
                        textViewRegistered,
                    )

                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                    buttonInterested.setOnClickListener {
                        buttonRSVP.performClick()
                    }

                    buttonRSVP.setOnClickListener {
                        isSignedUp = event.signedUp
                        event.signedUp=!event.signedUp
                        if(isSignedUp){
                            setRSVPButton(
                                buttonRSVP = buttonRSVP,
                                textId = R.string.rsvp,
                                textColor = resources.getColor(R.color.accent_yellow, null),
                                backgroundColor = resources.getColor(R.color.dark_green, null)
                            )
                            /*
                            val color = resources.getColor(R.color.accent_yellow, null)
                            buttonRSVP.setText(R.string.rsvp)
                            buttonRSVP.backgroundTintList = ColorStateList.valueOf(
                                resources.getColor(R.color.dark_green, null)
                            )
                            buttonRSVP.setTextColor(color)
                             */
                            setButtonInterest(
                                buttonInterest = buttonInterested,
                                textId = R.string.sign_up,
                                textColor = resources.getColor(R.color.accent_yellow, null),
                                backgroundColor = resources.getColor(R.color.dark_green, null)
                            )
                            /*
                            buttonInterested.text = resources.getString(R.string.sign_up)
                            buttonInterested.backgroundTintList = ColorStateList.valueOf(
                                resources.getColor(R.color.dark_green, null)
                            )
                            buttonInterested.setTextColor(color)
                             */

                        }
                        else{
                            setRSVPButton(
                                buttonRSVP = buttonRSVP,
                                textId = R.string.unregister,
                                textColor = Color.BLACK,
                                backgroundColor = null
                            )
                            /*
                            buttonRSVP.setText(R.string.unregister)
                            buttonRSVP.backgroundTintList = null
                            buttonRSVP.setTextColor(Color.BLACK)
                             */
                            setButtonInterest(
                                buttonInterest = buttonInterested,
                                textId = R.string.unregister,
                                textColor = Color.BLACK,
                                backgroundColor = null
                            )
                            /*
                            buttonInterested.text = resources.getString(R.string.unregister)
                            buttonInterested.backgroundTintList = null
                            buttonInterested.setTextColor(Color.BLACK)
                             */
                            Log.d("interestedBtn", "${buttonInterested.text}, ${buttonInterested.backgroundTintList}, ${buttonInterested.currentTextColor}")
                        }
                        //(recyclerView?.adapter as CommunityRecyclerAdapter).notifyDataSetChanged()

                        eventDataAdapter.setLikedEvent(event){ event ->
                            refreshBottomSheet(
                                event,
                                relativeLayoutImage,
                                textInterested,
                                buttonRSVP,
                                buttonInterested,
                                textViewEventStatus,
                                linearLayoutVerified,
                                linearLayoutVerifiedAndIcon,
                                textViewRegistered,
                            )
                            (recyclerView.adapter as CommunityRecyclerAdapter).notifyItemChanged(position)
                            Log.d("Liked Event Firebase Update", "Liked Event Firebase Update Success")
                        }
                    }

                    buttonClose.setOnClickListener{
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }
            })

            //recyclerView!!.addItemDecoration(LinePaint())
            var changedType: String? = null
            var eventId: String? = null
            var eventTitle: String = "unknown event"

            arguments?.let {
                changedType = it.getString("changedType")
                eventId = it.getString("eventId")
                eventTitle = it.getString("eventTitle")?:"unknown event"
            }

            Log.d("workManager", "changedType: $changedType")
            Log.d("workManager", "eventId: $eventId")
            Log.d("workManager", "eventTitle: $eventTitle")

            if(changedType != null && eventId != null) {
                if(changedType == ChangedType.Add.type || changedType == ChangedType.Modify.type) {
                    val adapter = recyclerView.adapter as CommunityRecyclerAdapter
                    val pos = adapter.getItemPosition(eventId)
                    Log.d("getItemPosition", "In Fragment, pos: $pos")
                    pos?.let {
                        recyclerView.scrollToPosition(it)
                        val communityData = adapter.getItemAtPosition(it)
                        communityData?.event?.let{ event ->
                            adapter.clickItem(event, pos)
                        }
                    }
                }
                else if(changedType == ChangedType.Remove.type) {
                    Toast.makeText(activity?.applicationContext, "$eventTitle ${getString(R.string.event_removed)}", Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(activity?.applicationContext, "$eventTitle ${getString(R.string.no_event)}", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    private fun refreshBottomSheet(
        event: Event,
        relativeLayoutImage: RelativeLayout,
        textInterested: TextView,
        buttonRSVP: AppCompatButton,
        buttonInterested: AppCompatButton,
        textViewEventStatus: TextView,
        linearLayoutVerified: LinearLayout,
        linearLayoutVerifiedAndIcon: LinearLayout,
        textViewRegistered: TextView,
    ) {

        val isSignedUp = event.signedUp
        //val numOfInterest = event.interest?.minus(event.itemList.size)

        setVerifiedAndRegistered(
            context = this@CommunityEventFragment.requireContext(),
            isVerified = event.approved!!,
            isRegistered = isSignedUp,
            isEventCard = false,
            isPastEvents = isPastEvents,
            linearLayoutVerified = linearLayoutVerified,
            linearLayoutVerifiedAndIcon = linearLayoutVerifiedAndIcon,
            textViewRegistered = textViewRegistered,
            cardViewEvent = null,
            bottomSheetView = bottomSheetView,
        )

        if(!isPastEvents) {
            if (isSignedUp) {
                /*
                buttonRSVP.setText(R.string.unregister)
                buttonRSVP.backgroundTintList = null
                buttonRSVP.setTextColor(Color.BLACK)
                buttonRSVP.isEnabled = true
                 */
                setButtonInterest(
                    buttonInterest = buttonInterested,
                    textId = R.string.unregister,
                    textColor = Color.BLACK,
                    backgroundColor = null
                )
                /*
                buttonInterested.backgroundTintList = null
                buttonInterested.text = resources.getString(R.string.unregister)
                buttonInterested.setTextColor(Color.BLACK)
                buttonInterested.isEnabled = true
                 */
            } else {
                if(event.totalSlots == null || event.totalSlots == -1 || (event.participants?.size ?: 0) < event.totalSlots!!) {
                    val textColor = resources.getColor(R.color.accent_yellow, null)
                    /*
                    buttonRSVP.setText(R.string.rsvp)
                    buttonRSVP.backgroundTintList = ColorStateList.valueOf(
                        resources.getColor(R.color.dark_green, null)
                    )
                    buttonRSVP.setTextColor(textColor)
                     */

                    setButtonInterest(
                        buttonInterest = buttonInterested,
                        textId = R.string.sign_up,
                        textColor = textColor,
                        backgroundColor = resources.getColor(R.color.dark_green, null)
                    )
                    /*
                    buttonInterested.text = resources.getString(R.string.sign_up)
                    buttonInterested.backgroundTintList = ColorStateList.valueOf(
                        resources.getColor(R.color.dark_green, null)
                    )
                    buttonInterested.setTextColor(textColor)
                     */
                }
                else {
                    /*
                    buttonRSVP.setText(R.string.event_full)
                    buttonRSVP.backgroundTintList = null
                    buttonRSVP.setTextColor(Color.BLACK)
                    buttonRSVP.isEnabled = false
                     */

                    replaceButtonInterest(
                        buttonInterest = buttonInterested,
                        tvEventStatus = textViewEventStatus,
                        textId = R.string.event_full
                    )
                    /*
                    buttonInterested.backgroundTintList = null
                    buttonInterested.text = resources.getString(R.string.event_full)
                    buttonInterested.setTextColor(Color.BLACK)
                    buttonInterested.isEnabled = false
                     */
                }

            }
        }
        else {
            if(!event.signedUp) {
                /*
                buttonRSVP.setText(R.string.completed)
                buttonRSVP.backgroundTintList = null
                buttonRSVP.setTextColor(Color.BLACK)
                buttonRSVP.isEnabled = false
                 */
                replaceButtonInterest(
                    buttonInterest = buttonInterested,
                    tvEventStatus = textViewEventStatus,
                    textId = R.string.completed
                )
                /*
                buttonInterested.backgroundTintList = null
                buttonInterested.text = resources.getString(R.string.completed)
                buttonInterested.setTextColor(Color.BLACK)
                buttonInterested.isEnabled = false
                 */
            }
            else {
                /*
                buttonRSVP.setText(R.string.attended)
                buttonRSVP.backgroundTintList = null
                buttonRSVP.setTextColor(Color.BLACK)
                buttonRSVP.isEnabled = false
                 */
                replaceButtonInterest(
                    buttonInterest = buttonInterested,
                    tvEventStatus = textViewEventStatus,
                    textId = R.string.attended
                )
                /*
                buttonInterested.backgroundTintList = null
                buttonInterested.text = resources.getString(R.string.attended)
                buttonInterested.setTextColor(Color.BLACK)
                buttonInterested.isEnabled = false
                 */
            }
        }
        /*
        if(event.signedUp){
            buttonRSVP.setText(R.string.unregister)
            buttonRSVP.backgroundTintList = null
            buttonRSVP.setTextColor(Color.BLACK)

            buttonInterested.text = resources.getString(R.string.unregister)
            buttonInterested.backgroundTintList = null
            buttonInterested.setTextColor(Color.BLACK)
            Log.d("interestedBtn", "${buttonInterested.text}, ${buttonInterested.backgroundTintList}, ${buttonInterested.currentTextColor}")

        }
        else{
            val color = resources.getColor(R.color.accent_yellow, null)
            buttonRSVP.setText(R.string.rsvp)
            buttonRSVP.backgroundTintList = ColorStateList.valueOf(
                resources.getColor(R.color.dark_green, null)
            )
            buttonRSVP.setTextColor(color)

            buttonInterested.text = resources.getString(R.string.sign_up)
            buttonInterested.backgroundTintList = ColorStateList.valueOf(
                resources.getColor(R.color.dark_green, null)
            )
            buttonInterested.setTextColor(color)

        }
        */
        //refreshNumOfInterestAndProfileImg(event, textInterested, relativeLayoutImage)

        // refreshNumOfInterest
        refreshNumOfInterest(event, textInterested, isPastEvents)

    }

    private fun pastEventsItemSelected(parent: AdapterView<*>, pos: Int) {
        var shouldUpdateSelectedItemPos = true
        val selectedItem = parent.getItemAtPosition(pos)
        when(selectedItem.toString()) {
            "Select..." -> {
                shouldUpdateSelectedItemPos = false
            }
            "Last 7 days" -> {
                val targetDate = Timestamp(Date(System.currentTimeMillis() - getDayInMilliSec(7)))
                refreshEvents(
                    eventDataAdapter,
                    this@CommunityEventFragment.resources,
                    getQueryToFilterEventsAfterTargetDate(targetDate, isPastEvents),
                    userInputText
                )
                searchEvents(
                    eventDataAdapter,
                    this@CommunityEventFragment.resources,
                    getQueryToFilterEventsAfterTargetDate(targetDate, isPastEvents),
                )
            }
            "Last 30 days" -> {
                val targetDate = Timestamp(Date(System.currentTimeMillis() - getDayInMilliSec(30)))
                refreshEvents(
                    eventDataAdapter,
                    this@CommunityEventFragment.resources,
                    getQueryToFilterEventsAfterTargetDate(targetDate, isPastEvents),
                    userInputText
                )
                searchEvents(
                    eventDataAdapter,
                    this@CommunityEventFragment.resources,
                    getQueryToFilterEventsAfterTargetDate(targetDate, isPastEvents),
                )
            }
            "Last 60 days" -> {
                val targetDate = Timestamp(Date(System.currentTimeMillis() - getDayInMilliSec(60)))
                refreshEvents(
                    eventDataAdapter,
                    this@CommunityEventFragment.resources,
                    getQueryToFilterEventsAfterTargetDate(targetDate, isPastEvents),
                    userInputText
                )
                searchEvents(
                    eventDataAdapter,
                    this@CommunityEventFragment.resources,
                    getQueryToFilterEventsAfterTargetDate(targetDate, isPastEvents),
                )
            }
            "Last 90 days" -> {
                val targetDate = Timestamp(Date(System.currentTimeMillis() - getDayInMilliSec(90)))
                refreshEvents(
                    eventDataAdapter,
                    this@CommunityEventFragment.resources,
                    getQueryToFilterEventsAfterTargetDate(targetDate, isPastEvents),
                    userInputText
                )
                searchEvents(
                    eventDataAdapter,
                    this@CommunityEventFragment.resources,
                    getQueryToFilterEventsAfterTargetDate(targetDate, isPastEvents),
                )
            }
            "Other past events" -> {
                val targetDate = Timestamp(Date(System.currentTimeMillis() - getDayInMilliSec(90)))
                refreshEvents(
                    eventDataAdapter,
                    this@CommunityEventFragment.resources,
                    getQueryToFilterEventsBeforeTargetDate(targetDate, isPastEvents),
                    userInputText
                )
                searchEvents(
                    eventDataAdapter,
                    this@CommunityEventFragment.resources,
                    getQueryToFilterEventsBeforeTargetDate(targetDate, isPastEvents),
                )
            }
            "Reset" -> {
                refreshEvents(
                    eventDataAdapter,
                    this@CommunityEventFragment.resources,
                    defaultQuery,
                    userInputText
                )
                searchEvents(
                    eventDataAdapter,
                    this@CommunityEventFragment.resources,
                    defaultQuery,
                )
            }
        }
        if(shouldUpdateSelectedItemPos) selectedItemPos = pos
        Log.d("filter", "selectedItem: $selectedItem")
    }

    private fun upcomingEventsItemSelected(parent: AdapterView<*>, pos: Int) {
        var shouldUpdateSelectedItemPos = true
        val selectedItem = parent.getItemAtPosition(pos)
        when(selectedItem.toString()) {
            "Select..." -> {
                shouldUpdateSelectedItemPos = false
            }
            "Next 7 days" -> {
                val targetDate = Timestamp(Date(System.currentTimeMillis() + getDayInMilliSec(7)))
                refreshEvents(
                    eventDataAdapter,
                    this@CommunityEventFragment.resources,
                    getQueryToFilterEventsBeforeTargetDate(targetDate, isPastEvents,Query.Direction.ASCENDING),
                    userInputText
                )
                searchEvents(
                    eventDataAdapter,
                    this@CommunityEventFragment.resources,
                    getQueryToFilterEventsBeforeTargetDate(targetDate, isPastEvents, Query.Direction.ASCENDING),
                )
            }
            "Next 30 days" -> {
                val targetDate = Timestamp(Date(System.currentTimeMillis() + getDayInMilliSec(30)))
                refreshEvents(
                    eventDataAdapter,
                    this@CommunityEventFragment.resources,
                    getQueryToFilterEventsBeforeTargetDate(targetDate, isPastEvents, Query.Direction.ASCENDING),
                    userInputText
                )
                searchEvents(
                    eventDataAdapter,
                    this@CommunityEventFragment.resources,
                    getQueryToFilterEventsBeforeTargetDate(targetDate, isPastEvents, Query.Direction.ASCENDING),
                )
            }
            "Next 60 days" -> {
                val targetDate = Timestamp(Date(System.currentTimeMillis() + getDayInMilliSec(60)))
                refreshEvents(
                    eventDataAdapter,
                    this@CommunityEventFragment.resources,
                    getQueryToFilterEventsBeforeTargetDate(targetDate, isPastEvents, Query.Direction.ASCENDING),
                    userInputText
                )
                searchEvents(
                    eventDataAdapter,
                    this@CommunityEventFragment.resources,
                    getQueryToFilterEventsBeforeTargetDate(targetDate, isPastEvents, Query.Direction.ASCENDING),
                )
            }
            "Next 90 days" -> {
                val targetDate = Timestamp(Date(System.currentTimeMillis() + getDayInMilliSec(90)))
                refreshEvents(
                    eventDataAdapter,
                    this@CommunityEventFragment.resources,
                    getQueryToFilterEventsBeforeTargetDate(targetDate, isPastEvents, Query.Direction.ASCENDING),
                    userInputText
                )
                searchEvents(
                    eventDataAdapter,
                    this@CommunityEventFragment.resources,
                    getQueryToFilterEventsBeforeTargetDate(targetDate, isPastEvents, Query.Direction.ASCENDING),
                )
            }
            "Other upcoming events" -> {
                val targetDate = Timestamp(Date(System.currentTimeMillis() + getDayInMilliSec(90)))
                refreshEvents(
                    eventDataAdapter,
                    this@CommunityEventFragment.resources,
                    getQueryToFilterEventsAfterTargetDate(targetDate, isPastEvents, Query.Direction.ASCENDING),
                    userInputText
                )
                searchEvents(
                    eventDataAdapter,
                    this@CommunityEventFragment.resources,
                    getQueryToFilterEventsAfterTargetDate(targetDate, isPastEvents, Query.Direction.ASCENDING),
                )
            }
            "Reset" -> {
                refreshEvents(
                    eventDataAdapter,
                    this@CommunityEventFragment.resources,
                    defaultQuery,
                    userInputText
                )
                searchEvents(
                    eventDataAdapter,
                    this@CommunityEventFragment.resources,
                    defaultQuery,
                )
            }
        }
        if(shouldUpdateSelectedItemPos) selectedItemPos = pos
        Log.d("filter", "selectedItem: $selectedItem")
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        // An item is selected.
        // You can retrieve the selected item using parent.getItemAtPosition(pos).
        if(isPastEvents) {
            pastEventsItemSelected(parent, pos)
        }
        else {
            upcomingEventsItemSelected(parent, pos)
        }

    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback.
    }


    /*
        private suspend fun syncLikedEventsAndEvents() {
            val db = Firebase.firestore
            val eventIdNumOfInterestMap = mutableMapOf<String, Int>()

            val likedEvents = db.collection("likedEvents").get().await()
            Log.d("sync", "likedEvents size: ${likedEvents.size()}")

            for(likedEvent in likedEvents) {
                val eventId = likedEvent.get("eventId").toString()
                val numOfInterest = eventIdNumOfInterestMap.getOrDefault(eventId, 0)
                eventIdNumOfInterestMap[eventId] = numOfInterest + 1
            }
            Log.d("sync", "eventIdNumOfInterestMap: $eventIdNumOfInterestMap")
            eventIdNumOfInterestMap.forEach { entry ->
                val eventId = entry.key
                val numOfInterest = entry.value
                val docRef = db.collection("events").document(eventId).get().await()

                if(docRef.exists()) {
                    db.collection("events").document(eventId)
                        .update("interest", numOfInterest)
                        .addOnSuccessListener {
                            Log.d("sync", "$eventId: $numOfInterest")
                        }
                        .addOnFailureListener {
                            Log.d("sync", "failed to update events")
                        }
                    if(docRef.get("time") == null) {
                        db.collection("events").document(eventId)
                            .update("time", "12:15")
                            .addOnSuccessListener {
                                Log.d("sync", "$eventId: 12:15")
                            }
                            .addOnFailureListener {
                                Log.d("sync", "failed to update events")
                            }
                    }


                }
                else {
                    val event = hashMapOf(
                        "date" to Timestamp(Date(Calendar.getInstance().timeInMillis)),
                        "description" to "test event",
                        "interest" to numOfInterest,
                        "location" to "San Diego",
                        "status" to "Approved",
                        "title" to "San Diego Street Care",
                        "time"  to "12:15"
                    )

                    db.collection("events").document(eventId)
                        .set(event)
                        .addOnSuccessListener {
                            Log.d("sync", "saved new event: $eventId")
                        }
                        .addOnFailureListener {
                            Log.d("sync", "failed to save new event: $eventId")
                        }
                }
            }
        }

     */

}