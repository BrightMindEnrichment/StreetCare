package org.brightmindenrichment.street_care.ui.community

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.ui.community.adapter.CommunityRecyclerAdapter
import org.brightmindenrichment.street_care.ui.community.data.Event
import org.brightmindenrichment.street_care.ui.community.data.EventDataAdapter


class CommunityFragment : Fragment() {

    lateinit var buttonAdd: ImageButton
    private val eventDataAdapter = EventDataAdapter()

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
        Log.d(ContentValues.TAG, "Community oncreateview")

        return inflater.inflate(R.layout.fragment_community, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
                super.onPrepareMenu(menu)
                val item = menu.add("+ Add New")
                item.setShowAsAction(1)
            }
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                //menuInflater.inflate(R.menu.main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                Log.d("menuItem.isVisible", "menuItem.isVisible")
                if (menuItem.title.equals("+ Add New")){
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

        Log.d(ContentValues.TAG, "Community onViewCreated start")
        if (Firebase.auth.currentUser == null) {


            val layout = view.findViewById<LinearLayout>(R.id.root)
            val image= ImageView(context)
            val textView = TextView(context)
            //setting height and width
            image.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            val imgResId = R.drawable.ic_community_login
            var resId = imgResId
            image.setImageResource(resId)
           /* textView.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            textView.text = "Log in to connect with your local community"
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            textView.setTextColor(Color.GRAY)
            val font = Typeface.SERIF
            // Setting the TextView typeface
            textView.typeface = font
            textView.setPadding(20, 20, 20, 20)
            textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            textView.gravity = Gravity.CENTER
            textView.isAllCaps=false

            layout?.addView(textView)*/
            layout?.addView(image)
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
          eventDataAdapter.refresh {
                val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerCommunity)
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
              (recyclerView?.adapter as CommunityRecyclerAdapter).setClickListener(object :
                  CommunityRecyclerAdapter.ClickListener {
                  @SuppressLint("ResourceAsColor")
                  override fun onClick(event: Event) {
                      textViewTitle.text = event.title
                      textViewCommunityLocation.text = event.location
                      textViewCommunityTime.text = event.time
                      textViewCommunityDesc.text = event.description

                      var isFavorite = event.liked
                      val numOfInterest = event.interest?.minus(event.itemList.size)
                      if (isFavorite) {
                          imageViewUnFav.setImageResource(R.drawable.ic_favorite)
                          buttonInterested.backgroundTintList = null
                          buttonInterested.setText(R.string.not_interested)
                          buttonInterested.setTextColor(Color.BLACK)
                      } else {
                          imageViewUnFav.setImageResource(R.drawable.ic_unfav)
                      }
                      if (numOfInterest != null) {
                          if(numOfInterest>0)
                              textInterested.text = "+"+numOfInterest.toString()+" "+getString(R.string.plural_interested)
                          else{
                              when (event.itemList.size) {
                                  0 -> {
                                      textInterested.text = getString(R.string.first_one_to_join)
                                  }
                                  1 -> {
                                      textInterested.text = getString(R.string.singular_interested)
                                  }
                                  else -> {
                                      textInterested.text = getString(R.string.plural_interested)
                                  }
                              }
                          }

                      }

                      relativeLayoutImage.removeAllViews()
                      if(event.itemList!=null){
                          for (i in event.itemList.indices){
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

                      bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                      buttonInterested.setOnClickListener {
                          imageViewUnFav.performClick()
                      }

                      imageViewUnFav.setOnClickListener {
                          isFavorite = event.liked
                          event.liked=!event.liked
                          if(isFavorite){
                              imageViewUnFav.setImageResource(R.drawable.ic_unfav)
                              buttonInterested.text = getString(R.string.interested)
                              buttonInterested.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(
                                  context!!, R.color.dark_green))
                              val color = ContextCompat.getColor(context!!, R.color.accent1)
                              buttonInterested.setTextColor(color)
                          }
                          else{
                              imageViewUnFav.setImageResource(R.drawable.ic_favorite)
                              buttonInterested.text = getString(R.string.not_interested)
                              buttonInterested.backgroundTintList = null
                              buttonInterested.setTextColor(Color.BLACK)
                          }
                          (recyclerView?.adapter as CommunityRecyclerAdapter).notifyDataSetChanged()
                          eventDataAdapter.setLikedEvent(event.eventId!!,event.liked){
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
    }


    override fun onResume() {
        super.onResume()
        Log.d("BME", "onResume")
        /*val toolbar = activity?.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        if (toolbar == null) {
            Log.d("BME", "Did not find toolbar")
        } else {
            buttonAdd = ImageButton(this.context)
            buttonAdd.setBackgroundResource(R.drawable.ic_menu_add)
            val l3 = Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT
            )
            l3.gravity = Gravity.LEFT
            buttonAdd.layoutParams = l3
            toolbar.addView(buttonAdd)
            buttonAdd.setOnClickListener {
                findNavController().navigate(R.id.nav_add_event)
                Log.d("BME", "Add")
                onDetach()
            }
        }*/
    }



    override fun onDetach() {
        super.onDetach()
        /*val toolbar = activity?.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        if (toolbar == null) {
            Log.d("BME", "Did not find toolbar")
        } else {
             buttonAdd.visibility=View.GONE
        }*/
    }
}// end class