package org.brightmindenrichment.street_care.ui.user

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.card.MaterialCardView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.util.Extensions
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentLikedPostsBinding
import org.brightmindenrichment.street_care.ui.community.adapter.CommunityRecyclerAdapter
import org.brightmindenrichment.street_care.ui.community.data.Event
import org.brightmindenrichment.street_care.ui.community.data.EventDataAdapter
import org.brightmindenrichment.street_care.ui.community.model.CommunityPageName
import org.brightmindenrichment.street_care.util.Queries.getLikedEventsQuery
import org.brightmindenrichment.street_care.util.Extensions.Companion.toPx
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Fragment to display user's liked posts from the community
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
    
    // Debounced search variables
    private var searchJob: kotlinx.coroutines.Job? = null

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
        loadLikedPosts()
    }

    private fun setupRecyclerView() {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerViewLikedPosts)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        val communityRecyclerAdapter =
            CommunityRecyclerAdapter(eventDataAdapter, CommunityPageName.UPCOMING_EVENTS)
        recyclerView?.adapter = communityRecyclerAdapter
    }

    private fun setupSearchAndFilter() {
        // Setup search view
        searchView = view!!.findViewById(R.id.search_view)
        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                userInputText = query ?: ""
                applyAllFilters()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                userInputText = newText ?: ""
                
                // Cancel previous search job
                searchJob?.cancel()
                
                // Start new debounced search
                searchJob = scope.launch {
                    delay(300) // Wait 300ms after user stops typing
                    applyAllFilters()
                }
                return false
            }
        })

        // Setup filter spinner
        spinner = Spinner(requireContext())
        spinner.onItemSelectedListener = this
        spinner.dropDownHorizontalOffset = (-130).toPx()
        spinner.dropDownVerticalOffset = 40.toPx()
        spinner.dropDownWidth = 180.toPx()
        spinner.background =
            ResourcesCompat.getDrawable(resources, R.drawable.filter_layer, null)

        // Menu items
        menuItems = listOf(
            getString(R.string.select),
            getString(R.string.next_7_days),
            getString(R.string.next_30_days),
            getString(R.string.next_60_days),
            getString(R.string.next_90_days),
            getString(R.string.other_upcoming_events),
            getString(R.string.reset)
        )

        // Custom adapter so spinner text doesn’t show in toolbar
        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            menuItems
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.text = "" // hide label in toolbar
                return view
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                return super.getDropDownView(position, convertView, parent)
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        setupToolbarMenu()
    }

    private fun setupToolbarMenu() {
        val menuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                menu.clear()

                val itemEventsFilter = menu.add(Menu.NONE, 0, 0, "events filter").apply {
                    actionView = spinner
                    setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                }

            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {}

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupBottomSheet() {
        // reserved for future
    }

    private fun loadLikedPosts() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            showEmptyState()
            return
        }

        showLoadingState()

        Firebase.firestore
            .collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { userDocument ->
                val likedEventIds =
                    userDocument.get("likedOutreach") as? List<String> ?: emptyList()

                if (likedEventIds.isEmpty()) {
                    showEmptyState()
                    return@addOnSuccessListener
                }

                fetchEventsByIds(likedEventIds)
            }
            .addOnFailureListener { exception ->
                Log.e("LikedPostsFragment", "Error loading user liked events", exception)
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
                        }
                    } catch (e: Exception) {
                        Log.e("LikedPostsFragment", "Exception parsing doc ${document.id}", e)
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

            val location = document.get("location")
            event.location = when (location) {
                is Map<*, *> -> {
                    val street = location["street"]?.toString() ?: ""
                    val city = location["city"]?.toString() ?: ""
                    val state = location["state"]?.toString() ?: ""
                    val zipcode = location["zipcode"]?.toString() ?: ""
                    "$street, $city, $state $zipcode".trim()
                }
                is String -> location
                else -> "No Location"
            }

            val dateField = document.get("eventDate") ?: document.get("date")
            event.date = when (dateField) {
                is Timestamp -> DateFormat.format("MMM dd, yyyy", dateField.toDate()).toString()
                is String -> dateField
                else -> "No Date"
            }

            event.time = when {
                document.getString("time") != null -> document.getString("time")!!
                document.get("eventStartTime") is String -> document.getString("eventStartTime")!!
                document.get("eventStartTime") is Timestamp -> {
                    val timestamp = document.getTimestamp("eventStartTime")
                    DateFormat.format("HH:mm", timestamp?.toDate()).toString()
                }
                else -> "No Time"
            }

            event.interest =
                (document.getLong("interests") ?: document.getLong("interest") ?: 0).toInt()

            event
        } catch (e: Exception) {
            Log.e("LikedPostsFragment", "Error creating event from doc ${document.id}", e)
            null
        }
    }

    private fun handleFetchCompletion(events: List<Event>) {
        if (events.isNotEmpty()) showEvents(events) else showEmptyState()
    }

    private fun showLoadingState() {
        binding.progressBarLoading.visibility = View.VISIBLE
        binding.recyclerViewLikedPosts.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE
    }

    private fun showEvents(events: List<Event>) {
        allEvents.clear()
        allEvents.addAll(events)

        binding.progressBarLoading.visibility = View.GONE
        binding.recyclerViewLikedPosts.visibility = View.VISIBLE
        binding.emptyStateLayout.visibility = View.GONE

        val recyclerView = binding.recyclerViewLikedPosts
        recyclerView.layoutManager = LinearLayoutManager(context)

        val communityDataList = mutableListOf<org.brightmindenrichment.street_care.ui.community.data.CommunityData>()
        var prevMonth: String? = null
        var prevDay: String? = null

        val sortedEvents = events.sortedBy { it.date ?: "" }

        sortedEvents.forEach { event ->
            val dateString = event.date ?: "Unknown"
            val month = when {
                dateString.contains("Jan") -> "January"
                dateString.contains("Feb") -> "February"
                dateString.contains("Mar") -> "March"
                dateString.contains("Apr") -> "April"
                dateString.contains("May") -> "May"
                dateString.contains("Jun") -> "June"
                dateString.contains("Jul") -> "July"
                dateString.contains("Aug") -> "August"
                dateString.contains("Sep") -> "September"
                dateString.contains("Oct") -> "October"
                dateString.contains("Nov") -> "November"
                dateString.contains("Dec") -> "December"
                else -> "Unknown"
            }
            val dayOfMonth = dateString.replace(Regex("[^0-9]"), "")

            if (prevMonth == null || month != prevMonth) {
                prevMonth = month
                prevDay = dayOfMonth
                event.layoutType = org.brightmindenrichment.street_care.util.Extensions.TYPE_NEW_DAY

                val eventYear = org.brightmindenrichment.street_care.ui.community.data.EventYear()
                eventYear.year = "$month 2025"
                communityDataList.add(
                    org.brightmindenrichment.street_care.ui.community.data.CommunityData(
                        eventYear,
                        org.brightmindenrichment.street_care.util.Extensions.TYPE_MONTH
                    )
                )
            } else if (dayOfMonth != prevDay) {
                prevDay = dayOfMonth
                event.layoutType = org.brightmindenrichment.street_care.util.Extensions.TYPE_NEW_DAY
            } else {
                event.layoutType = org.brightmindenrichment.street_care.util.Extensions.TYPE_DAY
            }

            communityDataList.add(
                org.brightmindenrichment.street_care.ui.community.data.CommunityData(
                    event,
                    event.layoutType!!
                )
            )
        }

        recyclerView.adapter = createCustomCommunityAdapter(communityDataList)
    }

    private fun createCustomCommunityAdapter(
        communityDataList: List<org.brightmindenrichment.street_care.ui.community.data.CommunityData>
    ): RecyclerView.Adapter<RecyclerView.ViewHolder> {
        return object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun getItemViewType(position: Int): Int {
                return communityDataList[position].layoutType
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return when (viewType) {
                    org.brightmindenrichment.street_care.util.Extensions.TYPE_MONTH -> {
                        val view = inflater.inflate(R.layout.card_community_year, parent, false)
                        MonthHeaderViewHolder(view)
                    }
                    org.brightmindenrichment.street_care.util.Extensions.TYPE_NEW_DAY,
                    org.brightmindenrichment.street_care.util.Extensions.TYPE_DAY -> {
                        val view = inflater.inflate(R.layout.event_list_layout, parent, false)
                        EventViewHolder(view)
                    }
                    else -> {
                        val view = inflater.inflate(R.layout.event_list_layout, parent, false)
                        EventViewHolder(view)
                    }
                }
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val communityData = communityDataList[position]
                when (holder) {
                    is MonthHeaderViewHolder -> holder.bind(communityData.eventYear?.year ?: "")
                    is EventViewHolder -> communityData.event?.let { holder.bind(it) }
                }
            }

            override fun getItemCount(): Int = communityDataList.size
        }
    }

    private class MonthHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val monthTextView: TextView =
            itemView.findViewById(R.id.textViewCommunityYear)
        fun bind(monthYear: String) {
            monthTextView.text = monthYear
        }
    }

    private class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewCommunityTitle)
        private val locationTextView: TextView = itemView.findViewById(R.id.textViewCommunityLocation)
        private val timeTextView: TextView = itemView.findViewById(R.id.textViewCommunityTime)
        private val dateTextView: TextView = itemView.findViewById(R.id.textViewDate)
        private val dayTextView: TextView = itemView.findViewById(R.id.textViewDay)
        private val interestTextView: TextView = itemView.findViewById(R.id.textInterested)
        private val helpTypeTextView: TextView = itemView.findViewById(R.id.tvHelpType)
        private val linearLayoutVerified: LinearLayout = itemView.findViewById(R.id.llVerifiedAndRegistered)
        private val linearLayoutVerifiedAndIcon: LinearLayout = itemView.findViewById(R.id.llVerifiedAndIcon)
        private val textViewRegistered: TextView = itemView.findViewById(R.id.tvRegistered)
        private val cardViewEvent: MaterialCardView = itemView.findViewById(R.id.cardViewEvent)
        private val ivVerificationMark: ImageView = itemView.findViewById(R.id.ivVerificationMark)
        private val ivFlag: ImageView = itemView.findViewById(R.id.ivFlag)
        private val btnLike: ImageButton = itemView.findViewById(R.id.btnLike)
        private val textViewLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)

        fun bind(event: Event) {
            // Basic event information (matching CommunityRecyclerAdapter)
            titleTextView.text = event.title
            locationTextView.text = if (!event.city.isNullOrEmpty() && !event.state.isNullOrEmpty()) {
                "${event.city}, ${event.state}"
            } else {
                event.location.orEmpty()
            }
            timeTextView.text = event.time.orEmpty()
            dateTextView.text = event.date.orEmpty()
            dayTextView.text = event.day.orEmpty()

            // Help type processing (matching CommunityRecyclerAdapter logic)
            val helpType = event.helpType ?: "Help Type Required"
            val cleanHelpType = helpType.replace(" and ", ",", ignoreCase = true)
            val helpTypeList = cleanHelpType.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val displayedHelpType = when {
                helpTypeList.size > 2 -> helpTypeList.take(2).joinToString(", ") + "..."
                helpTypeList.size == 2 -> helpTypeList.joinToString(", ")
                else -> helpTypeList.firstOrNull() ?: "Help Type Required"
            }
            helpTypeTextView.text = displayedHelpType

            // Verification and registration status (matching CommunityRecyclerAdapter)
            val approved = event.approved ?: false
            val isSignedUp = event.signedUp
            val isPastEvents = false // Liked posts are always upcoming events
            
            Extensions.setVerifiedAndRegistered(
                context = null,
                isVerified = approved,
                isRegistered = isSignedUp,
                isEventCard = true,
                linearLayoutVerified = linearLayoutVerified,
                linearLayoutVerifiedAndIcon = linearLayoutVerifiedAndIcon,
                textViewRegistered = textViewRegistered,
                cardViewEvent = cardViewEvent,
                bottomSheetView = null,
                isPastEvents = isPastEvents,
            )

            // Like state (matching CommunityRecyclerAdapter)
            val liked = event.likedByMe
            btnLike.setImageResource(
                if (liked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
            )
            btnLike.tag = if (liked) "liked" else "unliked"
            textViewLikeCount.text = event.likeCount.toString()

            // Interest count (matching CommunityRecyclerAdapter)
            Extensions.refreshNumOfInterest(event, interestTextView, isPastEvents, itemView.context)

            // Flag icon status (matching CommunityRecyclerAdapter)
            val isFlagged = event.isFlagged == true
            ivFlag.setColorFilter(
                ContextCompat.getColor(
                    itemView.context,
                    if (isFlagged) R.color.red else R.color.gray
                )
            )

            // Layout type handling (matching CommunityRecyclerAdapter)
            when (event.layoutType) {
                org.brightmindenrichment.street_care.util.Extensions.TYPE_DAY -> {
                    dateTextView.visibility = View.INVISIBLE
                    dayTextView.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun showEmptyState() {
        binding.progressBarLoading.visibility = View.GONE
        binding.recyclerViewLikedPosts.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.VISIBLE
    }

    private fun showErrorState() {
        binding.progressBarLoading.visibility = View.GONE
        binding.recyclerViewLikedPosts.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.textViewEmptyState.text = "Error loading liked posts"
    }

    private fun applyAllFilters() {
        // Always start from the full set of liked events
        var filteredList = allEvents.toList()

        // Apply text filter if search text is not empty
        if (userInputText.isNotBlank()) {
            val lowercaseQuery = userInputText.lowercase().trim()
            filteredList = filteredList.filter { event ->
                event.title.lowercase().contains(lowercaseQuery) ||
                event.description?.lowercase()?.contains(lowercaseQuery) == true ||
                event.location?.lowercase()?.contains(lowercaseQuery) == true ||
                event.city?.lowercase()?.contains(lowercaseQuery) == true ||
                event.state?.lowercase()?.contains(lowercaseQuery) == true
            }
        }

        // Apply date filter (still runs even if search is empty)
        filteredList = applyDateFilter(filteredList, selectedDateFilter)

        // Update and display
        filteredEvents.clear()
        filteredEvents.addAll(filteredList)
        showEvents(filteredEvents)
    }

    private fun applyDateFilter(events: List<Event>, filter: DateFilter): List<Event> {
        if (filter == DateFilter.ALL) return events

        val calendar = Calendar.getInstance()
        val currentDate = calendar.time

        return events.filter { event ->
            val eventDate = parseEventDate(event.date) ?: return@filter false
            val daysDifference =
                ((eventDate.time - currentDate.time) / (1000 * 60 * 60 * 24)).toInt()

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
            getString(R.string.select) -> return
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
                spinner.setSelection(0)
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
        // Cancel any pending search operations
        searchJob?.cancel()
        _binding = null
    }
}