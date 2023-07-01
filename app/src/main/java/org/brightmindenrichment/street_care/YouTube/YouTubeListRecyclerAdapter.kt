package org.brightmindenrichment.street_care.YouTube

import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.squareup.picasso.Picasso
import org.brightmindenrichment.street_care.R


class YouTubeListRecyclerAdapter(
    private val controller: YouTubeController,
    private val delegate: YouTubeRecyclerAdapterDelegate
) : RecyclerView.Adapter<YouTubeListRecyclerAdapter.ViewHolder>() {

    interface YouTubeRecyclerAdapterDelegate {
        fun onItemClick(position: Int)

    }
    inner class ViewHolder(
        private val itemView: View,
        private val delegate: YouTubeRecyclerAdapterDelegate
    ) : RecyclerView.ViewHolder(itemView) {
        val imgYouTubeThumbnail: ImageView = itemView.findViewById<ImageView>(R.id.imgYouTubeThumbnail)
        val txtYouTubeTitle: TextView = itemView.findViewById<TextView>(R.id.txtYouTubeTitle)
        val DescArrow:ImageView=itemView.findViewById(R.id.imgYouTubeDescArrow)
        val layoutDesc: LinearLayout = itemView.findViewById<LinearLayout>(R.id.layoutDesc)
        val txtYouTubeDesc: TextView = itemView.findViewById<TextView>(R.id.txtYouTubeDesc)
        val cardView:CardView=itemView.findViewById(R.id.cardView)

        init {
            imgYouTubeThumbnail.setOnClickListener {
                delegate.onItemClick(bindingAdapterPosition)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_youtube_list_item, parent, false)
        return ViewHolder(itemView, delegate)
    }
    override fun getItemCount(): Int {
        return controller.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = controller.itemAtIndex(position)
        if (item != null) {
            Picasso.get().load(item.snippet.thumbnails.standard.url)
                .placeholder(R.drawable.circle_button).into(holder.imgYouTubeThumbnail)
            holder.txtYouTubeTitle.text = item.snippet.title
        }
        holder.DescArrow.setOnClickListener {
            if (holder.layoutDesc.visibility ==View.GONE) {
                TransitionManager.beginDelayedTransition(holder.cardView, AutoTransition())
                holder.layoutDesc.visibility = View.VISIBLE
                if (item != null) {
                    holder.txtYouTubeDesc.text=item.snippet.description
                }
                holder.DescArrow.setImageResource(R.drawable.ic_up_arrow)
            }else {
                TransitionManager.beginDelayedTransition(holder.cardView, AutoTransition())
                holder.layoutDesc.visibility = View.GONE
                holder.DescArrow.setImageResource(R.drawable.ic_down_arrow)
            }
        }
    }
} // end class