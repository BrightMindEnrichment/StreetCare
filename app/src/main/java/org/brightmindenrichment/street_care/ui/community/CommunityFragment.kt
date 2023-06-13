package org.brightmindenrichment.street_care.ui.community

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realtimelocation.CommunityActivityAdapter
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentCommunityBinding
import org.brightmindenrichment.street_care.ui.community.model.CommunityActivityObject
import org.brightmindenrichment.street_care.ui.community.viewModel.CommunityViewModel
import java.util.*


private val TAG = "COMMUNITY_FRAGMENT"
private const val REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001
class CommunityFragment : Fragment() {

    private lateinit var binding: FragmentCommunityBinding
    private lateinit var locationManager: LocationManager
    private lateinit var cityTextView: TextView
    private lateinit var allActivitiesBtn: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewModel: CommunityViewModel
    private lateinit var myPostText: TextView
    private lateinit var adapter: CommunityActivityAdapter
    private val permissionId = 2
    val activityModel = CommunityActivityObject.Builder()
        .setLocation("BOS")
        .setTime("05/01/2023")
        .setDescription("Start an Activity")
        .build()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommunityBinding.inflate(inflater, container, false)
        cityTextView = binding.cityTextView
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        allActivitiesBtn = binding.viewAllActivityBtn
//        addMyPostButton()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(CommunityViewModel::class.java)
        setupRecyclerView()
        viewModel.activitiesLiveData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            adapter.submitList(it)
        })

    }

    override fun onStart() {
        super.onStart()
        if (checkGooglePlayServices()) {
            getLocation()
        }
    }
    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                fusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        Log.d(TAG,"not null location")
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        val list: List<Address> = geocoder
                                .getFromLocation(location.latitude, location.longitude, 1)
                                    as List<Address>
                        cityTextView.paintFlags = cityTextView.paintFlags or
                                Paint.UNDERLINE_TEXT_FLAG
                        cityTextView.text = list[0].locality
                    }else{
                        Log.d(TAG,"null location")
                    }
                }
            } else {
                Toast.makeText(requireContext(), R.string.turn_on_location, Toast.LENGTH_LONG)
                    .show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }
    private fun checkGooglePlayServices(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val checkGooglePlayServices =
            googleApiAvailability.isGooglePlayServicesAvailable(requireContext())
        if (checkGooglePlayServices != ConnectionResult.SUCCESS) {
            /*
        * Google Play Services is missing or update is required
        *  return code could be
        * SUCCESS,
        * SERVICE_MISSING, SERVICE_VERSION_UPDATE_REQUIRED,
        * SERVICE_DISABLED, SERVICE_INVALID.
        */
            if (googleApiAvailability.isUserResolvableError(checkGooglePlayServices)) {
                googleApiAvailability.getErrorDialog(
                    requireActivity(),
                    checkGooglePlayServices,
                    REQUEST_CODE_RECOVER_PLAY_SERVICES
                )!!
                    .show()
            }
            return false
        }
        return true
    }
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }
    private fun requestPermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }
    private fun addMyPostButton(){
        val toolbar = activity?.findViewById<MaterialToolbar>(R.id.toolbar)
        if (toolbar == null) {
            Log.d("BME", "Did not find toolbar")
        } else {
            myPostText = TextView(this.context).apply {
                text = "MyPost"
                textSize = 8f
                setTextColor(Color.parseColor("#007AFF"))
            }
            toolbar.addView(myPostText)

            //Have the add button invisible by default
            myPostText.visibility = View.GONE
            myPostText.setOnClickListener {
                findNavController().navigate(R.id.nav_add_event)
                Log.d("BME", "Add")
                disableMyPost()//onStop()
            }
        }
    }
    private fun setupRecyclerView() {
        adapter = CommunityActivityAdapter()
        binding.recyclerView2.layoutManager = LinearLayoutManager(context)
        binding.recyclerView2.adapter = adapter
        val dividerItemDecoration = DividerItemDecorator(
            ContextCompat.getDrawable(requireContext(), R.drawable.divider)!!
        )
        binding.recyclerView2.addItemDecoration(dividerItemDecoration)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionId) {
            if (grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                //update UI
                getLocation()
            }
        }
    }
    override fun onResume() {
        super.onResume()
        Log.d("BME", "onResume")
        //set the buttonAdd back on if the user is logged in
//        enableMyPost()
    }

    override fun onStop() {
        super.onStop()
//       disableMyPost()
    }
    private fun enableMyPost(){
        if(Firebase.auth.currentUser != null) {
            if (findToolbar()) {
                myPostText.visibility = View.VISIBLE
            }
        }
    }
    private fun disableMyPost(){
        if (findToolbar()){
            myPostText.visibility = View.GONE
        }
    }
    private fun findToolbar(): Boolean {
        return (activity?.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)!=null)
    }

}