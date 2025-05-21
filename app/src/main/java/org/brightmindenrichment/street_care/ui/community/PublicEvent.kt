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
        val state: String = "",
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

            // Log document IDs for debugging
            querySnapshot.documents.forEachIndexed { index, doc ->
                Log.d("PublicEvent", "Document $index: ${doc.id}")
            }

            return@withContext querySnapshot.documents.mapNotNull { document ->
                try {
                    // Extract fields from document
                    val city = document.getString("city") ?: ""
                    val stateAbv = document.getString("stateAbv") ?: ""
                    val timestamp = document.getTimestamp("dateTime")?.toDate()
                    val userId = document.getString("uid") ?: ""

                    // Handle whatGiven as an array
                    val whatGivenArray = document.get("whatGiven") as? List<*> ?: listOf<String>()
                    val whatGiven = whatGivenArray.joinToString(", ") { it.toString() }

                    Log.d("PublicEvent", "Parsed document ${document.id}: city=$city, state=$stateAbv")

                    VisitLog(
                        id = document.id,
                        timestamp = timestamp,
                        city = city,
                        state = stateAbv,
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
                    val username = userDoc.getString("name") ?: "Unknown User"
                    usernameMap[userId] = username
                    Log.d("PublicEvent", "Fetched username for $userId: $username")
                } catch (e: Exception) {
                    Log.e("PublicEvent", "Error fetching user: $userId", e)
                    usernameMap[userId] = "Unknown User"
                }
            }
        }

        // Update all logs with usernames
        for (i in updatedLogs.indices) {
            val log = updatedLogs[i]
            val username = if (log.userId.isNotEmpty()) usernameMap[log.userId] ?: "Unknown User" else "Anonymous"
            updatedLogs[i] = log.copy(title = username)
        }

        return@withContext updatedLogs
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

            // Set date and day
            visitLog.timestamp?.let { date ->
                val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

                holder.dayName.text = dayFormat.format(date).uppercase()
                holder.dateNumber.text = dateFormat.format(date)

                // Set dateTime to eventCard_time
                holder.time.text = timeFormat.format(date)
            } ?: run {
                // Handle null timestamp
                holder.dayName.text = "N/A"
                holder.dateNumber.text = "N/A"
                holder.time.text = "Time not available"
            }

            // Set title to username
            holder.title.text = visitLog.title

            // Set city and stateAbv to eventCard_location
            holder.location.text = "${visitLog.city}, ${visitLog.state}".takeIf { it != ", " } ?: "Location not specified"

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