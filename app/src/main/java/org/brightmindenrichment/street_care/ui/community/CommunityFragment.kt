package org.brightmindenrichment.street_care.ui.community

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.ActivityMainBinding.bind
import org.brightmindenrichment.street_care.databinding.ActivityMainBinding.inflate
import org.brightmindenrichment.street_care.databinding.FragmentCommunityBinding
import org.brightmindenrichment.street_care.databinding.FragmentLoginBinding
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
        Log.d(ContentValues.TAG, "Community onViewCreated start")
        if (Firebase.auth.currentUser == null) {
            val layout = view.findViewById<LinearLayout>(R.id.root)
            val textView = TextView(context)
            //setting height and width
            textView.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            textView.text = "Events are only available for logged in Users"
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            textView.setTextColor(Color.GRAY)
            textView.setPadding(20, 20, 20, 20)
            textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            textView.gravity = Gravity.CENTER_VERTICAL
            textView.isAllCaps=false
            layout?.addView(textView)
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
               //val communityRecyclerAdapter = CommunityRecyclerAdapter(eventDataAdapter)
              //recyclerView?.adapter = communityRecyclerAdapter
                recyclerView?.adapter = CommunityRecyclerAdapter(eventDataAdapter)
              (recyclerView?.adapter as CommunityRecyclerAdapter).setClickListener(object :
                  CommunityRecyclerAdapter.ClickListener {
                  override fun onClick(event: Event) {
                      val textViewTitle: TextView = bottomSheetView.findViewById<TextView>(R.id.textViewCommunityTitle)
                      val textViewCommunityLocation: TextView =bottomSheetView.findViewById<TextView>(R.id.textViewCommunityLocation)
                      val textViewCommunityTime: TextView =bottomSheetView.findViewById<TextView>(R.id.textViewCommunityTime)
                      val textViewCommunityDesc: TextView =bottomSheetView.findViewById<TextView>(R.id.textViewCommunityDesc)
                      val relativeLayoutImage: RelativeLayout = bottomSheetView.findViewById<RelativeLayout>(R.id.relativeLayoutImage)
                      val imageViewUnFav: ImageView = bottomSheetView.findViewById<ImageView>(R.id.imageViewUnFav)
                      val isFavorite = event.liked
                      if (isFavorite) {
                          imageViewUnFav.setImageResource(R.drawable.ic_favorite)
                      } else {
                          imageViewUnFav.setImageResource(R.drawable.ic_unfav)
                      }
                      textViewTitle.text = event.title
                      textViewCommunityLocation.text = event.location
                      textViewCommunityTime.text = event.time
                      textViewCommunityDesc.text = event.description
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
                              //layoutParams.topMargin = i * 20 // Adjust the spacing between images
                              imageView.borderWidth = 2 // Set border width
                              imageView.borderColor = Color.BLACK

                              //imageView.setImageResource(R.drawable.ic_profile)
                              Picasso.get().load(event.itemList[i]).error(R.drawable.ic_profile).into(imageView)
                              /*else{
                                  imageView.setImageResource(R.drawable.ic_profile)
                              }*/
                              imageView.setCircleBackgroundColorResource(R.color.white)
                              relativeLayoutImage.addView(imageView)
                              Log.d(ContentValues.TAG, "Image Loaded"+i.toString())
                          }
                      }
                      bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                  }
                  })

              recyclerView!!.addItemDecoration(LinePaint())

            }
        }
    }



    override fun onResume() {
        super.onResume()
        Log.d("BME", "onResume")
        val toolbar = activity?.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
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
        }
    }

    override fun onDetach() {
        super.onDetach()
        val toolbar = activity?.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        if (toolbar == null) {
            Log.d("BME", "Did not find toolbar")
        } else {
             buttonAdd.visibility=View.GONE
        }
    }
}// end class