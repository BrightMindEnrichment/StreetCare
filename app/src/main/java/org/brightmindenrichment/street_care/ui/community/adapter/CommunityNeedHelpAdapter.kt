package org.brightmindenrichment.street_care.ui.community.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.brightmindenrichment.street_care.databinding.CommunityNeedHelpItemBinding
import org.brightmindenrichment.street_care.ui.community.model.CommunityActivityHelp
import org.brightmindenrichment.street_care.ui.community.model.CommunityActivityRequest

class CommunityNeedHelpAdapter (private val onItemClicked: (CommunityActivityRequest) -> Unit)
    : RecyclerView.Adapter<CommunityNeedHelpAdapter.ViewHolder>() {
    private lateinit var requestList: List<CommunityActivityRequest>
    inner class ViewHolder(private val binding: CommunityNeedHelpItemBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(activity: CommunityActivityRequest) {
            //TODO: Add user
            binding.title.text = activity.title
            binding.description.text = activity.description
            binding.timeLog.text = activity.time
            binding.root.setOnClickListener {
                onItemClicked(activity)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CommunityNeedHelpItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = requestList[position]
        holder.bind(activity)
    }
    fun submitList(newDataList: List<CommunityActivityRequest>) {
        requestList = newDataList
        this.notifyDataSetChanged()
    }
    override fun getItemCount() = requestList.size
}