package org.brightmindenrichment.street_care.ui.visit.visit_forms

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import org.brightmindenrichment.street_care.databinding.VisitLogListLayoutBinding
import org.brightmindenrichment.street_care.ui.visit.VisitDataAdapter
import org.brightmindenrichment.street_care.ui.visit.data.VisitLog
import java.text.SimpleDateFormat

class VisitLogRecyclerAdapter(val context: Context, private val controller: VisitDataAdapter) :
    RecyclerView.Adapter<VisitLogRecyclerAdapter.ViewHolder>() {
    val sdf = SimpleDateFormat("dd MMM yyyy")

    //private var list: MutableList<VisitLog> = arrayListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            VisitLogListLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val visit = controller.getVisitAtPosition(position)
        if (visit != null) {
            holder.bind(visit)
        }
    }

    override fun getItemCount(): Int {
        return controller.size
    }


    inner class ViewHolder(val binding: VisitLogListLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        //val textView : TextView = visitLogView.findViewById(R.id.item_view_for_visit)
        fun bind(item: VisitLog) {
            binding.itemViewForVisitDate.setText(sdf.format(item.date))
            if(!item.location.equals("null")) {
                binding.itemViewForVisitLocation.text = item.location
            }
            if(!item.comments.equals("null")){
                binding.itemViewForVisitComments.setText(item.comments)
            }

        }

    }
}