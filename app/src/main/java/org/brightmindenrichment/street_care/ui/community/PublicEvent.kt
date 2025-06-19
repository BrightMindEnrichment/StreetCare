package org.brightmindenrichment.street_care.ui.community

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.util.DebouncingQueryTextListener
import org.brightmindenrichment.street_care.util.Extensions.Companion.getDayInMilliSec
import org.brightmindenrichment.street_care.util.Extensions.Companion.toPx
import java.text.SimpleDateFormat
import java.util.*
import android.widget.ImageView
import android.widget.LinearLayout

class PublicEvent : Fragment(), AdapterView.OnItemSelectedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PublicVisitAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var textView: TextView
    private lateinit var searchView: SearchView

    // Filter state variables
    private var userInputText = ""
    private var selectedItemPos = -1
    private lateinit var menuItems: List<String>
    private var selectedDateFilter: DateFilter = DateFilter.ALL

    // All visit logs (unfiltered)
    private var allVisitLogs = listOf<VisitLog>()

    enum class DateFilter {
        ALL,
        LAST_7_DAYS,
        LAST_30_DAYS,
        LAST_60_DAYS,
        LAST_90_DAYS,
        OLDER_THAN_90_DAYS
    }

    // Model class for visit logs with mutable flag properties
    data class VisitLog(
        val id: String = "",
        val timestamp: Date? = null,
        val city: String = "",
        val state: String = "", // This holds the stateAbbv value
        val whatGiven: String = "",
        val title: String = "",
        val userId: String = "",
        val userType: String = "", // Added to store user type for verified icons
        val avatarUrl: String = "", // Added to store user avatar URL
        var isFlagged: Boolean = false, // Mutable flag status
        var flaggedByUser: String? = null // Mutable flagged by user
    ) {
        // Method to update flag status like in CommunityEventFragment
        fun updateFlagStatus(flagged: Boolean, flaggedBy: String?) {
            this.isFlagged = flagged
            this.flaggedByUser = flaggedBy
        }
    }

    // Data class for grouped items (header + logs)
    sealed class ListItem {
        data class Header(val monthYear: String, val count: Int) : ListItem()
        data class LogItem(val visitLog: VisitLog) : ListItem()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_public_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Try to find and set the title TextView - using safe approach
        try {
            val titleView = view.findViewById<TextView>(
                resources.getIdentifier("page_title", "id", requireContext().packageName)
            ) ?: view.findViewById<TextView>(
                resources.getIdentifier("toolbar_title", "id", requireContext().packageName)
            ) ?: view.findViewById<TextView>(
                resources.getIdentifier("fragment_title", "id", requireContext().packageName)
            ) ?: view.findViewById<TextView>(
                resources.getIdentifier("title", "id", requireContext().packageName)
            ) ?: view.findViewById<TextView>(
                resources.getIdentifier("tv_title", "id", requireContext().packageName)
            ) ?: view.findViewById<TextView>(
                resources.getIdentifier("community_title", "id", requireContext().packageName)
            )

            titleView?.text = "Public Interaction Logs"

            if (titleView == null) {
                Log.w("PublicEvent", "Could not find title TextView - check fragment_public_event.xml")
                // Try setting activity title as fallback
                requireActivity().title = "Public Interaction Logs"
            }
        } catch (e: Exception) {
            Log.e("PublicEvent", "Error setting title: ${e.message}")
            // Fallback to activity title
            requireActivity().title = "Public Interaction Logs"
        }

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerCommunity)
        progressBar = view.findViewById(R.id.progressBar)
        textView = view.findViewById(R.id.text_view)
        searchView = view.findViewById(R.id.search_view)

        // Initialize RecyclerView with proper layout settings for scrolling
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(false) // Allow dynamic sizing

        adapter = PublicVisitAdapter()
        recyclerView.adapter = adapter

        setupMenuAndFilters()
        setupSearch()

        // Load data
        loadPublicVisitLogs()
    }

    private fun setupMenuAndFilters() {
        val menuHost: MenuHost = requireActivity()

        // Setup filter menu items - only date filters now
        menuItems = listOf(
            getString(R.string.select),
            getString(R.string.last_7_days),
            getString(R.string.last_30_days),
            getString(R.string.last_60_days),
            getString(R.string.last_90_days),
            "Older than 90 days", // Using hardcoded string
            getString(R.string.reset)
        )

        // Setup spinner
        val spinner: Spinner = view?.findViewById(R.id.events_filter) ?: return
        spinner.onItemSelectedListener = this
        spinner.dropDownHorizontalOffset = (-130).toPx()
        spinner.dropDownVerticalOffset = 40.toPx()
        spinner.dropDownWidth = 180.toPx()

        val dataAdapter: ArrayAdapter<String> =
            object : ArrayAdapter<String>(this.requireContext(), android.R.layout.simple_spinner_item, menuItems) {
                override fun getDropDownView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup
                ): View {
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

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)

                // Only add the date filter spinner (removed location and items filters)
                spinner.background = ResourcesCompat.getDrawable(resources, R.drawable.filter_layer_past_events, null)
                spinner.layoutParams.width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40f, resources.displayMetrics).toInt()

                val itemEventsFilter = menu.add(Menu.NONE, 0, 1, "date filter").apply {
                    actionView = spinner
                    setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                }
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId) {
                    0 -> {
                        // Date filter spinner - handled by onItemSelected
                    }
                    else -> {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupSearch() {
        searchView.setIconifiedByDefault(false)
        searchView.isSubmitButtonEnabled = true
        searchView.imeOptions = EditorInfo.IME_ACTION_SEARCH
        searchView.queryHint = getString(R.string.search)

        searchView.setOnQueryTextListener(
            DebouncingQueryTextListener(lifecycle) { inputText ->
                inputText?.let {
                    userInputText = it
                    applyAllFilters()
                }
            }
        )
    }

    private fun applyAllFilters() {
        var filteredLogs = allVisitLogs.toList()

        // Apply text search filter
        if (userInputText.isNotEmpty()) {
            val lowercaseQuery = userInputText.lowercase()
            filteredLogs = filteredLogs.filter { visitLog ->
                visitLog.city.lowercase().contains(lowercaseQuery) ||
                        visitLog.state.lowercase().contains(lowercaseQuery) ||
                        visitLog.whatGiven.lowercase().contains(lowercaseQuery) ||
                        visitLog.title.lowercase().contains(lowercaseQuery)
            }
        }

        // Apply date filter only
        filteredLogs = applyDateFilter(filteredLogs, selectedDateFilter)

        // Group by month and create list items
        val groupedItems = groupLogsByMonth(filteredLogs)

        // Update UI
        updateUIWithFilteredData(groupedItems)
    }

    // Function to group logs by month
    private fun groupLogsByMonth(logs: List<VisitLog>): List<ListItem> {
        if (logs.isEmpty()) return emptyList()

        val monthFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val groupedItems = mutableListOf<ListItem>()

        // Group logs by month-year
        val logsByMonth = logs.groupBy { log ->
            log.timestamp?.let { monthFormat.format(it) } ?: "Unknown Date"
        }

        // Sort months chronologically (most recent first)
        val sortedMonths = logsByMonth.keys.sortedWith { month1, month2 ->
            try {
                val date1 = monthFormat.parse(month1)
                val date2 = monthFormat.parse(month2)
                // Sort descending (newest first)
                date2?.compareTo(date1) ?: 0
            } catch (e: Exception) {
                // If parsing fails, sort alphabetically
                month2.compareTo(month1)
            }
        }

        // Create list items: header followed by logs for each month
        for (month in sortedMonths) {
            val logsInMonth = logsByMonth[month] ?: emptyList()

            // Add header
            groupedItems.add(ListItem.Header(month, logsInMonth.size))

            // Add logs for this month (sorted by date within month, newest first)
            val sortedLogsInMonth = logsInMonth.sortedByDescending { it.timestamp }
            sortedLogsInMonth.forEach { log ->
                groupedItems.add(ListItem.LogItem(log))
            }
        }

        Log.d("PublicEvent", "Grouped ${logs.size} logs into ${sortedMonths.size} months")
        return groupedItems
    }

    private fun applyDateFilter(logs: List<VisitLog>, dateFilter: DateFilter): List<VisitLog> {
        if (dateFilter == DateFilter.ALL) return logs

        val currentTime = System.currentTimeMillis()

        return when (dateFilter) {
            DateFilter.LAST_7_DAYS -> {
                val targetDate = Date(currentTime - getDayInMilliSec(7))
                logs.filter { it.timestamp != null && it.timestamp >= targetDate }
            }
            DateFilter.LAST_30_DAYS -> {
                val targetDate = Date(currentTime - getDayInMilliSec(30))
                logs.filter { it.timestamp != null && it.timestamp >= targetDate }
            }
            DateFilter.LAST_60_DAYS -> {
                val targetDate = Date(currentTime - getDayInMilliSec(60))
                logs.filter { it.timestamp != null && it.timestamp >= targetDate }
            }
            DateFilter.LAST_90_DAYS -> {
                val targetDate = Date(currentTime - getDayInMilliSec(90))
                logs.filter { it.timestamp != null && it.timestamp >= targetDate }
            }
            DateFilter.OLDER_THAN_90_DAYS -> {
                val targetDate = Date(currentTime - getDayInMilliSec(90))
                logs.filter { it.timestamp != null && it.timestamp < targetDate }
            }
            else -> logs
        }
    }

    // Updated to handle grouped items
    private fun updateUIWithFilteredData(groupedItems: List<ListItem>) {
        if (groupedItems.isEmpty()) {
            textView.visibility = View.VISIBLE
            textView.text = getString(R.string.no_results_were_found)
            recyclerView.visibility = View.GONE
        } else {
            textView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.setGroupedData(groupedItems)
        }

        val totalLogs = groupedItems.count { it is ListItem.LogItem }
        Log.d("PublicEvent", "Applied filters - showing $totalLogs logs in ${groupedItems.count { it is ListItem.Header }} months")
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        var shouldUpdateSelectedItemPos = true
        val selectedItem = parent.getItemAtPosition(pos)

        when(selectedItem.toString()) {
            getString(R.string.select) -> {
                shouldUpdateSelectedItemPos = false
            }
            getString(R.string.last_7_days) -> {
                selectedDateFilter = DateFilter.LAST_7_DAYS
                applyAllFilters()
            }
            getString(R.string.last_30_days) -> {
                selectedDateFilter = DateFilter.LAST_30_DAYS
                applyAllFilters()
            }
            getString(R.string.last_60_days) -> {
                selectedDateFilter = DateFilter.LAST_60_DAYS
                applyAllFilters()
            }
            getString(R.string.last_90_days) -> {
                selectedDateFilter = DateFilter.LAST_90_DAYS
                applyAllFilters()
            }
            "Older than 90 days" -> { // Using hardcoded string to match menuItems
                selectedDateFilter = DateFilter.OLDER_THAN_90_DAYS
                applyAllFilters()
            }
            getString(R.string.reset) -> {
                selectedDateFilter = DateFilter.ALL
                userInputText = ""
                searchView.setQuery("", false)
                applyAllFilters()
            }
        }

        if(shouldUpdateSelectedItemPos) selectedItemPos = pos
        Log.d("PublicEvent", "Filter selected: $selectedItem")
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback.
    }

    private fun loadPublicVisitLogs() {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val visitLogs = fetchPublicVisitLogs()
                Log.d("PublicEvent", "Fetched ${visitLogs.size} visit logs initially")

                // Fetch usernames for all visit logs
                val logsWithUsernames = fetchUsernames(visitLogs)
                Log.d("PublicEvent", "After username fetching: ${logsWithUsernames.size} logs")

                withContext(Dispatchers.Main) {
                    // Store all logs for filtering
                    allVisitLogs = logsWithUsernames

                    if (logsWithUsernames.isEmpty()) {
                        textView.visibility = View.VISIBLE
                        textView.text = getString(R.string.no_results_were_found)
                        recyclerView.visibility = View.GONE
                    } else {
                        textView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE

                        // Group by month on initial load
                        val groupedItems = groupLogsByMonth(logsWithUsernames)
                        adapter.setGroupedData(groupedItems)

                        // Debug flag data after setting
                        adapter.debugFlagData()
                    }
                    progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    textView.visibility = View.VISIBLE
                    textView.text = "Error loading data: ${e.message}"
                    recyclerView.visibility = View.GONE
                    progressBar.visibility = View.GONE
                }
                Log.e("PublicEvent", "Error loading visit logs", e)
            }
        }
    }

    private suspend fun fetchPublicVisitLogs(): List<VisitLog> = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        try {
            Log.d("PublicEvent", "Starting Firestore query with fresh data fetch")

            // Force fresh data from server (not cache)
            val source = com.google.firebase.firestore.Source.SERVER

            val querySnapshot = db.collection("visitLogWebProd")
                .whereEqualTo("public", true)
                .whereEqualTo("status", "approved") // Only fetch approved status
                .orderBy("dateTime", Query.Direction.DESCENDING)
                // Remove or increase limit to get all data
                // .limit(500) // REMOVED - now fetches all documents
                .get(source) // Force server fetch, not cache
                .await()

            Log.d("PublicEvent", "Query returned ${querySnapshot.documents.size} approved documents from SERVER")

            // Log the first few document timestamps to debug ordering
            querySnapshot.documents.take(5).forEach { doc ->
                val rawDateTime = doc.get("dateTime")
                Log.d("PublicEvent", "Top document ${doc.id}: dateTime = $rawDateTime")
            }

            return@withContext querySnapshot.documents.mapNotNull { document ->
                try {
                    // Log the full document data for debugging
                    Log.d("PublicEvent", "Processing document ${document.id}")

                    // Verify status is approved (additional safety check)
                    val status = document.getString("status")
                    if (status != "approved") {
                        Log.w("PublicEvent", "Skipping document ${document.id} with status: $status")
                        return@mapNotNull null
                    }

                    // Extract fields from document with extra validation
                    val city = document.getString("city")?.trim() ?: ""

                    // FIXED: Correct field name is "stateAbbv" not "stateAbv"
                    val stateAbbv = document.getString("stateAbbv")?.trim() ?: ""

                    Log.d("PublicEvent", "CORRECTED STATE FIELD - stateAbbv: '$stateAbbv', city: '$city'")

                    // If state is still empty, log a warning
                    if (stateAbbv.isEmpty()) {
                        Log.w("PublicEvent", "WARNING: No state abbreviation found for document ${document.id}")
                    }

                    // Enhanced timestamp handling with multiple formats
                    val timestamp = try {
                        // Log the raw dateTime value for debugging
                        val rawDateTime = document.get("dateTime")
                        Log.d("PublicEvent", "Document ${document.id} - Raw dateTime: $rawDateTime (${rawDateTime?.javaClass?.simpleName})")

                        // Try standard Firestore timestamp first
                        val firestoreTimestamp = document.getTimestamp("dateTime")
                        if (firestoreTimestamp != null) {
                            Log.d("PublicEvent", "Using Firestore timestamp: $firestoreTimestamp")
                            firestoreTimestamp.toDate()
                        } else {
                            // Try string format as fallback
                            val dateTimeString = document.getString("dateTime")
                            if (dateTimeString != null) {
                                Log.d("PublicEvent", "Trying to parse dateTime string: $dateTimeString")

                                // Try different date formats
                                val possibleFormats = listOf(
                                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                                    "yyyy-MM-dd HH:mm:ss",
                                    "MMM dd, yyyy 'at' h:mm:ss a z",
                                    "yyyy-MM-dd'T'HH:mm:ssZ", // Additional format
                                    "yyyy-MM-dd'T'HH:mm:ss.SSSZ" // Additional format
                                )

                                var parsedDate: Date? = null
                                for (format in possibleFormats) {
                                    try {
                                        val sdf = SimpleDateFormat(format, Locale.US)
                                        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                                        parsedDate = sdf.parse(dateTimeString)
                                        if (parsedDate != null) {
                                            Log.d("PublicEvent", "Successfully parsed with format: $format -> $parsedDate")
                                            break
                                        }
                                    } catch (e: Exception) {
                                        // Try next format
                                        Log.d("PublicEvent", "Failed to parse with format $format: ${e.message}")
                                    }
                                }

                                if (parsedDate == null) {
                                    Log.w("PublicEvent", "Could not parse dateTime string with any format: $dateTimeString")
                                }

                                parsedDate
                            } else {
                                // Try number format (milliseconds since epoch)
                                val dateTimeLong = document.getLong("dateTime")
                                if (dateTimeLong != null) {
                                    Log.d("PublicEvent", "Using milliseconds timestamp: $dateTimeLong")
                                    Date(dateTimeLong)
                                } else {
                                    Log.w("PublicEvent", "No valid date format found in document")
                                    null
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("PublicEvent", "Error parsing dateTime: ${e.message}", e)
                        null
                    }

                    val userId = document.getString("uid") ?: ""

                    // Improved whatGiven array handling
                    val whatGiven = try {
                        val whatGivenRaw = document.get("whatGiven")
                        Log.d("PublicEvent", "Raw whatGiven: $whatGivenRaw (${whatGivenRaw?.javaClass?.simpleName})")

                        when (whatGivenRaw) {
                            is List<*> -> {
                                whatGivenRaw.mapNotNull { it?.toString() }.joinToString(", ")
                            }
                            is String -> whatGivenRaw
                            else -> "Items provided"
                        }
                    } catch (e: Exception) {
                        Log.e("PublicEvent", "Error parsing whatGiven: ${e.message}", e)
                        "Items provided"
                    }

                    // CRITICAL: Extract flag fields properly
                    val isFlagged = document.getBoolean("isFlagged") ?: false
                    val flaggedByUser = document.getString("flaggedByUser")

                    // Log flag data for every document
                    Log.d("PublicEvent", "Document ${document.id} flags - isFlagged: $isFlagged, flaggedByUser: $flaggedByUser")

                    Log.d("PublicEvent", "Final parsed approved document - city: '$city', state: '$stateAbbv', timestamp: $timestamp, status: $status, isFlagged: $isFlagged")

                    // Make sure stateAbbv goes into the state field of VisitLog
                    VisitLog(
                        id = document.id,
                        timestamp = timestamp,
                        city = city,
                        state = stateAbbv, // Pass stateAbbv to state field
                        whatGiven = whatGiven,
                        title = "Event", // Placeholder - will be replaced with username
                        userId = userId,
                        userType = "", // Will be filled when fetching usernames
                        avatarUrl = "", // Will be filled when fetching usernames
                        isFlagged = isFlagged,
                        flaggedByUser = flaggedByUser
                    )
                } catch (e: Exception) {
                    Log.e("PublicEvent", "Error parsing document ${document.id}: ${e.message}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("PublicEvent", "Error fetching documents: ${e.message}", e)
            return@withContext emptyList()
        }
    }

    private suspend fun fetchUsernames(visitLogs: List<VisitLog>): List<VisitLog> = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()

        // Group logs by userId to minimize Firestore queries
        val userIds = visitLogs.map { it.userId }.distinct()
        Log.d("PublicEvent", "Fetching usernames for ${userIds.size} distinct users")

        // Create maps for userId to username, userType, and avatarUrl
        val usernameMap = mutableMapOf<String, String>()
        val userTypeMap = mutableMapOf<String, String>()
        val avatarUrlMap = mutableMapOf<String, String>()

        for (userId in userIds) {
            if (userId.isNotEmpty()) {
                try {
                    val userDoc = db.collection("users").document(userId).get().await()

                    // Log the user document for debugging
                    Log.d("PublicEvent", "User document for $userId: ${userDoc.data}")

                    // Get username and user type
                    val username = userDoc.getString("username")
                    val userType = userDoc.getString("Type") ?: "" // Fetch user type for verified icons

                    if (username != null) {
                        usernameMap[userId] = username
                        userTypeMap[userId] = userType

                        // Fetch avatar URL from Firebase Storage
                        val avatarUrl = fetchAvatarUrl(userId)
                        avatarUrlMap[userId] = avatarUrl

                        Log.d("PublicEvent", "Fetched username: $username, type: $userType, avatar: $avatarUrl for $userId")
                    } else {
                        Log.d("PublicEvent", "No username found for $userId, this card will be skipped")
                    }
                } catch (e: Exception) {
                    Log.e("PublicEvent", "Error fetching user: $userId", e)
                }
            }
        }

        // Only include logs where we have a valid username
        val filteredLogs = visitLogs.filter { log ->
            val hasUsername = log.userId.isNotEmpty() && usernameMap.containsKey(log.userId)
            if (!hasUsername) {
                Log.d("PublicEvent", "Skipping log ${log.id} as no valid username was found")
            }
            hasUsername
        }

        // Update logs with usernames, user types, and avatar URLs while preserving flag data
        return@withContext filteredLogs.map { log ->
            val updatedLog = log.copy(
                title = usernameMap[log.userId] ?: "",
                userType = userTypeMap[log.userId] ?: "",
                avatarUrl = avatarUrlMap[log.userId] ?: ""
                // isFlagged and flaggedByUser are automatically preserved by copy()
            )

            // Verify flag data is preserved
            Log.d("PublicEvent", "Updated log ${updatedLog.id}: isFlagged=${updatedLog.isFlagged}, flaggedByUser=${updatedLog.flaggedByUser}")

            updatedLog
        }
    }

    private suspend fun fetchAvatarUrl(userId: String): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val storage = FirebaseStorage.getInstance()
            val avatarRef = storage.reference.child("webappUserImages/$userId.jpg")

            // Try to get the download URL
            val downloadUrl = avatarRef.downloadUrl.await()
            Log.d("PublicEvent", "Avatar URL found for $userId: $downloadUrl")
            downloadUrl.toString()
        } catch (e: Exception) {
            // If image doesn't exist or there's an error, try with .png extension
            try {
                val storage = FirebaseStorage.getInstance()
                val avatarRef = storage.reference.child("webappUserImages/$userId.png")
                val downloadUrl = avatarRef.downloadUrl.await()
                Log.d("PublicEvent", "Avatar URL (PNG) found for $userId: $downloadUrl")
                downloadUrl.toString()
            } catch (e2: Exception) {
                Log.d("PublicEvent", "No avatar found for $userId (tried both .jpg and .png)")
                "" // Return empty string if no avatar found
            }
        }
    }

    // Add manual refresh method
    private fun refreshData() {
        Log.d("PublicEvent", "Manual refresh triggered")
        loadPublicVisitLogs()
    }

    // Updated adapter to handle grouped items with headers
    inner class PublicVisitAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private var groupedItems = listOf<ListItem>()

        // Move constants outside companion object
        private val VIEW_TYPE_HEADER = 0
        private val VIEW_TYPE_LOG_ITEM = 1

        // ViewHolder for header items
        inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val headerText: TextView = itemView.findViewById(android.R.id.text1)
        }

        // ViewHolder for log items (same as before)
        inner class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val dateNumber: TextView = itemView.findViewById(R.id.eventCard_dateNumber)
            val dayName: TextView = itemView.findViewById(R.id.eventCard_dayName)
            val cardView: MaterialCardView = itemView.findViewById(R.id.eventCard_cardView)
            val title: TextView = itemView.findViewById(R.id.eventCard_title)
            val location: TextView = itemView.findViewById(R.id.eventCard_location)
            val time: TextView = itemView.findViewById(R.id.eventCard_time)
            val helpType: TextView = itemView.findViewById(R.id.eventCard_helpType)
            val detailsButton: TextView = itemView.findViewById(R.id.eventCard_detailsButton)
            val rootLayout: ConstraintLayout = itemView.findViewById(R.id.eventCard_rootLayout)
            val flagIcon: ImageView = itemView.findViewById(R.id.eventCard_flagIcon)
            val verifiedIcon: ImageView = itemView.findViewById(R.id.eventCard_verifiedIcon)
            val statusIconsContainer: LinearLayout = itemView.findViewById(R.id.eventCard_statusIconsContainer)
            val avatarImage: ImageView = itemView.findViewById(R.id.eventCard_avatar)

            // Make the status icons visible
            init {
                statusIconsContainer.visibility = View.VISIBLE
            }
        }

        override fun getItemViewType(position: Int): Int {
            return when (groupedItems[position]) {
                is ListItem.Header -> VIEW_TYPE_HEADER
                is ListItem.LogItem -> VIEW_TYPE_LOG_ITEM
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                VIEW_TYPE_HEADER -> {
                    // Create header layout programmatically or inflate from XML if you have one
                    val headerView = LayoutInflater.from(parent.context)
                        .inflate(android.R.layout.simple_list_item_1, parent, false)
                    HeaderViewHolder(headerView)
                }
                VIEW_TYPE_LOG_ITEM -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.fragment_interaction_public, parent, false)
                    LogViewHolder(view)
                }
                else -> throw IllegalArgumentException("Invalid view type")
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val item = groupedItems[position]) {
                is ListItem.Header -> {
                    val headerHolder = holder as HeaderViewHolder
                    headerHolder.headerText.text = "${item.monthYear} (${item.count} events)"
                    // Style the header with transparent background
                    headerHolder.headerText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.dark_green))
                    headerHolder.headerText.textSize = 16f
                    headerHolder.headerText.setPadding(16, 12, 16, 8)

                    // Set transparent background instead of darker gray
                    headerHolder.itemView.setBackgroundColor(Color.TRANSPARENT)
                }
                is ListItem.LogItem -> {
                    val logHolder = holder as LogViewHolder
                    bindLogItem(logHolder, item.visitLog, position)
                }
            }
        }

        @SuppressLint("SetTextIsString")
        private fun bindLogItem(holder: LogViewHolder, visitLog: VisitLog, position: Int) {
            Log.d("PublicEvent", "Binding position $position: id=${visitLog.id}, isFlagged=${visitLog.isFlagged}, flaggedByUser=${visitLog.flaggedByUser}")

            // Set date and day with detailed logging
            visitLog.timestamp?.let { date ->
                try {
                    // Log raw date for debugging
                    Log.d("PublicEvent", "Raw date value: $date")

                    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                    val dateFormat = SimpleDateFormat("dd", Locale.getDefault()) // Changed from "dd" to "d" to remove leading zero
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

                    // Remove any potential underline styling
                    holder.dayName.paintFlags = holder.dayName.paintFlags and android.graphics.Paint.UNDERLINE_TEXT_FLAG.inv()
                    holder.dateNumber.paintFlags = holder.dateNumber.paintFlags and android.graphics.Paint.UNDERLINE_TEXT_FLAG.inv()
                    holder.time.paintFlags = holder.time.paintFlags and android.graphics.Paint.UNDERLINE_TEXT_FLAG.inv()

                    val formattedDay = dayFormat.format(date).uppercase()
                    val formattedDate = dateFormat.format(date)
                    val formattedTime = timeFormat.format(date)

                    // Log formatted values
                    Log.d("PublicEvent", "Formatted values - Day: $formattedDay, Date: $formattedDate, Time: $formattedTime")

                    holder.dayName.text = formattedDay
                    holder.dateNumber.text = formattedDate
                    holder.time.text = formattedTime

                    // Make sure these text views are not clickable
                    holder.dayName.isClickable = false
                    holder.dateNumber.isClickable = false
                    holder.time.isClickable = false
                } catch (e: Exception) {
                    Log.e("PublicEvent", "Error formatting date: ${e.message}", e)
                    holder.dayName.text = "ERR"
                    holder.dateNumber.text = "--"
                    holder.time.text = "--:--"
                }
            } ?: run {
                // Handle null timestamp
                holder.dayName.text = "N/A"
                holder.dateNumber.text = "N/A"
                holder.time.text = "Time not available"
            }

            // Set title to username
            holder.title.text = visitLog.title

            // Fixed location display using correct field name
            val city = visitLog.city.trim()
            val state = visitLog.state.trim()

            // Simplified logic with proper state display
            val locationText = if (city.isNotEmpty() && state.isNotEmpty()) {
                "$city, $state"
            } else if (city.isNotEmpty()) {
                city
            } else if (state.isNotEmpty()) {
                state
            } else {
                "Location not specified"
            }

            Log.d("PublicEvent", "Location display - City: '$city', State: '$state', Final: '$locationText'")
            holder.location.text = locationText

            // Set whatGiven to eventCard_helpType
            holder.helpType.text = visitLog.whatGiven.takeIf { it.isNotEmpty() } ?: "Items provided"

            // Set verified icon based on user type using your existing drawable names
            // Always show the verified icon - default to Account Holder if type is not specified
            holder.verifiedIcon.visibility = View.VISIBLE
            when (visitLog.userType) {
                "Chapter Leader" -> {
                    holder.verifiedIcon.setImageResource(R.drawable.verified_green)
                    Log.d("PublicEvent", "Showing green verified icon for Chapter Leader")
                }
                "Street Care Hub Leader" -> {
                    holder.verifiedIcon.setImageResource(R.drawable.verified_blue)
                    Log.d("PublicEvent", "Showing blue verified icon for Street Care Hub Leader")
                }
                "Chapter Member" -> {
                    holder.verifiedIcon.setImageResource(R.drawable.verified_purple)
                    Log.d("PublicEvent", "Showing purple verified icon for Chapter Member")
                }
                "Account Holder" -> {
                    holder.verifiedIcon.setImageResource(R.drawable.verified_orange)
                    Log.d("PublicEvent", "Showing orange verified icon for Account Holder")
                }
                else -> {
                    // Default to Account Holder icon for any unspecified or unknown user types
                    holder.verifiedIcon.setImageResource(R.drawable.verified_orange)
                    Log.d("PublicEvent", "Showing default orange verified icon for unspecified user type: '${visitLog.userType}'")
                }
            }

            // CRITICAL: Initialize flag icon state IMMEDIATELY in onBindViewHolder
            holder.flagIcon.visibility = View.VISIBLE
            holder.flagIcon.isClickable = true

            // Clear any previous color filter
            holder.flagIcon.clearColorFilter()

            // Set the correct flag color based on current state
            val flagColor = if (visitLog.isFlagged) {
                Log.d("PublicEvent", "Setting flag to RED for ${visitLog.id}")
                R.color.red
            } else {
                Log.d("PublicEvent", "Setting flag to GRAY for ${visitLog.id}")
                R.color.gray
            }

            holder.flagIcon.setColorFilter(
                ContextCompat.getColor(holder.itemView.context, flagColor)
            )

            // IMPORTANT: Set the click listener EVERY time we bind
            holder.flagIcon.setOnClickListener { flagClickView ->
                Log.d("PublicEvent", "Flag clicked for ${visitLog.id}, current isFlagged: ${visitLog.isFlagged}")

                // Check authentication
                val currentUser = Firebase.auth.currentUser
                if (currentUser == null) {
                    Toast.makeText(
                        holder.itemView.context,
                        "Please log in to flag content",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                val db = FirebaseFirestore.getInstance()
                val visitLogRef = db.collection("visitLogWebProd").document(visitLog.id)
                val currentUserId = currentUser.uid

                // Handle flag/unflag logic
                if (visitLog.isFlagged) {
                    // Check if user can unflag
                    if (visitLog.flaggedByUser == currentUserId) {
                        Log.d("PublicEvent", "Unflagging ${visitLog.id}")

                        // Update Firestore first
                        val updates = mapOf(
                            "isFlagged" to false,
                            "flaggedByUser" to null
                        )

                        visitLogRef.update(updates)
                            .addOnSuccessListener {
                                Log.d("PublicEvent", "Firestore unflag successful")

                                // Update local object
                                visitLog.updateFlagStatus(false, null)

                                // Update UI immediately
                                holder.flagIcon.clearColorFilter()
                                holder.flagIcon.setColorFilter(
                                    ContextCompat.getColor(holder.itemView.context, R.color.gray)
                                )

                                // Update the lists to maintain consistency
                                updateLogInList(position, visitLog)

                                Log.d("PublicEvent", "UI updated to gray")
                            }
                            .addOnFailureListener { e ->
                                Log.e("PublicEvent", "Error unflagging: ", e)
                                Toast.makeText(
                                    holder.itemView.context,
                                    "Error updating flag status",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            holder.itemView.context,
                            "Only the user who flagged this content can unflag it.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.d("PublicEvent", "Flagging ${visitLog.id}")

                    // Update Firestore first
                    val updates = mapOf(
                        "isFlagged" to true,
                        "flaggedByUser" to currentUserId
                    )

                    visitLogRef.update(updates)
                        .addOnSuccessListener {
                            Log.d("PublicEvent", "Firestore flag successful")

                            // Update local object
                            visitLog.updateFlagStatus(true, currentUserId)

                            // Update UI immediately
                            holder.flagIcon.clearColorFilter()
                            holder.flagIcon.setColorFilter(
                                ContextCompat.getColor(holder.itemView.context, R.color.red)
                            )

                            // Update the lists to maintain consistency
                            updateLogInList(position, visitLog)

                            Log.d("PublicEvent", "UI updated to red")
                        }
                        .addOnFailureListener { e ->
                            Log.e("PublicEvent", "Error flagging: ", e)
                            Toast.makeText(
                                holder.itemView.context,
                                "Error updating flag status",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }

            // Load avatar image - default @drawable/avatar is already set in layout
            if (visitLog.avatarUrl.isNotEmpty()) {
                Log.d("PublicEvent", "Firebase avatar URL available: ${visitLog.avatarUrl}")
                // TODO: Load Firebase Storage image using Glide when dependency is added
                // For now, Firebase URLs are fetched but we keep the default avatar
                // Uncomment below when Glide is added:
                /*
                Glide.with(holder.itemView.context)
                    .load(visitLog.avatarUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.avatar) // Keep current default
                    .error(R.drawable.avatar) // Fallback to default
                    .circleCrop()
                    .into(holder.avatarImage)
                */
            } else {
                Log.d("PublicEvent", "No Firebase avatar found, using default avatar from layout")
                // Default @drawable/avatar is already set in the layout XML
                // No need to set it again unless you want to ensure it's there:
                // holder.avatarImage.setImageResource(R.drawable.avatar)
            }

            // Other click listeners - keep them simple for now
            holder.rootLayout.setOnClickListener {
                Log.d("PublicEvent", "Card clicked - implement details view later")
            }

            holder.detailsButton.setOnClickListener {
                Log.d("PublicEvent", "Details button clicked - implement later")
            }
        }

        override fun getItemCount(): Int = groupedItems.size

        // Method to set grouped data
        fun setGroupedData(items: List<ListItem>) {
            groupedItems = items
            Log.d("PublicEvent", "Adapter grouped data set with ${items.size} items")
            notifyDataSetChanged()
        }

        // Keep for backward compatibility but redirect to grouped data
        fun setData(logs: List<VisitLog>) {
            val groupedItems = groupLogsByMonth(logs)
            setGroupedData(groupedItems)
        }

        fun setFilteredData(logs: List<VisitLog>) {
            val groupedItems = groupLogsByMonth(logs)
            setGroupedData(groupedItems)
        }

        // Legacy filter method (kept for compatibility)
        fun filter(query: String?) {
            // This method is now handled by applyAllFilters() in the fragment
            Log.d("PublicEvent", "Filter method called - handled by fragment applyAllFilters()")
        }

        // Helper method to update the list with new flag status
        private fun updateLogInList(position: Int, updatedLog: VisitLog) {
            // Find and update the log in grouped items
            if (position < groupedItems.size) {
                val item = groupedItems[position]
                if (item is ListItem.LogItem) {
                    val mutableGroupedItems = groupedItems.toMutableList()
                    mutableGroupedItems[position] = ListItem.LogItem(updatedLog)
                    groupedItems = mutableGroupedItems
                }
            }

            Log.d("PublicEvent", "Lists updated for ${updatedLog.id}: isFlagged=${updatedLog.isFlagged}")
        }

        // Debug method to verify flag data
        fun debugFlagData() {
            Log.d("PublicEvent", "=== DEBUG FLAG DATA ===")
            groupedItems.forEachIndexed { index, item ->
                when (item) {
                    is ListItem.Header -> Log.d("PublicEvent", "[$index] HEADER: ${item.monthYear} (${item.count} events)")
                    is ListItem.LogItem -> Log.d("PublicEvent", "[$index] LOG: ${item.visitLog.id}: isFlagged=${item.visitLog.isFlagged}, flaggedByUser=${item.visitLog.flaggedByUser}")
                }
            }
            Log.d("PublicEvent", "=== END DEBUG ===")
        }
    }
}