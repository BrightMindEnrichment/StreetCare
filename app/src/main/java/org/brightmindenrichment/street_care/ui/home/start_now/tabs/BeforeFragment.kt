package org.brightmindenrichment.street_care.ui.home.start_now.tabs

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.CardBeforeFragmentBinding
import org.brightmindenrichment.street_care.databinding.FragmentBeforeBinding
import java.nio.file.Paths.get


class BeforeFragment : Fragment() {


    private val TAG: String = "Before Fragment"
    private lateinit var viewModel: BeforeViewModel

    private var _binding :FragmentBeforeBinding? = null
    private val binding get() = _binding!!

    private lateinit var includedView: CardBeforeFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentBeforeBinding.inflate(inflater, container, false)
         val view : View = binding.root
        includedView = binding.cardBeforeFragment
        viewModel = ViewModelProvider(this)[BeforeViewModel::class.java]
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showIntroDetails()
        includedView.imageLetSomeoneKnow.setOnClickListener {
            showLetSomeoneKnowDetails()
        }

        includedView.imageHowToPrepare.setOnClickListener {
            showHowToPrepareDetails()
        }

        includedView.imageMustCarry.setOnClickListener {
            showMustCarryDetails()
        }

        includedView.imagePlanAnIntro.setOnClickListener {
            showPlanAnIntroDetails()
        }

    }


    private fun showLetSomeoneKnowDetails() {
        if (includedView.linearlayoutLetSomeoneKnow.visibility == View.GONE) {
            TransitionManager.beginDelayedTransition(includedView.cardViewLetSomeoneKnow, AutoTransition())
            includedView.linearlayoutLetSomeoneKnow.visibility = View.VISIBLE
            displayLetSomeoneKnowDetails()
            includedView.imageLetSomeoneKnow.setImageResource(R.drawable.ic_up_arrow)


        } else {
            TransitionManager.beginDelayedTransition(includedView.cardViewLetSomeoneKnow, AutoTransition())
            includedView.linearlayoutLetSomeoneKnow.visibility = View.GONE
            includedView.imageLetSomeoneKnow.setImageResource(R.drawable.ic_down_arrow)

        }
    }

    private fun showHowToPrepareDetails() {
        if(includedView.linearlayoutHowToPrepare.visibility == View.GONE){
            TransitionManager.beginDelayedTransition(includedView.cardViewHowToPrepare, AutoTransition())
            includedView.linearlayoutHowToPrepare.visibility = View.VISIBLE
            displayHowToPrepareDetails()
            includedView.imageHowToPrepare.setImageResource(R.drawable.ic_up_arrow)

        }else{
            TransitionManager.beginDelayedTransition(includedView.cardViewHowToPrepare, AutoTransition())
            includedView.linearlayoutHowToPrepare.visibility = View.GONE
            includedView.imageHowToPrepare.setImageResource(R.drawable.ic_down_arrow)
        }

    }

    private fun showMustCarryDetails() {
        if(includedView.linearlayoutMustCarry.visibility == View.GONE){
            TransitionManager.beginDelayedTransition(includedView.cardViewMustCarry, AutoTransition())
            includedView.linearlayoutMustCarry.visibility = View.VISIBLE
            displayMustCarryDetails()
            includedView.imageMustCarry.setImageResource(R.drawable.ic_up_arrow)
        }else{
            TransitionManager.beginDelayedTransition(includedView.cardViewMustCarry, AutoTransition())
            includedView.linearlayoutMustCarry.visibility = View.GONE
            includedView.imageMustCarry.setImageResource(R.drawable.ic_down_arrow)
        }

    }

    private fun showPlanAnIntroDetails() {
        if(includedView.linearlayoutPlanAnIntro.visibility == View.GONE){
            TransitionManager.beginDelayedTransition(includedView.cardViewPlanAnIntro, AutoTransition())
            includedView.linearlayoutPlanAnIntro.visibility = View.VISIBLE
            displayPlanAnIntroDetails()
            includedView.imagePlanAnIntro.setImageResource(R.drawable.ic_up_arrow)
        }else{
            TransitionManager.beginDelayedTransition(includedView.cardViewPlanAnIntro, AutoTransition())
            includedView.linearlayoutPlanAnIntro.visibility = View.GONE
            includedView.imagePlanAnIntro.setImageResource(R.drawable.ic_down_arrow)
        }

    } // end of showPlanAnIntroDetails method


    private fun showIntroDetails(){
        viewModel.beforePageLiveData.observe(viewLifecycleOwner, Observer { beforeAfterData ->
            binding.beforePageIntro.text = beforeAfterData.beforeIntro
        })
    }

     fun displayLetSomeoneKnowDetails(){

        includedView.tvLetSomeoneKnow.text =
            viewModel.beforePageLiveData.value?.getBeforePageContent("para2")
    }

    private fun displayHowToPrepareDetails(){
        includedView.tvHowToPrepare.text = viewModel.beforePageLiveData.value?.getBeforePageContent("para3")


    }

    private fun displayMustCarryDetails(){
        includedView.tvMustCarry.text =
            viewModel.beforePageLiveData.value?.getBeforePageContent("para4")

    }

    private fun displayPlanAnIntroDetails() {
        includedView.tvPlanAnIntro.text =
            viewModel.beforePageLiveData.value?.getBeforePageContent("para5")

      //  documentSnapshot.data?.get("para5") as CharSequence

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


} //end of class