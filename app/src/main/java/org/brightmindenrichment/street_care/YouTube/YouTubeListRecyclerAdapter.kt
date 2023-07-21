package org.brightmindenrichment.street_care.YouTube

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.brightmindenrichment.street_care.R


class YouTubeListRecyclerAdapter(private val controller: YouTubeController,
                                 private val delegate: YouTubeRecyclerAdapterDelegate) : RecyclerView.Adapter<YouTubeListRecyclerAdapter.ViewHolder>() {

    interface YouTubeRecyclerAdapterDelegate {
        fun onItemClick(position: Int)
    }

    inner class ViewHolder(private val itemView: View, private val delegate: YouTubeRecyclerAdapterDelegate): RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById<ImageView>(R.id.imageViewYouTubeThumbnail)

        init {
            imageView.setOnClickListener {
                delegate.onItemClick(bindingAdapterPosition)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.fragment_youtube_list_item, parent, false)
        return ViewHolder(itemView, delegate)
    }

    override fun getItemCount(): Int {
        return controller.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = controller.itemAtIndex(position)

        if (item != null) {
            Picasso.get().load(item.snippet.thumbnails.standard.url).placeholder(R.drawable.circle_button).into(holder.imageView)
        }
    }
} // end class