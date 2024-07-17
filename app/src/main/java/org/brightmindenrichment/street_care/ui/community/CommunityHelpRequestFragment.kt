package org.brightmindenrichment.street_care.ui.community

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.ui.community.adapter.CommunityHelpRequestAdapter
import org.brightmindenrichment.street_care.ui.community.data.HelpRequest
import org.brightmindenrichment.street_care.ui.community.data.HelpRequestDataAdapter
import org.brightmindenrichment.street_care.util.DebouncingQueryTextListener
import org.brightmindenrichment.street_care.util.Extensions.Companion.createSkillTextView
import org.brightmindenrichment.street_care.util.Extensions.Companion.setHelpRequestActionButton
import org.brightmindenrichment.street_care.util.Extensions.Companion.setHelpRequestActionButtonStyle
import org.brightmindenrichment.street_care.util.Queries.getHelpRequestDefaultQuery


class CommunityHelpRequestFragment : Fragment() {

    private var scope = lifecycleScope

    private var userInputText = ""
    //private var isPastEvents = true
    private var defaultQuery = getHelpRequestDefaultQuery()

    //private lateinit var fragmentCommunityEventView: View
    private lateinit var bottomSheetView: ScrollView
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ScrollView>
    private lateinit var searchView: SearchView
    private lateinit var helpRequestDataAdapter: HelpRequestDataAdapter

    override fun onDestroy() {
        super.onDestroy()
        Log.d("syncWebApp", "Community Help Requests Fragment onDestroy...")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("syncWebApp", "Community Help Requests Fragment onCreate...")
        arguments?.let {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Log.d(ContentValues.TAG, "Community onCreateView")
        return inflater.inflate(R.layout.fragment_community_help_requests, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        helpRequestDataAdapter = HelpRequestDataAdapter(
            scope = scope,
            context = this.requireContext(),
            navController = findNavController()
        )

        val menuHost: MenuHost = requireActivity()
        Log.d("notification", "associated activity: $menuHost")
        searchView = view.findViewById(R.id.search_view)

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
                super.onPrepareMenu(menu)

                val itemAddNew = menu.add(Menu.NONE, 0, 0, "add new").apply {
                    setIcon(R.drawable.ic_menu_add)
                    setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                }

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
                        findNavController().popBackStack()
                        findNavController().navigate(R.id.nav_add_help_request, Bundle().apply {

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

        Log.d(ContentValues.TAG, "Community help requests onViewCreated start")
        if (Firebase.auth.currentUser == null) {
            val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
            val textView = view.findViewById<LinearLayout>(R.id.root).findViewById<TextView>(R.id.text_view)
            progressBar?.visibility = View.GONE
            textView.visibility = View.VISIBLE
            textView.text = context?.getString(R.string.events_are_only_available_for_logged_in_users)
            //val layout = view.findViewById<LinearLayout>(R.id.root)
            //val textView = createTextView("Events are only available for logged in Users")
            //layout?.addView(textView)
        }
        else{
            bottomSheetView = view.findViewById(R.id.bottomLayout)
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            val backgroundOverlay: FrameLayout = view.findViewById(R.id.backgroundOverlay)
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
            Log.d("debug", "helpRequests refresh1")
            refreshHelpRequests(
                helpRequestDataAdapter,
                defaultQuery,
                "",
                Firebase.auth.currentUser!!.uid
            )

            searchEvents(
                helpRequestDataAdapter,
                defaultQuery,
                Firebase.auth.currentUser!!.uid
            )

        }

    }

    private fun setUpSearchView(searchView: SearchView) {
        searchView.setIconifiedByDefault(false)
        searchView.isSubmitButtonEnabled = true
        searchView.imeOptions = EditorInfo.IME_ACTION_SEARCH
        searchView.queryHint = context?.getString(R.string.search)

    }

    private fun searchEvents(
        helpRequestDataAdapter: HelpRequestDataAdapter,
        query: Query,
        currentUserId: String
    ) {
        searchView.setOnQueryTextListener(
            DebouncingQueryTextListener(lifecycle) { inputText ->
                inputText?.let {
                    userInputText = it
                    requestQuery(
                        inputText = it,
                        helpRequestDataAdapter = helpRequestDataAdapter,
                        query = query,
                        currentUserId = currentUserId
                    )
                }
            }
        )
    }

    private fun requestQuery(
        inputText: String,
        helpRequestDataAdapter: HelpRequestDataAdapter,
        query: Query,
        currentUserId: String
    ) {
        refreshHelpRequests(
            helpRequestDataAdapter = helpRequestDataAdapter,
            query = query,
            inputText = inputText,
            currentUserId = currentUserId
        )
    }


    private fun refreshHelpRequests(
        helpRequestDataAdapter: HelpRequestDataAdapter,
        query: Query,
        inputText: String,
        currentUserId: String
    ) {
        Log.d("debug", "helpRequests refresh2")
        val progressBar = view?.findViewById<ProgressBar>(R.id.progressBar)
        val textView = view?.findViewById<LinearLayout>(R.id.root)?.findViewById<TextView>(R.id.text_view)
        helpRequestDataAdapter.refresh(
            inputText = inputText,
            query = query,
            showProgressBar = {
                progressBar?.visibility = View.VISIBLE
            },
            onNoResults = {
                progressBar?.visibility = View.GONE
                textView?.visibility = View.VISIBLE
                textView?.text = context?.getString(R.string.no_results_were_found)
            }
        ) {
            // onComplete()
            textView?.visibility = View.GONE
            progressBar?.visibility = View.GONE
            val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerCommunity)!!
            val communityHelpRequestAdapter = CommunityHelpRequestAdapter(
                controller = helpRequestDataAdapter,
                currentUserId = currentUserId,
                context = requireContext()
            )
            recyclerView.visibility = View.VISIBLE
            recyclerView.layoutManager = LinearLayoutManager(view?.context)
            recyclerView.adapter = communityHelpRequestAdapter
            recyclerView.addItemDecoration(LinePaint())


            val bsTextViewTitle: TextView = bottomSheetView.findViewById(R.id.tvHelpRequestTitle)
            val bsTextViewHelpRequestLocation: TextView = bottomSheetView.findViewById(R.id.tvHelpRequestLocation)
            val bsTextViewHelpRequestHowToFind: TextView = bottomSheetView.findViewById(R.id.tvHelpRequestHowToFind)
            val bsTextViewHelpRequestDesc: TextView =bottomSheetView.findViewById(R.id.tvHelpRequestDesc)
            val bsButtonAction: AppCompatButton = bottomSheetView.findViewById(R.id.btnAction)
            val bsButtonClose: AppCompatButton = bottomSheetView.findViewById(R.id.buttonClose)
            val bsLinearLayoutStatus: LinearLayout = bottomSheetView.findViewById(R.id.llStatus)
            val bsTextViewHelpRequestStatus: TextView = bottomSheetView.findViewById(R.id.tvHelpRequestStatus)
            val bsFlexboxLayoutSkills: FlexboxLayout = bottomSheetView.findViewById(R.id.flSkills)
            val bsLinearLayoutButton: LinearLayout = bottomSheetView.findViewById(R.id.llButton)

            (recyclerView?.adapter as CommunityHelpRequestAdapter).setRefreshBottomSheet { helpRequest ->
                refreshBottomSheet(
                    helpRequest = helpRequest,
                    btnAction = bsButtonAction,
                    tvHelpRequestStatus = bsTextViewHelpRequestStatus,
                    llButton = bsLinearLayoutButton,
                    currentUserId = currentUserId,
                    flexboxLayoutSkills = bsFlexboxLayoutSkills,
                )
            }

            (recyclerView.adapter as CommunityHelpRequestAdapter).setClickListener(object :
                CommunityHelpRequestAdapter.ClickListener {
                @SuppressLint("ResourceAsColor")
                override fun onClick(helpRequest: HelpRequest, position: Int) {
                    bsTextViewTitle.text  = helpRequest.title
                    bsTextViewHelpRequestLocation.text = helpRequest.location
                    bsTextViewHelpRequestHowToFind.text = helpRequest.identification
                    bsTextViewHelpRequestDesc.text = helpRequest.description

                    refreshBottomSheet(
                        helpRequest = helpRequest,
                        btnAction = bsButtonAction,
                        tvHelpRequestStatus = bsTextViewHelpRequestStatus,
                        llButton = bsLinearLayoutButton,
                        currentUserId = currentUserId,
                        flexboxLayoutSkills = bsFlexboxLayoutSkills,
                    )

                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                    bsButtonAction.setOnClickListener {

                        helpRequestDataAdapter.setBtnAction(
                            helpRequest = helpRequest,
                            onComplete = { helpRequest ->
                                setHelpRequestActionButton(
                                    helpRequest = helpRequest,
                                    btnAction = bsButtonAction,
                                    tvHelpRequestStatus = bsTextViewHelpRequestStatus,
                                    llButton = bsLinearLayoutButton,
                                    currentUserId = currentUserId,
                                    context = requireContext()
                                )

                                refreshBottomSheet(
                                    helpRequest = helpRequest,
                                    btnAction = bsButtonAction,
                                    tvHelpRequestStatus = bsTextViewHelpRequestStatus,
                                    llButton = bsLinearLayoutButton,
                                    currentUserId = currentUserId,
                                    flexboxLayoutSkills = bsFlexboxLayoutSkills,
                                )
                                (recyclerView.adapter as CommunityHelpRequestAdapter).notifyItemChanged(position)
                                Log.d("Liked Event Firebase Update", "Liked Event Firebase Update Success")
                            }
                        )
                    }

                    bsButtonClose.setOnClickListener{
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }
            })
        }

    }

    private fun refreshBottomSheet(
        helpRequest: HelpRequest,
        btnAction: AppCompatButton,
        tvHelpRequestStatus: TextView,
        llButton: LinearLayout,
        currentUserId: String,
        flexboxLayoutSkills: FlexboxLayout,
    ) {
        helpRequest.skills?.let { skills ->
            flexboxLayoutSkills.removeAllViews()
            for(skill in skills) {
                flexboxLayoutSkills.addView(createSkillTextView(skill, requireContext()))
            }
        }
        setHelpRequestActionButtonStyle(
            helpRequest = helpRequest,
            btnAction = btnAction,
            tvHelpRequestStatus = tvHelpRequestStatus,
            llButton = llButton,
            currentUserId = currentUserId,
            context = requireContext(),
            textColor = Color.BLACK,
            backgroundColor = null

        )
    }
}