package org.brightmindenrichment.street_care.ui.community

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.brightmindenrichment.street_care.ui.community.model.CommunityActivityObject
import org.brightmindenrichment.street_care.databinding.CommunityActivityItemBinding

class CommunityActivityAdapter() : RecyclerView.Adapter<CommunityActivityAdapter.ViewHolder>() {
    private lateinit var activityList: List<CommunityActivityObject>
    inner class ViewHolder(private val binding: CommunityActivityItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(activity: CommunityActivityObject) {
            binding.activityDescription.text = activity.location+activity.description
            binding.activityTime.text = activity.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CommunityActivityItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = activityList[position]
        holder.bind(activity)
    }
    fun submitList(newDataList: List<CommunityActivityObject>) {
        activityList = newDataList
        this.notifyDataSetChanged()
    }

    override fun getItemCount() = activityList.size
}