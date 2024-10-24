package org.brightmindenrichment.street_care.ui.community

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentCommunityNeedHelpBinding
import org.brightmindenrichment.street_care.ui.community.adapter.CommunityNeedHelpAdapter
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

        setupClickableText()
        setupReplyHideHint(view)
        setUpBottomSheet(view)

    }

    private fun setupReplyHideHint(view: View) {
        val editText = view.findViewById<EditText>(R.id.reply_input)
        editText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                // EditText is focused, hide the hint
                editText.hint = ""
            } else {
                // EditText is not focused, show the hint
                editText.hint = getString(R.string.your_hint_here)
            }
        }
    }

    private fun setUpBottomSheet(view: View) {
        val bottomSheet: View = view.findViewById<ConstraintLayout>(R.id.bottomLayout)
        val bgView: FrameLayout = view.findViewById(R.id.backgroundOverlay)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        bgView.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                bgView.visibility = View.VISIBLE
                bgView.alpha = slideOffset
            }
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    bgView.visibility = View.GONE
                }
            }
        })
        val adapter = CommunityNeedHelpAdapter({ _ ->
            // Handle item click here
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        },bottomSheet)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        val dividerItemDecoration = DividerItemDecorator(
            ContextCompat.getDrawable(requireContext(), R.drawable.divider)!!
        )
        binding.recyclerView.addItemDecoration(dividerItemDecoration)
        viewModel.requestListLiveData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
        viewModel.loadList()
    }

    private fun setupClickableText(){
        myTextView = binding.description2

        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                findNavController().navigate(R.id.communityAddRequestFragment)
            }
        }
        val spannableString = SpannableString(myTextView.text.toString())
        spannableString.setSpan(
            clickableSpan,
            myTextView.text.indexOf("or"),
            myTextView.text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#007AFF")),
            myTextView.text.indexOf("or"),
            myTextView.text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        myTextView.text= spannableString
        myTextView.movementMethod = LinkMovementMethod.getInstance();
    }

}