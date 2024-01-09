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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.ui.community.adapter.CommunityRecyclerAdapter
import org.brightmindenrichment.street_care.ui.community.data.Event
import org.brightmindenrichment.street_care.ui.community.data.EventDataAdapter
import org.brightmindenrichment.street_care.util.DebouncingQueryTextListener


class CommunityEventFragment : Fragment() {

    lateinit var buttonAdd: ImageButton
    private val eventDataAdapter = EventDataAdapter()
    private val defaultQuery = Firebase.firestore
                                    .collection("events")
                                    .orderBy("date", Query.Direction.DESCENDING)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
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

        val menuHost: MenuHost = requireActivity()
        val searchView: SearchView = view.findViewById(R.id.search_view)

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
                super.onPrepareMenu(menu)
                val item = menu.add("add new")
                item.setIcon(R.drawable.ic_menu_add)
                item.setShowAsAction(1)
            }
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                Log.d("menuItem.isVisible", "menuItem.isVisible"+menuItem.itemId)

                if(menuItem.itemId!=0){
                    requireActivity().onBackPressed()
                }
                else {
                    findNavController().navigate(R.id.nav_add_event)
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
            val bottomSheetView = view.findViewById<LinearLayout>(R.id.bottomLayout)
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            val backgroundOverlay: FrameLayout = view.findViewById<FrameLayout>(R.id.backgroundOverlay)

            bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            backgroundOverlay.visibility = View.VISIBLE
                        }
                        else -> {
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
                view,
                bottomSheetView,
                bottomSheetBehavior,
                this.resources,
                defaultQuery,
                ""
            )

            searchEvents(
                searchView,
                eventDataAdapter,
                view,
                bottomSheetView,
                bottomSheetBehavior,
                this.resources
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
        searchView.setIconifiedByDefault(false);
        searchView.isSubmitButtonEnabled = true;
        searchView.imeOptions = EditorInfo.IME_ACTION_SEARCH;
        searchView.queryHint = "search events";

    }

    private fun searchEvents(
        searchView: SearchView,
        eventDataAdapter: EventDataAdapter,
        view: View?,
        bottomSheetView: LinearLayout,
        bottomSheetBehavior: BottomSheetBehavior<LinearLayout>,
        resources: Resources,
    ) {

        searchView.setOnQueryTextListener(
            DebouncingQueryTextListener(lifecycle) { inputText ->
                inputText?.let {
                    requestQuery(
                        inputText,
                        eventDataAdapter,
                        view,
                        bottomSheetView,
                        bottomSheetBehavior,
                        resources
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
        inputText: String?,
        eventDataAdapter: EventDataAdapter,
        view: View?,
        bottomSheetView: LinearLayout,
        bottomSheetBehavior: BottomSheetBehavior<LinearLayout>,
        resources: Resources
    ) {
        inputText?.let {
//            val query = if(it.isNotEmpty()) Firebase.firestore
//                .collection("events")
//                .where(Filter.or(
//                    Filter.equalTo("description", it),
//                    Filter.equalTo("location", it),
//                    Filter.equalTo("title", it),
//                ))
//                .orderBy("date", Query.Direction.DESCENDING)
//            else defaultQuery

            val query = defaultQuery

            refreshEvents(
                eventDataAdapter = eventDataAdapter,
                view = view,
                bottomSheetView = bottomSheetView,
                bottomSheetBehavior = bottomSheetBehavior,
                resources = resources,
                query = query,
                it
            )
        }

    }


    private fun refreshEvents(
        eventDataAdapter: EventDataAdapter,
        view: View?,
        bottomSheetView: LinearLayout,
        bottomSheetBehavior: BottomSheetBehavior<LinearLayout>,
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
            val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerCommunity)
            recyclerView?.visibility = View.VISIBLE
            recyclerView?.layoutManager = LinearLayoutManager(view?.context)
            recyclerView?.adapter = CommunityRecyclerAdapter(eventDataAdapter)
            val textViewTitle: TextView = bottomSheetView.findViewById<TextView>(R.id.textViewCommunityTitle)
            val textViewCommunityLocation: TextView =bottomSheetView.findViewById<TextView>(R.id.textViewCommunityLocation)
            val textViewCommunityTime: TextView =bottomSheetView.findViewById<TextView>(R.id.textViewCommunityTime)
            val textViewCommunityDesc: TextView =bottomSheetView.findViewById<TextView>(R.id.textViewCommunityDesc)
            val relativeLayoutImage: RelativeLayout = bottomSheetView.findViewById<RelativeLayout>(R.id.relativeLayoutImage)
            val imageViewUnFav: ImageView = bottomSheetView.findViewById<ImageView>(R.id.imageViewUnFav)
            val textInterested:TextView = bottomSheetView.findViewById<TextView>(R.id.textInterested)
            val buttonInterested: AppCompatButton = bottomSheetView.findViewById<AppCompatButton>(R.id.buttonInterested)
            val buttonClose: AppCompatButton = bottomSheetView.findViewById<AppCompatButton>(R.id.buttonClose)

            (recyclerView?.adapter as CommunityRecyclerAdapter).setRefreshBottomSheet { event ->
                refreshBottomSheet(
                    event = event,
                    relativeLayoutImage = relativeLayoutImage,
                    textInterested = textInterested,
                    imageViewUnFav = imageViewUnFav,
                    buttonInterested = buttonInterested
                )
            }

            (recyclerView?.adapter as CommunityRecyclerAdapter).setClickListener(object :
                CommunityRecyclerAdapter.ClickListener {
                @SuppressLint("ResourceAsColor")
                override fun onClick(event: Event, position: Int) {
                    textViewTitle.text = event.title
                    textViewCommunityLocation.text = event.location
                    textViewCommunityTime.text = event.time
                    textViewCommunityDesc.text = event.description

                    var isFavorite = event.liked
                    //val numOfInterest = event.interest?.minus(event.itemList.size)
                    if (isFavorite) {
                        imageViewUnFav.setImageResource(R.drawable.ic_favorite)
                        buttonInterested.backgroundTintList = null
                        buttonInterested.setText(R.string.not_interested)
                        buttonInterested.setTextColor(Color.BLACK)
                    } else {
                        imageViewUnFav.setImageResource(R.drawable.ic_unfav)
                    }
                    Log.d("query", "event.interest: ${event.interest}")
                    Log.d("query", "event.itemList.size: ${event.itemList.size}")

                    refreshBottomSheet(event, relativeLayoutImage, textInterested, imageViewUnFav, buttonInterested)

                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                    buttonInterested.setOnClickListener {
                        imageViewUnFav.performClick()
                    }

                    imageViewUnFav.setOnClickListener {
                        isFavorite = event.liked
                        event.liked=!event.liked
                        if(isFavorite){
                            imageViewUnFav.setImageResource(R.drawable.ic_unfav)
                            buttonInterested.text = resources.getString(R.string.interested)
                            buttonInterested.backgroundTintList = ColorStateList.valueOf(
                                resources.getColor(R.color.dark_green, null)
                            )
                            val color = resources.getColor(R.color.accent_yellow, null)
                            buttonInterested.setTextColor(color)

                        }
                        else{
                            imageViewUnFav.setImageResource(R.drawable.ic_favorite)
                            buttonInterested.text = resources.getString(R.string.not_interested)
                            buttonInterested.backgroundTintList = null
                            buttonInterested.setTextColor(Color.BLACK)
                            Log.d("interestedBtn", "${buttonInterested.text}, ${buttonInterested.backgroundTintList}, ${buttonInterested.currentTextColor}")
                        }
                        //(recyclerView?.adapter as CommunityRecyclerAdapter).notifyDataSetChanged()

                        eventDataAdapter.setLikedEvent(event){ event ->
                            refreshBottomSheet(event, relativeLayoutImage, textInterested, imageViewUnFav, buttonInterested)
                            (recyclerView?.adapter as CommunityRecyclerAdapter).notifyItemChanged(position)
                            Log.d("Liked Event Firebase Update", "Liked Event Firebase Update Success")
                        }
                    }

                    buttonClose.setOnClickListener{
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }
            })

            recyclerView!!.addItemDecoration(LinePaint())
        }

    }

    private fun refreshBottomSheet(
        event: Event,
        relativeLayoutImage: RelativeLayout,
        textInterested: TextView,
        imageViewUnFav: ImageView,
        buttonInterested: AppCompatButton
    ) {
        if(event.liked){
            imageViewUnFav.setImageResource(R.drawable.ic_favorite)
            buttonInterested.text = resources.getString(R.string.not_interested)
            buttonInterested.backgroundTintList = null
            buttonInterested.setTextColor(Color.BLACK)
            Log.d("interestedBtn", "${buttonInterested.text}, ${buttonInterested.backgroundTintList}, ${buttonInterested.currentTextColor}")

        }
        else{
            imageViewUnFav.setImageResource(R.drawable.ic_unfav)
            buttonInterested.text = resources.getString(R.string.interested)
            buttonInterested.backgroundTintList = ColorStateList.valueOf(
                resources.getColor(R.color.dark_green, null)
            )
            val color = resources.getColor(R.color.accent_yellow, null)
            buttonInterested.setTextColor(color)

        }

        val numOfInterest = if(event.itemList.size > 3)
            event.itemList.size.minus(3)
        else 0

        if(numOfInterest>0)
            textInterested.text = "+"+numOfInterest.toString()+" "+resources.getString(R.string.plural_interested)
        else{
            when (event.itemList.size) {
                0 -> {
                    textInterested.text = resources.getString(R.string.first_one_to_join)
                }
                1 -> {
                    textInterested.text = resources.getString(R.string.singular_interested)
                }
                else -> {
                    textInterested.text = resources.getString(R.string.plural_interested)
                }
            }
        }

        relativeLayoutImage.removeAllViews()
        if(event.itemList.isNotEmpty()){
            for (i in event.itemList.indices){
                if(i >= 3) break
                val imageView = CircleImageView(relativeLayoutImage.context)
                imageView.layoutParams = RelativeLayout.LayoutParams(80, 80)
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                val layoutParams = imageView.layoutParams as RelativeLayout.LayoutParams
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START)
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                layoutParams.marginStart = i * 40 // Adjust the spacing between images
                imageView.borderWidth = 2 // Set border width
                imageView.borderColor = Color.BLACK
                Picasso.get().load(event.itemList[i]).error(R.drawable.ic_profile).into(imageView)
                imageView.setCircleBackgroundColorResource(R.color.white)
                relativeLayoutImage.addView(imageView)
            }
        }
        else{
            val imageView = CircleImageView(relativeLayoutImage.context)
            imageView.layoutParams = RelativeLayout.LayoutParams(80, 80)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            val layoutParams = imageView.layoutParams as RelativeLayout.LayoutParams
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
            imageView.setImageResource(R.drawable.ic_profile)
            imageView.setBackgroundResource(R.drawable.dashed_border)
            relativeLayoutImage.addView(imageView)
        }
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




}// end class



