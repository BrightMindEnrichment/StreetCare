package org.brightmindenrichment.street_care.ui.community

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.brightmindenrichment.street_care.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.ImageView
import android.widget.LinearLayout

class PublicEvent : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PublicVisitAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var textView: TextView
    private lateinit var searchView: SearchView

    // Model class for visit logs
    data class VisitLog(
        val id: String = "",
        val timestamp: Date? = null,
        val city: String = "",
        val state: String = "", // This holds the stateAbv value
        val whatGiven: String = "",
        val title: String = "",
        val userId: String = ""
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_public_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        setupSearch()

        // Load data
        loadPublicVisitLogs()
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterVisitLogs(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterVisitLogs(newText)
                return true
            }
        })
    }

    private fun filterVisitLogs(query: String?) {
        adapter.filter(query)
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
                    if (logsWithUsernames.isEmpty()) {
                        textView.visibility = View.VISIBLE
                        textView.text = getString(R.string.no_results_were_found)
                        recyclerView.visibility = View.GONE
                    } else {
                        textView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter.setData(logsWithUsernames)
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
            Log.d("PublicEvent", "Starting Firestore query")
            val querySnapshot = db.collection("visitLogWebProd")
                .whereEqualTo("public", true)
                .orderBy("dateTime", Query.Direction.DESCENDING)
                .limit(50) // Get a reasonable number of documents
                .get()
                .await()

            Log.d("PublicEvent", "Query returned ${querySnapshot.documents.size} documents")

            return@withContext querySnapshot.documents.mapNotNull { document ->
                try {
                    // Log the full document data for debugging
                    Log.d("PublicEvent", "Document ${document.id} raw data: ${document.data}")

                    // Extract fields from document with extra validation
                    val city = document.getString("city")?.trim() ?: ""

                    // CRITICAL FIX: Make sure we're getting stateAbv correctly
                    val stateAbv = document.getString("stateAbv")?.trim() ?: ""
                    Log.d("PublicEvent", "STATE VALUE FROM FIRESTORE - stateAbv: '$stateAbv'")

                    // Enhanced timestamp handling with multiple formats
                    val timestamp = try {
                        // Log the raw dateTime value for debugging
                        val rawDateTime = document.get("dateTime")
                        Log.d("PublicEvent", "Raw dateTime value: $rawDateTime (${rawDateTime?.javaClass?.simpleName})")

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
                                    "MMM dd, yyyy 'at' h:mm:ss a z"
                                )

                                var parsedDate: Date? = null
                                for (format in possibleFormats) {
                                    try {
                                        val sdf = SimpleDateFormat(format, Locale.US)
                                        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                                        parsedDate = sdf.parse(dateTimeString)
                                        if (parsedDate != null) {
                                            Log.d("PublicEvent", "Successfully parsed with format: $format")
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

                    Log.d("PublicEvent", "Final parsed values - city: '$city', state: '$stateAbv', timestamp: $timestamp")

                    // Make sure stateAbv goes into the state field of VisitLog
                    VisitLog(
                        id = document.id,
                        timestamp = timestamp,
                        city = city,
                        state = stateAbv, // Pass stateAbv to state field
                        whatGiven = whatGiven,
                        title = "Event", // Placeholder - will be replaced with username
                        userId = userId
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
        val updatedLogs = visitLogs.toMutableList()

        // Group logs by userId to minimize Firestore queries
        val userIds = visitLogs.map { it.userId }.distinct()
        Log.d("PublicEvent", "Fetching usernames for ${userIds.size} distinct users")

        // Create a map of userId to username
        val usernameMap = mutableMapOf<String, String>()

        for (userId in userIds) {
            if (userId.isNotEmpty()) {
                try {
                    val userDoc = db.collection("users").document(userId).get().await()

                    // Log the user document for debugging
                    Log.d("PublicEvent", "User document for $userId: ${userDoc.data}")

                    // Changed from "name" to "username" to match your Firestore schema
                    val username = userDoc.getString("username")

                    if (username != null) {
                        usernameMap[userId] = username
                        Log.d("PublicEvent", "Fetched username for $userId: $username")
                    } else {
                        Log.d("PublicEvent", "No username found for $userId, this card will be skipped")
                    }
                } catch (e: Exception) {
                    Log.e("PublicEvent", "Error fetching user: $userId", e)
                }
            }
        }

        // Only include logs where we have a valid username
        val filteredLogs = updatedLogs.filter { log ->
            val hasUsername = log.userId.isNotEmpty() && usernameMap.containsKey(log.userId)
            if (!hasUsername) {
                Log.d("PublicEvent", "Skipping log ${log.id} as no valid username was found")
            }
            hasUsername
        }

        // Update logs with usernames - fixed to avoid using 'continue' in map
        return@withContext filteredLogs.map { log ->
            // We've already filtered, so all logs should have usernames
            log.copy(title = usernameMap[log.userId] ?: "")
        }
    }

    inner class PublicVisitAdapter : RecyclerView.Adapter<PublicVisitAdapter.ViewHolder>() {
        private var visitLogs = listOf<VisitLog>()
        private var filteredLogs = listOf<VisitLog>()

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

            // Make the status icons visible
            init {
                statusIconsContainer.visibility = View.VISIBLE
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_interaction_public, parent, false)
            return ViewHolder(view)
        }

        @SuppressLint("SetTextIsString")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val visitLog = filteredLogs[position]

            // Set date and day with detailed logging
            visitLog.timestamp?.let { date ->
                try {
                    // Log raw date for debugging
                    Log.d("PublicEvent", "Raw date value: $date")

                    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                    val dateFormat = SimpleDateFormat("d", Locale.getDefault()) // Changed from "dd" to "d" to remove leading zero
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

            // CRITICAL FIX: Ensure city and state are correctly displayed
            val city = visitLog.city.trim()
            val state = visitLog.state.trim()

            // Log the values being used
            Log.d("PublicEvent", "LOCATION DISPLAY - City: '$city', State: '$state'")

            // Always try to include both, even if one is empty
            val locationText = if (city.isNotEmpty() && state.isNotEmpty()) {
                "$city, $state"
            } else if (city.isNotEmpty()) {
                "$city"
            } else if (state.isNotEmpty()) {
                state
            } else {
                "Location not specified"
            }

            Log.d("PublicEvent", "Final location text: '$locationText'")
            holder.location.text = locationText

            // Set whatGiven to eventCard_helpType
            holder.helpType.text = visitLog.whatGiven.takeIf { it.isNotEmpty() } ?: "Items provided"

            // Placeholder click listeners
            val placeholderClickListener = View.OnClickListener {
                Log.d("PublicEvent", "Card clicked - bottom sheet will be implemented later")
            }

            holder.rootLayout.setOnClickListener(placeholderClickListener)
            holder.detailsButton.setOnClickListener(placeholderClickListener)
            holder.flagIcon.setOnClickListener {
                Log.d("PublicEvent", "Flag icon clicked - functionality will be implemented later")
            }
        }

        override fun getItemCount(): Int = filteredLogs.size

        fun setData(logs: List<VisitLog>) {
            visitLogs = logs
            filteredLogs = logs
            Log.d("PublicEvent", "Adapter data set with ${logs.size} items")
            notifyDataSetChanged()
        }

        fun filter(query: String?) {
            if (query.isNullOrBlank()) {
                filteredLogs = visitLogs
            } else {
                val lowercaseQuery = query.lowercase()
                filteredLogs = visitLogs.filter { visitLog ->
                    visitLog.city.lowercase().contains(lowercaseQuery) ||
                            visitLog.state.lowercase().contains(lowercaseQuery) ||
                            visitLog.whatGiven.lowercase().contains(lowercaseQuery) ||
                            visitLog.title.lowercase().contains(lowercaseQuery)
                }
            }
            Log.d("PublicEvent", "Filter applied: ${filteredLogs.size} items after filtering")
            notifyDataSetChanged()
        }
    }
}