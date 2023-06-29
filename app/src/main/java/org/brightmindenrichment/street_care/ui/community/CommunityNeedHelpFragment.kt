package org.brightmindenrichment.street_care.ui.community

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.brightmindenrichment.street_care.databinding.FragmentCommunityNeedHelpBinding
import org.brightmindenrichment.street_care.ui.community.viewModel.CommunityNeedHelpViewModel

class CommunityNeedHelpFragment : Fragment() {

    private var _binding: FragmentCommunityNeedHelpBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private lateinit var myTextView: TextView
    companion object {
        fun newInstance() = CommunityNeedHelpFragment()
    }

    private lateinit var viewModel: CommunityNeedHelpViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommunityNeedHelpBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[CommunityNeedHelpViewModel::class.java]

        // TODO: Use the ViewModel
        setupClickableText()
        setupRecyclerView()
    }
    private fun setupRecyclerView() {
//        val adapter = CommunityActivityAdapter(viewModel.value)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
//        binding.recyclerView.adapter = adapter
    }
    private fun setupClickableText(){
        myTextView = binding.description2

        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                // Do something when the clickable part of the text is clicked
            }
        }
        val spannableString = SpannableString(myTextView.text.toString())
        spannableString.setSpan(clickableSpan, myTextView.text.indexOf("or"), myTextView.text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        myTextView.text= spannableString
        myTextView.movementMethod = LinkMovementMethod.getInstance();
    }

}