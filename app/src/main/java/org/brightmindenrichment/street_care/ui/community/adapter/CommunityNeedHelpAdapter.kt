package org.brightmindenrichment.street_care.ui.community.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.brightmindenrichment.street_care.databinding.BottomSheetCommunityHelpBinding
import org.brightmindenrichment.street_care.databinding.CommunityNeedHelpItemBinding
import org.brightmindenrichment.street_care.ui.community.model.CommunityActivityHelp
import org.brightmindenrichment.street_care.ui.community.model.CommunityActivityRequest

private const val TAG = "CommunityNeedHelpAdapter"

class CommunityNeedHelpAdapter(
    private val onItemClicked: (CommunityActivityRequest) -> Unit,
    private val bottomSheet: View
) : RecyclerView.Adapter<CommunityNeedHelpAdapter.ViewHolder>() {
    private var requestList = emptyList<CommunityActivityRequest>()

    inner class ViewHolder(
        private val binding: CommunityNeedHelpItemBinding,
        private val bottomSheetBinding: BottomSheetCommunityHelpBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(activity: CommunityActivityRequest) {
            //TODO: Add user
            binding.title.text = activity.title
            binding.description.text = activity.description
            binding.timeLog.text = activity.time
            binding.root.setOnClickListener {
                onItemClicked(activity)
                bottomSheetBinding.titleText.text = activity.title
                bottomSheetBinding.detailDescription.text = activity.description
                //TODO: Need to edit the time text
                bottomSheetBinding.timeLog.text = activity.time

                activity.contact?.let {
                    bottomSheetBinding.contactText.text = it
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CommunityNeedHelpItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        val bottomSheetBinding = BottomSheetCommunityHelpBinding.bind(bottomSheet)

        return ViewHolder(binding, bottomSheetBinding)
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