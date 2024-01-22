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
import org.brightmindenrichment.street_care.ui.community.StickyHeaderInterface
import org.brightmindenrichment.street_care.ui.community.data.CommunityData
import org.brightmindenrichment.street_care.ui.community.data.Event
import org.brightmindenrichment.street_care.ui.community.data.EventDataAdapter
import org.brightmindenrichment.street_care.util.Extensions




class CommunityRecyclerAdapter(private val controller: EventDataAdapter) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), StickyHeaderInterface {

    interface ClickListener {
        fun onClick(event: Event, position: Int){}
    }
    private var clickListener:ClickListener?=null

    fun setClickListener(clickListener: ClickListener) {
        this.clickListener = clickListener
    }

    private lateinit var refreshBottomSheet: (Event) -> Unit
    fun setRefreshBottomSheet(
        refreshBottomSheet: (Event) -> Unit
    ){
        this.refreshBottomSheet = refreshBottomSheet
    }


    fun getItemPosition(eventId: String?): Int? {
        for (pos in 0 until controller.size) {
            val communityData = controller.getEventAtPosition(pos)
            if(communityData?.event?.eventId == eventId) return pos
        }
        return null
    }

    fun getItemAtPosition(pos: Int): CommunityData? {
        return controller.getEventAtPosition(pos)
    }

    fun clickItem(event: Event, pos: Int) {
        clickListener!!.onClick(event, pos)
    }

    private var currentHeaderText: String? = null

    fun getCurrentHeaderText(): String? {
        return currentHeaderText
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
            cardViewEvent.setOnClickListener{
                val position = bindingAdapterPosition
                clickListener?.let {
                    if(position != RecyclerView.NO_POSITION){
                        val communityData = controller.getEventAtPosition(position)
                        communityData?.event?.let{ event ->
                            clickListener!!.onClick(event, position)
                        }
                    }
                }
            }

            imageViewUnFav.setOnClickListener {

                val position = bindingAdapterPosition
                val communityData = controller.getEventAtPosition(position)
                if(communityData!=null){
                    communityData.event?.let{ event->
                        //val isFavorite = event.liked
                        event.liked=!event.liked

//                        if(isFavorite){
//                            imageViewUnFav.setImageResource(R.drawable.ic_unfav)
//                            //event.interest = event.interest?.minus(1)
//
//                        }
//                        else{
//                            imageViewUnFav.setImageResource(R.drawable.ic_favorite)
//                            //event.interest = event.interest?.plus(1)
//                        }

                        // refreshNumOfInterestAndProfileImg(event)
                        // notifyDataSetChanged()
                        controller.setLikedEvent(event) {
                            refreshBottomSheet(it)
                            notifyItemChanged(position)
                            Log.d("Liked Event Firebase Update", "Liked Event Firebase Update Success")
                        }
                    }

                }
            }

        }

        private fun refreshNumOfInterestAndProfileImg(event: Event) {
            val numOfInterest = if(event.itemList.size > 3)
                event.itemList.size.minus(3)
            else 0

            if(numOfInterest > 0)
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

            relativeLayoutImage.removeAllViews()
            if(event.itemList.size > 0){
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
                // numOfInterest = event.interest?.minus(event.itemList.size)
                if (isFavorite) {
                    imageViewUnFav.setImageResource(R.drawable.ic_favorite)
                } else {
                    imageViewUnFav.setImageResource(R.drawable.ic_unfav)
                }

                when(event.layoutType){
                    Extensions.TYPE_DAY ->{
                        textViewDate.visibility = View.INVISIBLE
                        textViewDay.visibility = View.INVISIBLE
                    }

                }

                refreshNumOfInterestAndProfileImg(event)
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
                currentHeaderText = eventYear.year
            }
            //Log.d("stickyHeader", "pos: $pos, textViewYear: ${textViewYear.text}")
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

    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        var headerPosition = 0
        var theItemPosition = itemPosition
        do {
            if (isHeader(theItemPosition)) {
                headerPosition = theItemPosition
                break
            }
            theItemPosition -= 1
        } while (theItemPosition >= 0)
        return headerPosition
    }

    override fun getHeaderLayout(headerPosition: Int): Int {
        return R.layout.card_community_year
    }

    override fun bindHeaderData(header: View?, headerPosition: Int) {
        header?.let{
            it.findViewById<TextView>(R.id.textViewCommunityYear).text = controller.getEventAtPosition(headerPosition)?.eventYear?.year
            it.findViewById<TextView>(R.id.textViewCommunityYear).setBackgroundColor(Color.WHITE)
        }
    }

    override fun isHeader(itemPosition: Int): Boolean {
        return controller.getEventAtPosition(itemPosition)?.layoutType == Extensions.TYPE_MONTH
    }

}
