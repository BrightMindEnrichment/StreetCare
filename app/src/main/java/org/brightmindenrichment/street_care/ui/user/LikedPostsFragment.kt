package org.brightmindenrichment.street_care.ui.user

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentLikedPostsBinding
import org.brightmindenrichment.street_care.ui.community.adapter.CommunityRecyclerAdapter
import org.brightmindenrichment.street_care.ui.community.data.Event
import org.brightmindenrichment.street_care.ui.community.data.EventDataAdapter
import org.brightmindenrichment.street_care.ui.community.model.CommunityPageName
import org.brightmindenrichment.street_care.util.Queries.getLikedEventsQuery
import org.brightmindenrichment.street_care.util.Extensions.Companion.toPx
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment to display user's liked posts from the community
 * Uses the same architecture as CommunityEventFragment for consistency
 */
class LikedPostsFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var _binding: FragmentLikedPostsBinding? = null
    private val binding get() = _binding!!
    
    private var scope = lifecycleScope
    private val eventDataAdapter = EventDataAdapter(scope)
    private var defaultQuery = getLikedEventsQuery()
    private var userInputText = ""
    private var selectedItemPos = -1
    
    // Search and filter components
    private lateinit var searchView: androidx.appcompat.widget.SearchView
    private lateinit var spinner: Spinner
    private var menuItems = listOf<String>()
    private var allEvents = mutableListOf<Event>()
    private var filteredEvents = mutableListOf<Event>()
    
    // Date filter enum
    enum class DateFilter {
        ALL, NEXT_7_DAYS, NEXT_30_DAYS, NEXT_60_DAYS, NEXT_90_DAYS, OTHER_UPCOMING
    }
    private var selectedDateFilter = DateFilter.ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLikedPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableAccessibilityHint")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupSearchAndFilter()
        setupRecyclerView()
        setupBottomSheet()
        loadLikedPosts()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh liked posts when fragment becomes visible
        loadLikedPosts()
    }

    private fun setupRecyclerView() {
        // Initialize the RecyclerView using the same approach as CommunityEventFragment
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerViewLikedPosts)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        // Initialize the adapter using the same controller
        var communityRecyclerAdapter = CommunityRecyclerAdapter(eventDataAdapter, CommunityPageName.UPCOMING_EVENTS)
        recyclerView?.adapter = communityRecyclerAdapter
    }
    
    private fun setupSearchAndFilter() {
        // Setup search view
        searchView = view!!.findViewById(R.id.search_view)
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                userInputText = query ?: ""
                applyAllFilters()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                userInputText = newText ?: ""
                applyAllFilters()
                return false
            }
        })
        
        // Setup filter spinner for toolbar
        spinner = Spinner(requireContext())
        spinner.onItemSelectedListener = this
        spinner.dropDownHorizontalOffset = (-130).toPx()
        spinner.dropDownVerticalOffset = 40.toPx()
        spinner.dropDownWidth = 180.toPx()
        
        // Setup filter menu items for liked events (using upcoming events filters)
        menuItems = listOf(
            getString(R.string.select),
            getString(R.string.next_7_days),
            getString(R.string.next_30_days),
            getString(R.string.next_60_days),
            getString(R.string.next_90_days),
            getString(R.string.other_upcoming_events),
            getString(R.string.reset)
        )
        
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, menuItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        
        // Add filter to toolbar using MenuProvider
        setupToolbarMenu()
    }
    
    private fun setupToolbarMenu() {
        val menuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                // Clear existing menu items to avoid duplicates
                menu.clear()
                
                // Add filter spinner to toolbar
                val itemEventsFilter = menu.add(Menu.NONE, 0, 0, "events filter").apply {
                    actionView = spinner
                    setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                }
                
            }

            override fun onCreateMenu(menu: Menu, menuInflater: android.view.MenuInflater) {
                // Not needed for this implementation
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle menu item selection if needed
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
    
    // Menu functionality removed for now - can be added later if needed
    
    private fun setupBottomSheet() {
        // Bottom sheet setup removed for now since it's not in the layout
        // Can be added later if needed
    }

    private fun loadLikedPosts() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            showEmptyState()
            return
        }

        showLoadingState()
        
        // First, get the liked event IDs from the likedEvents collection
        Firebase.firestore
            .collection("likedEvents")
            .whereEqualTo("uid", currentUser.uid)
            .get()
            .addOnSuccessListener { likedEventsSnapshot ->
                val likedEventIds = likedEventsSnapshot.documents.mapNotNull { doc ->
                    doc.getString("eventId")
                }
                
                if (likedEventIds.isEmpty()) {
                    showEmptyState()
                    return@addOnSuccessListener
                }
                
                // Now fetch the actual events from outreachEventsDev collection
                fetchEventsByIds(likedEventIds)
            }
            .addOnFailureListener { exception ->
                Log.e("LikedPostsFragment", "Error loading liked events", exception)
                showErrorState()
            }
    }
    
    private fun fetchEventsByIds(eventIds: List<String>) {
        if (eventIds.isEmpty()) {
            showEmptyState()
            return
        }
        
        val db = Firebase.firestore
        val events = mutableListOf<Event>()
        
        // Simple approach: just fetch the first batch (up to 10 events)
        val batch = eventIds.take(10)
        
        db.collection("outreachEventsDev")
            .whereIn(com.google.firebase.firestore.FieldPath.documentId(), batch)
                .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { document ->
                    
                    try {
                        val event = documentToEvent(document)
                        if (event != null) {
                            event.likedByMe = true
                            events.add(event)
                        } else {
                            Log.e("LikedPostsFragment", "Failed to parse event from document: ${document.id}")
                        }
                    } catch (e: Exception) {
                        Log.e("LikedPostsFragment", "Exception parsing document ${document.id}", e)
                    }
                }
                
                
                handleFetchCompletion(events)
                }
                .addOnFailureListener { exception ->
                Log.e("LikedPostsFragment", "Query failed", exception)
                            showErrorState()
                        }
                    }
    
    private fun documentToEvent(document: DocumentSnapshot): Event? {
        return try {
            val event = Event()
            event.eventId = document.id
            event.title = document.getString("title") ?: "No Title"
            event.description = document.getString("description") ?: "No Description"
            event.uid = document.getString("uid") ?: ""
            
            // Simple location parsing
            val location = document.get("location")
            when (location) {
                is Map<*, *> -> {
                    val street = location["street"]?.toString() ?: ""
                    val city = location["city"]?.toString() ?: ""
                    val state = location["state"]?.toString() ?: ""
                    val zipcode = location["zipcode"]?.toString() ?: ""
                    event.location = "$street, $city, $state $zipcode".trim()
                }
                is String -> {
                    event.location = location
                }
                else -> {
                    event.location = "No Location"
                }
            }
            
            // Simple date parsing
            val dateField = document.get("eventDate") ?: document.get("date")
            when (dateField) {
                is Timestamp -> {
                    event.date = DateFormat.format("MMM dd, yyyy", dateField.toDate()).toString()
                }
                is String -> {
                    event.date = dateField
                }
                else -> {
                    event.date = "No Date"
                }
            }
            
            // Simple time parsing - handle different data types
            event.time = when {
                document.getString("time") != null -> document.getString("time")!!
                document.get("eventStartTime") is String -> document.getString("eventStartTime")!!
                document.get("eventStartTime") is Timestamp -> {
                    val timestamp = document.getTimestamp("eventStartTime")
                    DateFormat.format("HH:mm", timestamp?.toDate()).toString()
                }
                else -> "No Time"
            }
            
            // Basic interest
            event.interest = (document.getLong("interests") ?: document.getLong("interest") ?: 0).toInt()
            
            event
            
        } catch (e: Exception) {
            Log.e("LikedPostsFragment", "Error creating event from document ${document.id}", e)
            null
        }
    }
    
    private fun handleFetchCompletion(events: List<Event>) {
        if (events.isNotEmpty()) {
            showEvents(events)
        } else {
            showEmptyState()
        }
    }
    
    private fun showLoadingState() {
        view?.findViewById<ProgressBar>(R.id.progressBarLoading)?.visibility = View.VISIBLE
        view?.findViewById<RecyclerView>(R.id.recyclerViewLikedPosts)?.visibility = View.GONE
        view?.findViewById<LinearLayout>(R.id.emptyStateLayout)?.visibility = View.GONE
    }
    
    private fun showEvents(events: List<Event>) {
        
        // Store all events for filtering
        allEvents.clear()
        allEvents.addAll(events)
        
        view?.findViewById<ProgressBar>(R.id.progressBarLoading)?.visibility = View.GONE
        view?.findViewById<RecyclerView>(R.id.recyclerViewLikedPosts)?.visibility = View.VISIBLE
        view?.findViewById<LinearLayout>(R.id.emptyStateLayout)?.visibility = View.GONE
        
        // Use the same approach as CommunityEventFragment
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerViewLikedPosts)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        
        // Create CommunityData objects with the same structure as future events
        val communityDataList = mutableListOf<org.brightmindenrichment.street_care.ui.community.data.CommunityData>()
        
        // Sort events by date (newest first)
        val sortedEvents = events.sortedByDescending { event ->
            try {
                val dateField = event.date
                when (dateField) {
                    is String -> {
                        // Try to parse the date string
                        if (dateField.contains("Sep")) {
                            // Simple parsing for "Sep 22, 2025" format
                            dateField.replace("Sep", "09").replace(" ", "").replace(",", "")
                        } else {
                            dateField
                        }
                    }
                    else -> "00000000"
                }
            } catch (e: Exception) {
                "00000000"
            }
        }
        
        
        // Convert events to CommunityData format (same as EventDataAdapter)
        var prevMonth: String? = null
        var prevDay: String? = null
        
        sortedEvents.forEach { event ->
            try {
                // Parse the date to determine layout type (same logic as EventDataAdapter)
                val dateString = event.date ?: "Unknown"
                if (dateString != "Unknown") {
                    // Simple date parsing - extract month and day
                    val month = when {
                        dateString.contains("Sep") -> "September"
                        dateString.contains("Oct") -> "October"
                        dateString.contains("Nov") -> "November"
                        dateString.contains("Dec") -> "December"
                        dateString.contains("Jan") -> "January"
                        dateString.contains("Feb") -> "February"
                        dateString.contains("Mar") -> "March"
                        dateString.contains("Apr") -> "April"
                        dateString.contains("May") -> "May"
                        dateString.contains("Jun") -> "June"
                        dateString.contains("Jul") -> "July"
                        dateString.contains("Aug") -> "August"
                        else -> "Unknown"
                    }
                    
                    val dayOfMonth = dateString.replace(Regex("[^0-9]"), "")
                    
                    // Determine layout type (same logic as EventDataAdapter)
                    when {
                        prevMonth == null -> {
                            // First event
                            prevMonth = month
                            prevDay = dayOfMonth
                            event.layoutType = org.brightmindenrichment.street_care.util.Extensions.TYPE_NEW_DAY
                            
                            // Add month header
                            val eventYear = org.brightmindenrichment.street_care.ui.community.data.EventYear()
                            eventYear.year = "$month 2025" // Assuming 2025 based on your data
                            val monthCommunity = org.brightmindenrichment.street_care.ui.community.data.CommunityData(eventYear, org.brightmindenrichment.street_care.util.Extensions.TYPE_MONTH)
                            communityDataList.add(monthCommunity)
                        }
                        month != prevMonth -> {
                            // New month
                            prevMonth = month
                            prevDay = dayOfMonth
                            event.layoutType = org.brightmindenrichment.street_care.util.Extensions.TYPE_NEW_DAY
                            
                            // Add month header
                            val eventYear = org.brightmindenrichment.street_care.ui.community.data.EventYear()
                            eventYear.year = "$month 2025"
                            val monthCommunity = org.brightmindenrichment.street_care.ui.community.data.CommunityData(eventYear, org.brightmindenrichment.street_care.util.Extensions.TYPE_MONTH)
                            communityDataList.add(monthCommunity)
                        }
                        dayOfMonth != prevDay -> {
                            // New day, same month
                            prevDay = dayOfMonth
                            event.layoutType = org.brightmindenrichment.street_care.util.Extensions.TYPE_NEW_DAY
                        }
                        else -> {
                            // Same day
                            event.layoutType = org.brightmindenrichment.street_care.util.Extensions.TYPE_DAY
                        }
                    }
                    
                    // Add the event
                    val communityEvent = org.brightmindenrichment.street_care.ui.community.data.CommunityData(event, event.layoutType!!)
                    communityDataList.add(communityEvent)
                    
                }
            } catch (e: Exception) {
                Log.e("LikedPostsFragment", "Error processing event date: ${event.title}", e)
                // Fallback: add event with default layout
                event.layoutType = org.brightmindenrichment.street_care.util.Extensions.TYPE_NEW_DAY
                val communityEvent = org.brightmindenrichment.street_care.ui.community.data.CommunityData(event, event.layoutType!!)
                communityDataList.add(communityEvent)
            }
        }
        
        
        // Create CommunityRecyclerAdapter with the same setup as CommunityEventFragment
        val communityRecyclerAdapter = CommunityRecyclerAdapter(eventDataAdapter, CommunityPageName.UPCOMING_EVENTS)
        
        // Since EventDataAdapter doesn't expose methods to set data directly,
        // we'll create a custom adapter that uses the same views as CommunityRecyclerAdapter
        recyclerView?.adapter = createCustomCommunityAdapter(communityDataList)
        
    }
    
    private fun createCustomCommunityAdapter(communityDataList: List<org.brightmindenrichment.street_care.ui.community.data.CommunityData>): RecyclerView.Adapter<RecyclerView.ViewHolder> {
        return object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            
            override fun getItemViewType(position: Int): Int {
                return communityDataList[position].layoutType
            }
            
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                
                return when (viewType) {
                    org.brightmindenrichment.street_care.util.Extensions.TYPE_MONTH -> {
                        val view = inflater.inflate(org.brightmindenrichment.street_care.R.layout.card_community_year, parent, false)
                        MonthHeaderViewHolder(view)
                    }
                    org.brightmindenrichment.street_care.util.Extensions.TYPE_NEW_DAY,
                    org.brightmindenrichment.street_care.util.Extensions.TYPE_DAY -> {
                        val view = inflater.inflate(org.brightmindenrichment.street_care.R.layout.event_list_layout, parent, false)
                        EventViewHolder(view)
                    }
                    else -> {
                        val view = inflater.inflate(org.brightmindenrichment.street_care.R.layout.event_list_layout, parent, false)
                        EventViewHolder(view)
                    }
                }
            }
            
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val communityData = communityDataList[position]
                
                when (holder) {
                    is MonthHeaderViewHolder -> {
                        val eventYear = communityData.eventYear
                        holder.bind(eventYear?.year ?: "")
                    }
                    is EventViewHolder -> {
                        val event = communityData.event
                        if (event != null) {
                            holder.bind(event)
                        }
                    }
                }
            }
            
            override fun getItemCount(): Int = communityDataList.size
        }
    }
    
    // ViewHolders for the custom adapter
    private class MonthHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val monthTextView: TextView = itemView.findViewById(org.brightmindenrichment.street_care.R.id.textViewCommunityYear)
        
        fun bind(monthYear: String) {
            monthTextView.text = monthYear
        }
    }
    
    private class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(org.brightmindenrichment.street_care.R.id.textViewCommunityTitle)
        private val locationTextView: TextView = itemView.findViewById(org.brightmindenrichment.street_care.R.id.textViewCommunityLocation)
        private val timeTextView: TextView = itemView.findViewById(org.brightmindenrichment.street_care.R.id.textViewCommunityTime)
        private val dateTextView: TextView = itemView.findViewById(org.brightmindenrichment.street_care.R.id.textViewDate)
        private val dayTextView: TextView = itemView.findViewById(org.brightmindenrichment.street_care.R.id.textViewDay)
        private val interestTextView: TextView = itemView.findViewById(org.brightmindenrichment.street_care.R.id.textInterested)
        
        fun bind(event: Event) {
            titleTextView.text = event.title
            locationTextView.text = event.location ?: "No location"
            timeTextView.text = event.time ?: "No time"
            dateTextView.text = event.date ?: "No date"
            dayTextView.text = "" // We can extract day from date if needed
            interestTextView.text = "${event.interest ?: 0} interested"
        }
    }
    
    private fun showEmptyState() {
        view?.findViewById<ProgressBar>(R.id.progressBarLoading)?.visibility = View.GONE
        view?.findViewById<RecyclerView>(R.id.recyclerViewLikedPosts)?.visibility = View.GONE
        view?.findViewById<LinearLayout>(R.id.emptyStateLayout)?.visibility = View.VISIBLE
    }
    
    private fun showErrorState() {
        view?.findViewById<ProgressBar>(R.id.progressBarLoading)?.visibility = View.GONE
        view?.findViewById<RecyclerView>(R.id.recyclerViewLikedPosts)?.visibility = View.GONE
        view?.findViewById<LinearLayout>(R.id.emptyStateLayout)?.visibility = View.VISIBLE
        view?.findViewById<LinearLayout>(R.id.emptyStateLayout)?.findViewById<TextView>(R.id.textViewEmptyState)?.text = "Error loading liked posts"
    }
    
    // Filter functionality removed for now - can be added later if needed
    
    // Filtering methods
    private fun applyAllFilters() {
        var filteredList = allEvents.toList()

        // Apply text search filter
        if (userInputText.isNotEmpty()) {
            val lowercaseQuery = userInputText.lowercase()
            filteredList = filteredList.filter { event ->
                event.title.lowercase().contains(lowercaseQuery) ||
                event.description?.lowercase()?.contains(lowercaseQuery) == true ||
                event.location?.lowercase()?.contains(lowercaseQuery) == true
            }
        }

        // Apply date filter
        filteredList = applyDateFilter(filteredList, selectedDateFilter)

        // Update the filtered events list
        filteredEvents.clear()
        filteredEvents.addAll(filteredList)

        // Update UI with filtered data
        showEvents(filteredEvents)
    }

    private fun applyDateFilter(events: List<Event>, filter: DateFilter): List<Event> {
        if (filter == DateFilter.ALL) return events

        val calendar = Calendar.getInstance()
        val currentDate = calendar.time

        return events.filter { event ->
            val eventDate = parseEventDate(event.date)
            if (eventDate == null) return@filter false

            val daysDifference = ((eventDate.time - currentDate.time) / (1000 * 60 * 60 * 24)).toInt()

            when (filter) {
                DateFilter.NEXT_7_DAYS -> daysDifference in 0..7
                DateFilter.NEXT_30_DAYS -> daysDifference in 0..30
                DateFilter.NEXT_60_DAYS -> daysDifference in 0..60
                DateFilter.NEXT_90_DAYS -> daysDifference in 0..90
                DateFilter.OTHER_UPCOMING -> daysDifference > 90
                else -> true
            }
        }
    }

    private fun parseEventDate(dateString: String?): Date? {
        if (dateString == null) return null
        
        return try {
            // Try to parse "Sep 22, 2025" format
            val format = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
            format.parse(dateString)
        } catch (e: Exception) {
            Log.e("LikedPostsFragment", "Error parsing date: $dateString", e)
            null
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val selectedItem = parent?.getItemAtPosition(position)?.toString() ?: return
        
        when (selectedItem) {
            getString(R.string.select) -> {
                // Do nothing
            }
            getString(R.string.next_7_days) -> {
                selectedDateFilter = DateFilter.NEXT_7_DAYS
                applyAllFilters()
            }
            getString(R.string.next_30_days) -> {
                selectedDateFilter = DateFilter.NEXT_30_DAYS
                applyAllFilters()
            }
            getString(R.string.next_60_days) -> {
                selectedDateFilter = DateFilter.NEXT_60_DAYS
                applyAllFilters()
            }
            getString(R.string.next_90_days) -> {
                selectedDateFilter = DateFilter.NEXT_90_DAYS
                applyAllFilters()
            }
            getString(R.string.other_upcoming_events) -> {
                selectedDateFilter = DateFilter.OTHER_UPCOMING
                applyAllFilters()
            }
            getString(R.string.reset) -> {
                selectedDateFilter = DateFilter.ALL
                userInputText = ""
                searchView.setQuery("", false)
                // Reset spinner to "Select" position
                spinner.setSelection(0)
                // Reload all events from Firebase
                loadLikedPosts()
            }
        }
        
        selectedItemPos = position
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        selectedItemPos = -1
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}