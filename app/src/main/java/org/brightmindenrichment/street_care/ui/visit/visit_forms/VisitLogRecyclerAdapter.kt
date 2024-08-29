package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.VisitLogListLayoutBinding
import org.brightmindenrichment.street_care.ui.visit.VisitDataAdapter
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog
import java.text.SimpleDateFormat

class VisitLogRecyclerAdapter(
    private val context: Context,
    private val controller: VisitDataAdapter,
    private val clickListener: DetailsButtonClickListener
) : RecyclerView.Adapter<VisitLogRecyclerAdapter.ViewHolder>() {

    private val sdf = SimpleDateFormat("dd MMM yyyy")

    inner class ViewHolder(val binding: VisitLogListLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: VisitLog, clickListener: DetailsButtonClickListener, position: Int, size: Int) {
            binding.textViewCountryName.text = sdf.format(item.date)
            binding.textViewDetails.text = if (item.location != "null") item.location else ""
            binding.detailsButton.setOnClickListener { clickListener.onClick(item) }

            // Timeline setup
            when (position) {
                0 -> {
                    binding.timelineLine.visibility = View.GONE
                    binding.timelineLineHalfDown.visibility = View.VISIBLE
                    binding.timelineLineHalfUp.visibility = View.GONE
                }
                (size - 1) ->{
                    binding.timelineLine.visibility = View.GONE
                    binding.timelineLineHalfDown.visibility = View.GONE
                    binding.timelineLineHalfUp.visibility = View.VISIBLE
                }
                else -> {
                    binding.timelineLine.visibility = View.VISIBLE
                    binding.timelineLineHalfDown.visibility = View.GONE
                    binding.timelineLineHalfUp.visibility = View.GONE
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