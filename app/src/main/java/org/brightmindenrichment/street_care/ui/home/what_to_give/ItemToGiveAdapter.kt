package org.brightmindenrichment.street_care.ui.home.what_to_give

import android.R.layout
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.ui.home.data.ItemsToGive


class ItemToGiveAdapter(private val context: Context, private val dataset :List<ItemsToGive>) : RecyclerView.Adapter<ItemToGiveAdapter.ItemsViewHolder>(){

     class ItemsViewHolder(val view: View) : RecyclerView.ViewHolder(view){
        val imageView : ImageView = view.findViewById(R.id.image_view_what_to_give)
         val textView : TextView = view.findViewById(R.id.item_title_what_to_give)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsViewHolder {
        val itemLayout = LayoutInflater.from(parent.context).inflate(R.layout.item_to_give_list,parent,false)
        return ItemsViewHolder(itemLayout)
    }

    override fun onBindViewHolder(holder: ItemsViewHolder, position: Int) {
        val item = dataset[position]
        holder.textView.text = context.resources.getString(item.stringResourceId)
        holder.imageView.setImageResource(item.imageResourceId)
        holder.itemView.setOnClickListener{
           val builder= AlertDialog.Builder(context).setMessage(item.stringResourceDetailsId).setIcon(item.imageResourceId).
            setPositiveButton("OK", DialogInterface.OnClickListener{ dialog, id ->
                dialog.cancel()
            })
            val alert = builder.create()
                alert.setTitle(item.stringResourceId)
            alert.show()
            alert.getWindow()?.setLayout(1000, 1200);

        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

}