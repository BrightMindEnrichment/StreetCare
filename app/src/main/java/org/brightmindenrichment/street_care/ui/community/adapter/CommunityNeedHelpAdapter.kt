package org.brightmindenrichment.street_care.ui.community.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.brightmindenrichment.street_care.databinding.CommunityNeedHelpItemBinding
import org.brightmindenrichment.street_care.ui.community.model.CommunityActivityRequest

class CommunityNeedHelpAdapterAdapter(
    private val activityList: List<CommunityActivityRequest>
) : RecyclerView.Adapter<CommunityNeedHelpAdapterAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: CommunityNeedHelpItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(activity: CommunityActivityRequest) {
            //TODO: Add user
            binding.title.text = activity.title
            binding.description.text = activity.description
            binding.timelog.text = activity.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CommunityNeedHelpItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = activityList[position]
        holder.bind(activity)
    }

    override fun getItemCount() = activityList.size
}