package org.brightmindenrichment.street_care.ui.user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.ItemLikedEventBinding
import org.brightmindenrichment.street_care.ui.community.data.Event

class LikedEventsAdapter(
    private var likedEvents: List<Event> = emptyList(),
    private val onEventClick: (Event) -> Unit = {}
) : RecyclerView.Adapter<LikedEventsAdapter.LikedEventViewHolder>() {

    class LikedEventViewHolder(private val binding: ItemLikedEventBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(event: Event, onEventClick: (Event) -> Unit) {
            binding.apply {
                // Safely handle potentially null/empty fields
                textViewEventTitle.text = event.title ?: "No Title"
                textViewEventDescription.text = event.description ?: "No Description"
                textViewEventLocation.text = event.location ?: "No Location"
                textViewEventDate.text = event.date ?: "No Date"
                textViewEventTime.text = event.time ?: "No Time"
                textViewInterestCount.text = event.interest?.toString() ?: "0"
                
                // Set click listener with null safety
                root.setOnClickListener {
                    try {
                        onEventClick(event)
                    } catch (e: Exception) {
                        android.util.Log.e("LikedEventsAdapter", "Error handling click", e)
                    }
                }
                
                // Load event image if available (you might need to add imageUrl to Event class)
                // Picasso.get().load(event.imageUrl).into(imageViewEvent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikedEventViewHolder {
        val binding = ItemLikedEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LikedEventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LikedEventViewHolder, position: Int) {
        holder.bind(likedEvents[position], onEventClick)
    }

    override fun getItemCount(): Int = likedEvents.size

    fun updateEvents(newEvents: List<Event>) {
        likedEvents = newEvents
        notifyDataSetChanged()
    }
}
