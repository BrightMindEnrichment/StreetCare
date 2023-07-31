package org.brightmindenrichment.street_care.ui.community.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.brightmindenrichment.street_care.databinding.CommunityMyPostItemBinding
import org.brightmindenrichment.street_care.ui.community.model.CommunityActivityHelp
import org.brightmindenrichment.street_care.ui.community.viewModel.CommunityMyHelpViewModel

class CommunityMyHelpAdapter(private val viewModel: CommunityMyHelpViewModel)
    : RecyclerView.Adapter<CommunityMyHelpAdapter.ViewHolder>() {
    private var myHelpList= emptyList<CommunityActivityHelp>()
    class ViewHolder (
        private val binding: CommunityMyPostItemBinding,
        private val viewModel: CommunityMyHelpViewModel)
        : RecyclerView.ViewHolder(binding.root) {
            fun bind(help: CommunityActivityHelp){
                //TODO: Add user
                binding.title.text = help.title
                binding.description.text = help.description
                binding.timeLog.text = help.time
                binding.removeBtn.setOnClickListener {
                    viewModel.deleteItem(help)
                }
            }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = CommunityMyPostItemBinding
            .inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding,viewModel)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(myHelpList[position])

    }

    override fun getItemCount() = myHelpList.size

    fun submitList(newDataList: List<CommunityActivityHelp>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = myHelpList.size

            override fun getNewListSize(): Int = newDataList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return myHelpList[oldItemPosition] == newDataList[newItemPosition]
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return myHelpList[oldItemPosition] == newDataList[newItemPosition]
            }
        })

        myHelpList = newDataList
        diffResult.dispatchUpdatesTo(this)
    }
}