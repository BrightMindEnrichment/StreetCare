package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.content.Context
import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.brightmindenrichment.street_care.databinding.VisitLogListLayoutBinding
import org.brightmindenrichment.street_care.ui.visit.VisitDataAdapter
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class VisitLogRecyclerAdapter(
    private val context: Context,
    private val controller: VisitDataAdapter,
    private val clickListener: DetailsButtonClickListener
) : RecyclerView.Adapter<VisitLogRecyclerAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMMM d',' yyyy | h:mma")

    inner class ViewHolder(val binding: VisitLogListLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: VisitLog, clickListener: DetailsButtonClickListener, position: Int, size: Int) {

            val locationParts = item.whereVisit.split(",").map { it.trim() }

            val cityState = if (locationParts.size >= 3) {
                // Assuming format: Street, City, State, Country
                "${locationParts[0]}, ${locationParts[1]}, ${locationParts[2]}"
            } else {
                item.whereVisit
            }

            binding.textViewDetails.text = if (item.whereVisit.isNotBlank() && item.whereVisit != "null") cityState else "Location"

            // Date + Time formatter
            val dateTimeFormat = SimpleDateFormat("MMM d, yyyy | h:mma", Locale.getDefault())
            dateTimeFormat.timeZone = TimeZone.getDefault() // Optional: set to specific zone if needed

            // Set text
            binding.textViewDate.text = dateTimeFormat.format(item.date)

            // Handle button click
            binding.detailsButton.setOnClickListener {
                clickListener.onClick(item)
            }

            // Timeline marker logic
            when (position) {
                0 -> { // First item
                    binding.timelineLine.visibility = View.GONE
                    binding.timelineLineHalfUp.visibility = View.GONE
                    binding.timelineLineHalfDown.visibility = View.VISIBLE
                }
                size - 1 -> { // Last item
                    binding.timelineLine.visibility = View.GONE
                    binding.timelineLineHalfUp.visibility = View.VISIBLE
                    binding.timelineLineHalfDown.visibility = View.GONE
                }
                else -> { // Middle items
                    binding.timelineLine.visibility = View.VISIBLE
                    binding.timelineLineHalfUp.visibility = View.GONE
                    binding.timelineLineHalfDown.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = VisitLogListLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val visit = controller.getVisitAtPosition(position)
        if (visit != null) {
            holder.bind(visit, clickListener, position, itemCount)
        }
    }

    override fun getItemCount(): Int = controller.size
}

interface DetailsButtonClickListener {
    fun onClick(visitId: VisitLog)
}
