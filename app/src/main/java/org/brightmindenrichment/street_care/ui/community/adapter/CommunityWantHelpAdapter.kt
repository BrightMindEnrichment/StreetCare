package org.brightmindenrichment.street_care.ui.community.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.brightmindenrichment.street_care.databinding.CommunityWantHelpItemBinding
import org.brightmindenrichment.street_care.ui.community.model.CommunityActivityHelp
import org.brightmindenrichment.street_care.ui.community.model.CommunityActivityObject
import org.brightmindenrichment.street_care.ui.community.model.CommunityActivityRequest

class CommunityWantHelpAdapter() : RecyclerView.Adapter<CommunityWantHelpAdapter.ViewHolder>() {
    private lateinit var helpList: List<CommunityActivityHelp>
    inner class ViewHolder(private val binding: CommunityWantHelpItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(activity: CommunityActivityHelp) {
            binding.title.text = activity.title
            binding.description.text = activity.description
            binding.timelog.text = activity.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CommunityWantHelpItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = helpList[position]
        holder.bind(activity)
    }

    override fun getItemCount() = helpList.size
}