package org.brightmindenrichment.street_care.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.smarteist.autoimageslider.SliderViewAdapter
import org.brightmindenrichment.street_care.R

class SliderAdapter(imageArray: Array<Int>) :
    SliderViewAdapter<SliderAdapter.SliderViewHolder>() {
    private val _imageArray = imageArray

  class SliderViewHolder(itemView: View?) : SliderViewAdapter.ViewHolder(itemView) {
        var imageView: ImageView = itemView!!.findViewById(R.id.image_view)

    }

    override fun getCount(): Int {
        return _imageArray.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?): SliderViewHolder {
        val view: View = LayoutInflater.from((parent!!.context))
            .inflate(R.layout.fragment_home_images_slider, null)
        return SliderViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: SliderViewHolder?, position: Int) {
        viewHolder!!.imageView.setImageResource(_imageArray[position])
    }


}