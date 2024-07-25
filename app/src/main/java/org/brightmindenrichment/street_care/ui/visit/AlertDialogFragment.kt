package org.brightmindenrichment.street_care.ui.visit

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import org.brightmindenrichment.street_care.R

class AlertDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity(), R.style.TransparentDialogTheme)
            .setTitle("Warning!")
            .setMessage("This will permanently delete your Visit Log. This can't be undone.")
            .setPositiveButton("Delete") { dialog, which ->
                // Handle delete action
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }

        return builder.create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.setBackgroundDrawableResource(R.drawable.blur_overlay)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
