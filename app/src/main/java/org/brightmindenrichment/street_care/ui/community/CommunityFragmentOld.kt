package org.brightmindenrichment.street_care.ui.community

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.R


class CommunityFragmentOld : Fragment() {
    lateinit var buttonAdd: ImageButton
    private val eventDataAdapter = EventDataAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
        //initialize the tool bar and add the buttonAdd
        val toolbar = activity?.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        if (toolbar == null) {
            Log.d("BME", "Did not find toolbar")
        } else {
            buttonAdd = ImageButton(this.context)
            buttonAdd.setBackgroundResource(R.drawable.ic_menu_add)
            val l3 = Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT
            )
            l3.gravity = Gravity.LEFT
            buttonAdd.layoutParams = l3
            toolbar.addView(buttonAdd)
            //Have the add button invisible by default
            buttonAdd.visibility = View.GONE
            buttonAdd.setOnClickListener {
                findNavController().navigate(R.id.nav_add_event)
                Log.d("BME", "Add")
                onStop()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_community_old, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Firebase.auth.currentUser == null) {
            val layout = view.findViewById<LinearLayout>(R.id.root)
            val textView = TextView(context)
            //setting height and width
            textView.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            textView.text = "Events are only available for logged in Users"
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            textView.setTextColor(Color.GRAY)
            textView.setPadding(20, 20, 20, 20)
            textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            textView.gravity = Gravity.CENTER_VERTICAL
            textView.isAllCaps=false
            layout?.addView(textView)
        }
        else{
            eventDataAdapter.refresh {
                val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerCommunity)
                recyclerView?.layoutManager = LinearLayoutManager(view?.context)
                recyclerView?.adapter = CommunityRecyclerAdapter(eventDataAdapter)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("BME", "onResume")
        //set the buttonAdd back on if the user is logged in
        if(Firebase.auth.currentUser != null) {
            val toolbar = activity?.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
            if (toolbar == null) {
                Log.d("BME", "Did not find toolbar")
            } else {
                buttonAdd.visibility = View.VISIBLE
            }
        }
    }

    override fun onStop() {
        super.onStop()
        val toolbar = activity?.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        if (toolbar == null) {
            Log.d("BME", "Did not find toolbar")
        } else {
            buttonAdd.visibility = View.GONE
        }
    }
}// end class