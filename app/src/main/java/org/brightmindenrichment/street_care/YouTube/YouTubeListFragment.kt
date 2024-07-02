package org.brightmindenrichment.street_care.YouTube

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineExceptionHandler
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentYouTubeListBinding


private const val ARG_VIDEO_ID = "videoId"

class YouTubeListFragment : Fragment(), YouTubeListRecyclerAdapter.YouTubeRecyclerAdapterDelegate {

    private var _binding : FragmentYouTubeListBinding? = null
    val binding get() = _binding!!

    private var paramVideoId: String? = null
    var controller = YouTubeController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            paramVideoId = it.getString(ARG_VIDEO_ID)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentYouTubeListBinding.inflate(inflater, container, false)
        return _binding!!.root

        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_you_tube_list, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateUI()
    }


    private fun createErrorHandler() : CoroutineExceptionHandler {

        val errorHandler = CoroutineExceptionHandler { _, exception ->
            AlertDialog.Builder(requireContext()).setTitle(getString(R.string.error))
                .setMessage(exception.message)
                .setPositiveButton(getString(R.string.ok)) { _, _ -> }
                .setIcon(R.drawable.donate_icon)
                .show()
        }

        return errorHandler
    }


    fun updateUI() {

        val errorHandler = createErrorHandler()

        if (paramVideoId != null) {
            controller.refresh(paramVideoId!!, errorHandler) {

                var items = controller.playlist?.items

                Log.i(TAG, items.toString())

                val recyclerView = view?.findViewById<RecyclerView>(R.id.youTubeListRecyclerView)

                recyclerView?.layoutManager = LinearLayoutManager(view?.context)
                recyclerView?.adapter = YouTubeListRecyclerAdapter(controller, this)

            }
        }
    }


    override fun onItemClick(position: Int) {

        val item = controller.itemAtIndex(position)

        if (item != null) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=${item.contentDetails.videoId}")
                )
            )
        }

    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment YouTubeListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            YouTubeListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_VIDEO_ID, paramVideoId)
                }
            }
    }
}