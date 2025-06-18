package org.brightmindenrichment.street_care.ui.community

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.brightmindenrichment.street_care.R

class InteractionPublic : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_interaction_public, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() = InteractionPublic()
    }
}