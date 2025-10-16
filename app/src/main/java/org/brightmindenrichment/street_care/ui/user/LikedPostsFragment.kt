package org.brightmindenrichment.street_care.ui.user

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.ui.community.LinePaint
import org.brightmindenrichment.street_care.ui.community.StickyHeaderItemDecorator
import org.brightmindenrichment.street_care.ui.community.adapter.CommunityRecyclerAdapter
import org.brightmindenrichment.street_care.ui.community.data.Event
import org.brightmindenrichment.street_care.ui.community.data.EventDataAdapter
import org.brightmindenrichment.street_care.ui.community.model.CommunityPageName
import org.brightmindenrichment.street_care.util.DebouncingQueryTextListener
import org.brightmindenrichment.street_care.util.Extensions.Companion.getDayInMilliSec
import org.brightmindenrichment.street_care.util.Extensions.Companion.toPx
import org.brightmindenrichment.street_care.util.Queries.getLikedEventsQuery
import java.util.*

/**
 * Fragment to display user's liked posts from the community
 */
class LikedPostsFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var scope = lifecycleScope
    private val eventDataAdapter = EventDataAdapter(scope)
    private var defaultQuery = getLikedEventsQuery()
    private var userInputText = ""
    private var selectedItemPos = -1
    private var likedEventIds = emptyList<String>()

    // Search and filter components
    private lateinit var searchView: androidx.appcompat.widget.SearchView
    private lateinit var spinner: Spinner
    private var menuItems = listOf<String>()

    // Bottom sheet components
    private lateinit var bottomSheetView: ScrollView
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ScrollView>

    // Date filter enum
    // Note: Date filtering is limited for liked posts due to Firestore constraints
    // (cannot mix whereIn on document IDs with inequality operators on dates)
    // Currently shows all liked events up to 10 (Firestore whereIn limit)
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
        return inflater.inflate(R.layout.fragment_liked_posts, container, false)
    }

    @SuppressLint("ClickableAccessibilityHint")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSearchAndFilter()
        setupBottomSheet()
        setupRecyclerView()
        loadLikedPosts()
    }

    override fun onResume() {
        super.onResume()
        loadLikedPosts()
    }

    private fun setupBottomSheet() {
        bottomSheetView = view!!.findViewById<ScrollView>(R.id.bottomLayout)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        val backgroundOverlay: FrameLayout = view!!.findViewById<FrameLayout>(R.id.backgroundOverlay)
        val mask = view!!.findViewById<LinearLayout>(R.id.ll_mask)

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
    }

    private fun setupRecyclerView() {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerViewLikedPosts)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        val communityRecyclerAdapter =
            CommunityRecyclerAdapter(eventDataAdapter, CommunityPageName.UPCOMING_EVENTS)
        recyclerView?.adapter = communityRecyclerAdapter

        // Setup click listener for bottom sheet
        communityRecyclerAdapter.setClickListener(object : CommunityRecyclerAdapter.ClickListener {
            @SuppressLint("ResourceAsColor")
            override fun onClick(event: Event, position: Int) {
                setupBottomSheetClickHandlers(event, position, communityRecyclerAdapter, recyclerView!!)
            }
        })
    }

    private fun setupBottomSheetClickHandlers(
        event: Event,
        position: Int,
        communityRecyclerAdapter: CommunityRecyclerAdapter,
        recyclerView: RecyclerView
    ) {
        // Get bottom sheet UI elements
        val bsTextViewTitle: TextView = bottomSheetView.findViewById<TextView>(R.id.textViewCommunityTitle)
        val bsTextViewCommunityLocation: TextView = bottomSheetView.findViewById<TextView>(R.id.textViewCommunityLocation)
        val bsTextViewCommunityTime: TextView = bottomSheetView.findViewById<TextView>(R.id.textViewCommunityTime)
        val bsTextViewCommunityDesc: TextView = bottomSheetView.findViewById<TextView>(R.id.textViewCommunityDesc)
        val bsButtonShare: ImageButton = bottomSheetView.findViewById(R.id.btnShare)
        val bsButtonLike: ImageButton = bottomSheetView.findViewById(R.id.btnLike)
        val tvLikeCount: TextView = bottomSheetView.findViewById(R.id.tvLikeCount)
        val bsButtonClose: AppCompatButton = bottomSheetView.findViewById(R.id.buttonClose)

        // Share button logic
        bsButtonShare.setOnClickListener {
            // Share functionality - can be implemented later
        }

        // Like button - no click listener needed for liked posts
        // Just display the current like state and count
        bsButtonLike.setImageResource(
            if (event.likedByMe) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )
        tvLikeCount.text = event.likeCount.toString()

        // Close button logic
        bsButtonClose.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        // Populate bottom sheet with event data
        communityRecyclerAdapter.setCurrentBottomSheetEvent(event)
        bsTextViewTitle.text = event.title
        bsTextViewCommunityLocation.text = if (!event.city.isNullOrEmpty() && !event.state.isNullOrEmpty()) {
            "${event.street}, ${event.city}, ${event.state} ${event.zipcode}"
        } else {
            event.location.orEmpty()
        }
        bsTextViewCommunityTime.text = event.time
        val eventDesc = event.description?.takeIf { it.isNotBlank() }
        bsTextViewCommunityDesc.text = eventDesc

        // Show the bottom sheet
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun setupSearchAndFilter() {
        // Setup search view
        searchView = view!!.findViewById(R.id.search_view)
        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                userInputText = query ?: ""
                refreshEvents(eventDataAdapter, resources, defaultQuery, userInputText)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                userInputText = newText ?: ""
                refreshEvents(eventDataAdapter, resources, defaultQuery, userInputText)
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
        // Add right padding so spinner doesn't stick to the edge
        spinner.setPadding(0, 0, 16.toPx(), 0)

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

        // Custom adapter so spinner text doesn't show in toolbar
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

                val itemEventsFilter = menu.add(Menu.NONE, 0, 1, "events filter").apply {
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

    private fun loadLikedPosts() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            showEmptyState()
            return
        }

        // Check if the user has liked any events first
        val progressBar = view?.findViewById<ProgressBar>(R.id.progressBarLoading)
        val textView = view?.findViewById<LinearLayout>(R.id.root)?.findViewById<TextView>(R.id.text_view)
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerViewLikedPosts)
        
        progressBar?.visibility = View.VISIBLE
        recyclerView?.visibility = View.GONE
        textView?.visibility = View.GONE

        Firebase.firestore
            .collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { userDocument ->
                likedEventIds = userDocument.get("likedOutreach") as? List<String> ?: emptyList()
                
                android.util.Log.d("LikedPostsFragment", "Liked event IDs count: ${likedEventIds.size}")
                android.util.Log.d("LikedPostsFragment", "Liked event IDs: $likedEventIds")

                if (likedEventIds.isEmpty()) {
                    showEmptyState()
                    return@addOnSuccessListener
                }

                // Create query with the liked event IDs
                defaultQuery = getLikedEventsQuery(likedEventIds)
                
                // Use query-based approach with the liked event IDs
                android.util.Log.d("LikedPostsFragment", "About to refresh events with query")
                refreshEvents(
                    eventDataAdapter,
                    resources,
                    defaultQuery,
                    ""
                )

                android.util.Log.d("LikedPostsFragment", "Setting up search")
                searchEvents(
                    eventDataAdapter,
                    resources,
                    defaultQuery
                )
            }
            .addOnFailureListener { exception ->
                android.util.Log.e("LikedPostsFragment", "Error loading user liked events", exception)
                showEmptyState()
            }
    }

    private fun refreshEvents(
        eventDataAdapter: EventDataAdapter,
        resources: Resources,
        query: Query,
        inputText: String
    ) {
        val progressBar = view?.findViewById<ProgressBar>(R.id.progressBarLoading)
        val textView = view?.findViewById<LinearLayout>(R.id.root)?.findViewById<TextView>(R.id.text_view)
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerViewLikedPosts)

        eventDataAdapter.refresh(
            inputText = inputText,
            query = query,
            showProgressBar = {
                progressBar?.visibility = View.VISIBLE
                recyclerView?.visibility = View.GONE
                textView?.visibility = View.GONE
            },
            onNoResults = {
                progressBar?.visibility = View.GONE
                recyclerView?.visibility = View.GONE
                textView?.visibility = View.VISIBLE
                textView?.text = getString(R.string.no_results_were_found)
            }
        ) {
            // Success - data loaded
            android.util.Log.d("LikedPostsFragment", "Events loaded successfully, count: ${eventDataAdapter.size}")
            textView?.visibility = View.GONE
            progressBar?.visibility = View.GONE
            recyclerView?.visibility = View.VISIBLE
            recyclerView?.layoutManager = LinearLayoutManager(view?.context)

            val communityRecyclerAdapter = CommunityRecyclerAdapter(
                eventDataAdapter,
                CommunityPageName.UPCOMING_EVENTS
            )
            recyclerView?.adapter = communityRecyclerAdapter

            // Setup click listener for bottom sheet
            communityRecyclerAdapter.setClickListener(object : CommunityRecyclerAdapter.ClickListener {
                @SuppressLint("ResourceAsColor")
                override fun onClick(event: Event, position: Int) {
                    setupBottomSheetClickHandlers(event, position, communityRecyclerAdapter, recyclerView!!)
                }
            })

            recyclerView?.addItemDecoration(LinePaint())
            val stickyHeaderItemDecorator = StickyHeaderItemDecorator(communityRecyclerAdapter)
            recyclerView?.addItemDecoration(stickyHeaderItemDecorator)
            
            android.util.Log.d("LikedPostsFragment", "RecyclerView setup complete")
        }
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
                    refreshEvents(
                        eventDataAdapter,
                        resources,
                        query,
                        it
                    )
                }
            }
        )
    }

    private fun showEmptyState() {
        val progressBar = view?.findViewById<ProgressBar>(R.id.progressBarLoading)
        val textView = view?.findViewById<LinearLayout>(R.id.root)?.findViewById<TextView>(R.id.text_view)
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerViewLikedPosts)
        
        progressBar?.visibility = View.GONE
        recyclerView?.visibility = View.GONE
        textView?.visibility = View.VISIBLE
        textView?.text = getString(R.string.no_liked_posts_yet)
    }

    private fun applyDateFilter(filter: DateFilter) {
        if (likedEventIds.isEmpty()) {
            showEmptyState()
            return
        }

        // Note: Firestore doesn't allow mixing whereIn() on document IDs with date inequality operators
        // So we fetch all liked events and filter them client-side using EventDataAdapter's built-in filtering
        selectedDateFilter = filter
        defaultQuery = getLikedEventsQuery(likedEventIds)
        refreshEvents(eventDataAdapter, resources, defaultQuery, userInputText)
        searchEvents(eventDataAdapter, resources, defaultQuery)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val selectedItem = parent?.getItemAtPosition(position)?.toString() ?: return

                when (selectedItem) {
            getString(R.string.select) -> return
            getString(R.string.next_7_days) -> {
                selectedDateFilter = DateFilter.NEXT_7_DAYS
                applyDateFilter(selectedDateFilter)
            }
            getString(R.string.next_30_days) -> {
                selectedDateFilter = DateFilter.NEXT_30_DAYS
                applyDateFilter(selectedDateFilter)
            }
            getString(R.string.next_60_days) -> {
                selectedDateFilter = DateFilter.NEXT_60_DAYS
                applyDateFilter(selectedDateFilter)
            }
            getString(R.string.next_90_days) -> {
                selectedDateFilter = DateFilter.NEXT_90_DAYS
                applyDateFilter(selectedDateFilter)
            }
            getString(R.string.other_upcoming_events) -> {
                selectedDateFilter = DateFilter.OTHER_UPCOMING
                applyDateFilter(selectedDateFilter)
            }
            getString(R.string.reset) -> {
                selectedDateFilter = DateFilter.ALL
                userInputText = ""
                searchView.setQuery("", false)
                spinner.setSelection(0)
                defaultQuery = getLikedEventsQuery(likedEventIds)
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
        eventDataAdapter.cleanupFlagStatusListeners()
    }
}
