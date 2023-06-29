package org.brightmindenrichment.street_care.ui.community

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import org.brightmindenrichment.street_care.databinding.FragmentCommunityWantHelpBinding
import org.brightmindenrichment.street_care.ui.community.viewModel.CommunityWantHelpViewModel

class CommunityWantHelpFragment : Fragment() {

    private var _binding: FragmentCommunityWantHelpBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private lateinit var myTextView: TextView

    companion object {
        fun newInstance() = CommunityWantHelpFragment()
    }

    private lateinit var viewModel: CommunityWantHelpViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommunityWantHelpBinding.inflate(inflater,container,false)
        return binding.root    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[CommunityWantHelpViewModel::class.java]
        // TODO: Use the ViewModel
        setupClickableText()
    }
    private fun setupClickableText(){
        myTextView = binding.requestDescription2

        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                // TODO: Do something when the clickable part of the text is clicked
            }
        }
        val spannableString = SpannableString(myTextView.text.toString())
        spannableString.setSpan(clickableSpan, myTextView.text.indexOf("or"), myTextView.text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        myTextView.text= spannableString
        myTextView.movementMethod = LinkMovementMethod.getInstance();
    }
    private fun startNewHelpPage(){
    }

}