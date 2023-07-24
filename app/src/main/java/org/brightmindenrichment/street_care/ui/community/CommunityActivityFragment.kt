package org.brightmindenrichment.street_care.ui.community


import android.content.ContentValues
import android.graphics.Color
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isNotEmpty
import androidx.recyclerview.widget.LinearLayoutManager
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentCommunityActivityBinding
import org.brightmindenrichment.street_care.ui.community.adapter.CommunityActivityAdapter
import org.brightmindenrichment.street_care.ui.community.viewModel.CommunityViewModel
import org.brightmindenrichment.street_care.ui.visit.VisitDataAdapter

class CommunityActivityFragment : Fragment() {

    private lateinit var viewModel: CommunityViewModel
    private lateinit var binding: FragmentCommunityActivityBinding
    private val visitDataAdapter = VisitDataAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommunityActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        visitDataAdapter.getPublicVisitLog {
                binding.recyclerView.layoutManager = LinearLayoutManager(context)
                binding.recyclerView.adapter = CommunityActivityAdapter( visitDataAdapter)

                val dividerItemDecoration = DividerItemDecorator(
                    ContextCompat.getDrawable(requireContext(), R.drawable.divider)!!
                )
                binding.recyclerView.addItemDecoration(dividerItemDecoration)

        }
        //viewModel = ViewModelProvider(this).get(CommunityViewModel::class.java)
    }

}