package org.brightmindenrichment.street_care.ui.community.adapter

import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.ui.community.data.Event
import org.brightmindenrichment.street_care.ui.community.data.EventDataAdapter
import org.brightmindenrichment.street_care.util.Extensions

class CommunityRecyclerAdapter(private val controller: EventDataAdapter) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface ClickListener {
        fun onClick(event: Event){}
    }
    private var clickListener:ClickListener?=null

    fun setClickListener(clickListener: ClickListener) {
        this.clickListener = clickListener
    }

    inner class EventViewHolder(private val communityItemView: View) : RecyclerView.ViewHolder(communityItemView) {

        private val textViewTitle: TextView = communityItemView.findViewById<TextView>(R.id.textViewCommunityTitle)
        private val textViewCommunityLocation: TextView = communityItemView.findViewById<TextView>(R.id.textViewCommunityLocation)
        private val textViewCommunityTime: TextView = communityItemView.findViewById<TextView>(R.id.textViewCommunityTime)
        private val textViewDate: TextView = communityItemView.findViewById<TextView>(R.id.textViewDate)
        private val textViewDay: TextView = communityItemView.findViewById<TextView>(R.id.textViewDay)
        private val imageViewUnFav: ImageView = communityItemView.findViewById<ImageView>(R.id.imageViewUnFav)
        private val relativeLayoutImage: RelativeLayout = communityItemView.findViewById<RelativeLayout>(R.id.relativeLayoutImage)
        private val textInterested:TextView = communityItemView.findViewById<TextView>(R.id.textInterested)
        private val cardViewEvent:CardView = communityItemView.findViewById<CardView>(R.id.cardViewEvent)
        init {
            imageViewUnFav.setOnClickListener {
                val position = bindingAdapterPosition
                val communityData = controller.getEventAtPosition(position)
                if(communityData!=null){
                    communityData.event?.let{ event->
                        val isFavorite = event.liked
                        event.liked=!event.liked
                        if(isFavorite){
                            imageViewUnFav.setImageResource(R.drawable.ic_unfav)
                        }
                        else{
                            imageViewUnFav.setImageResource(R.drawable.ic_favorite)
                        }
                        notifyDataSetChanged()
                        controller.setLikedEvent(event.eventId!!,event.liked){
                            Log.d("Liked Event Firebase Update", "Liked Event Firebase Update Success")
                        }
                    }

                }
            }
            cardViewEvent.setOnClickListener{
                val position = bindingAdapterPosition
                clickListener?.let {
                    if(position != RecyclerView.NO_POSITION){
                        val communityData = controller.getEventAtPosition(position)
                        communityData?.event?.let{ event ->
                            clickListener!!.onClick(event)
                        }
                    }
                }
            }

        }

        fun bind(pos: Int) {
            // Bind data to views
            // ...
            val communityData = controller.getEventAtPosition(pos)
            communityData?.event?.let{ event->
                textViewTitle.text = event.title
                textViewCommunityLocation.text = event.location.orEmpty()
                textViewCommunityTime.text = event.time.orEmpty()
                textViewDate.text = event.date.orEmpty()
                textViewDay.text = event.day.orEmpty()

                Log.d("query", "event.interest: ${event.interest}")
                Log.d("query", "event.itemList.size: ${event.itemList.size}")

                val isFavorite = event.liked
                val numOfInterest = event.interest?.minus(event.itemList.size)
                if (isFavorite) {
                    imageViewUnFav.setImageResource(R.drawable.ic_favorite)
                } else {
                    imageViewUnFav.setImageResource(R.drawable.ic_unfav)
                }


                if (numOfInterest != null) {
                    if(numOfInterest>0)
                        textInterested.text = "+"+numOfInterest.toString()+" "+communityItemView.context.getString(R.string.plural_interested)
                    else{
                        when (event.itemList.size) {
                            0 -> {
                                textInterested.text = communityItemView.context.getString(R.string.first_one_to_join)
                            }
                            1 -> {
                                textInterested.text = communityItemView.context.getString(R.string.singular_interested)
                            }
                            else -> {
                                textInterested.text = communityItemView.context.getString(R.string.plural_interested)
                            }
                        }
                    }

                }

                when(event.layoutType){
                    Extensions.TYPE_DAY ->{
                        textViewDate.visibility = View.INVISIBLE
                        textViewDay.visibility = View.INVISIBLE
                    }

                }

                relativeLayoutImage.removeAllViews()
                if(event.itemList.size>0){
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
        }

    }

    inner class YearViewHolder (yearItemView: View) : RecyclerView.ViewHolder(yearItemView){
        private val textViewYear: TextView = yearItemView.findViewById<TextView>(R.id.textViewCommunityYear)

        fun bind(pos: Int) {
            // Bind data to views
            // ...
            val communityData = controller.getEventAtPosition(pos)
            communityData?.eventYear?.let{ eventYear->
                textViewYear.text = eventYear.year
            }
        }

    }





    override  fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : RecyclerView.ViewHolder {

        return if(viewType==Extensions.TYPE_MONTH){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.card_community_year, parent, false)
            YearViewHolder(view)
        } else{
            val view = LayoutInflater.from(parent.context).inflate(R.layout.event_list_layout, parent, false)
            EventViewHolder(view)
        }


    }
    override fun getItemCount(): Int {
        return controller.size
    }

    override fun getItemViewType(position: Int): Int {
        val communityData = controller.getEventAtPosition(position)
        return communityData?.layoutType ?:0
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is EventViewHolder -> {
                holder.bind(position)
            }
            is YearViewHolder -> {
                holder.bind(position)
            }
        }
    }

    }
