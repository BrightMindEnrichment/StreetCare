package org.brightmindenrichment.street_care.ui.community.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.brightmindenrichment.street_care.databinding.CommunityMyPostItemBinding
import org.brightmindenrichment.street_care.ui.community.model.CommunityActivityRequest
import org.brightmindenrichment.street_care.ui.community.viewModel.CommunityMyRequestViewModel

class CommunityMyRequestAdapter(private val viewModel: CommunityMyRequestViewModel)
    : RecyclerView.Adapter<CommunityMyRequestAdapter.ViewHolder>() {
    private var requestList = emptyList<CommunityActivityRequest>()
    inner class ViewHolder(
        private val binding: CommunityMyPostItemBinding,
        private val viewModel: CommunityMyRequestViewModel
        )
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(activity: CommunityActivityRequest) {
            //TODO: Add user
            binding.title.text = activity.title
            binding.description.text = activity.description
            binding.timeLog.text = activity.time
            binding.removeBtn.setOnClickListener {
                viewModel.deleteItem(activity)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CommunityMyPostItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding,viewModel)
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