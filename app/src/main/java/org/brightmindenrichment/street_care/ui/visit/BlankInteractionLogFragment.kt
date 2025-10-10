package org.brightmindenrichment.street_care.ui.visit

import android.graphics.Color
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class BlankInteractionLogFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Show dialog after view is created
        Handler(Looper.getMainLooper()).post {
            InteractionLogDialog().show(parentFragmentManager, "InteractionDialog")
        }

        // Return a blank view (e.g., transparent or white)
        return View(requireContext()).apply {
            setBackgroundColor(Color.TRANSPARENT) // or use white
        }
    }
}