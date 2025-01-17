package org.brightmindenrichment.street_care.ui.community

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentCommunityBinding
import org.brightmindenrichment.street_care.ui.community.adapter.CommunityActivityAdapter
import org.brightmindenrichment.street_care.ui.community.model.CommunityActivityObject
import org.brightmindenrichment.street_care.ui.community.model.CommunityPageName
import org.brightmindenrichment.street_care.ui.community.viewModel.CommunityViewModel
import org.brightmindenrichment.street_care.ui.visit.VisitDataAdapter
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.brightmindenrichment.street_care.util.Queries.getUpcomingEventsQueryUpTo50
import org.brightmindenrichment.street_care.util.Queries.getHelpRequestDefaultQueryUpTo50
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.util.*


private val TAG = "COMMUNITY_FRAGMENT"
private const val REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001
class CommunityFragment : Fragment(), OnMapReadyCallback  {

    private lateinit var binding: FragmentCommunityBinding
    //private lateinit var cityTextView: TextView
    private lateinit var allActivitiesBtn: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewModel: CommunityViewModel
    private lateinit var adapter: CommunityActivityAdapter
    private val permissionId = 2
    private val visitDataAdapter = VisitDataAdapter()

    // Map properties - using SupportMapFragment instead of MapView
    private lateinit var mMap: GoogleMap

    // Marker data for map
    private data class MarkerData(
        val position: LatLng,
        val title: String,
        val description: String,
        val markerColor: Float
    )
    private var cachedEvents: List<MarkerData>? = null
    private var cachedHelpRequests: List<MarkerData>? = null

    // Geocode for Maps
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

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

      // cityTextView = binding.cityTextView
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Initialize map
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.pastEventComponent.setOnClickListener {
            findNavController().navigate(R.id.communityEventFragment, Bundle().apply {
                //putBoolean("isPastEvents", true)
                putString("pageTitle", it.context.getString(R.string.past_events))
                putSerializable("communityPageName", CommunityPageName.PAST_EVENTS)
            })
        }

        binding.upcomingEventComponent.setOnClickListener {
            findNavController().navigate(R.id.communityEventFragment, Bundle().apply {
                //putBoolean("isPastEvents", false)
                putString("pageTitle", it.context.getString(R.string.future_events))
                putSerializable("communityPageName", CommunityPageName.UPCOMING_EVENTS)
            })
        }

        binding.helpRequestsComponent.setOnClickListener {
            Log.d("debug", "click help requests icon on community page")
            findNavController().navigate(R.id.communityHelpRequestFragment, Bundle().apply {
                //putBoolean("isPastEvents", false)
                putString("pageTitle", context?.getString(R.string.help_request))

            })
        }

        setHelpComponentListener()
        setRequestComponentListener()
        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED) {
                    mMap.isMyLocationEnabled = true

                    // Center map on user's location
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            val currentLatLng = LatLng(location.latitude, location.longitude)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 11f))
                        }
                    }
                }
            }
        }
        loadEvents()
        loadHelpRequests()
    }

    private fun addMarkerToMap(markerData: MarkerData) {
        activity?.runOnUiThread {
            mMap.addMarker(MarkerOptions()
                .position(markerData.position)
                .title(markerData.title)
                .snippet(markerData.description)
                .icon(BitmapDescriptorFactory.defaultMarker(markerData.markerColor)))
        }
    }

    private fun loadEvents() {
        // Use cached events if available
        if (cachedEvents != null) {
            cachedEvents?.forEach { markerData ->
                addMarkerToMap(markerData)
            }
            return
        }

        getUpcomingEventsQueryUpTo50().get()
            .addOnSuccessListener { querySnapshot ->
                coroutineScope.launch(Dispatchers.IO) {
                    val markerDataList = mutableListOf<MarkerData>()
                    val geocoder = Geocoder(requireContext())

                    // Process all locations in parallel
                    val deferredResults = querySnapshot.documents.map { document ->
                        async {
                            try {
                                val location = document.get("location").toString()
                                val title = document.getString("title") ?: "Event"
                                val description = document.getString("description") ?: ""

                                val addresses = geocoder.getFromLocationName(location, 1)
                                if (addresses?.isNotEmpty() == true) {
                                    val address = addresses[0]
                                    val eventLocation = LatLng(
                                        address.latitude,
                                        address.longitude
                                    )
                                    MarkerData(
                                        eventLocation,
                                        title,
                                        description,
                                        BitmapDescriptorFactory.HUE_YELLOW
                                    )
                                } else null
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }

                    // Wait for all geocoding to complete
                    deferredResults.awaitAll()
                        .filterNotNull()
                        .forEach { markerData ->
                            markerDataList.add(markerData)
                            withContext(Dispatchers.Main) {
                                if (isAdded) {
                                    addMarkerToMap(markerData)
                                }
                            }
                        }
                    // uupdate UI
                    withContext(Dispatchers.Main) {
                        if (isAdded) {
                            cachedEvents = markerDataList
                        }
                    }
                }
            }
    }

    private fun loadHelpRequests() {
        // Use cached help requests if available
        if (cachedHelpRequests != null) {
            cachedHelpRequests?.forEach { markerData ->
                addMarkerToMap(markerData)
            }
            return
        }

        getHelpRequestDefaultQueryUpTo50().get()
            .addOnSuccessListener { querySnapshot ->
                coroutineScope.launch(Dispatchers.IO) {
                    val markerDataList = mutableListOf<MarkerData>()
                    val geocoder = Geocoder(requireContext())

                    // Process all locations in parallel
                    val deferredResults = querySnapshot.documents.map { document ->
                        async {
                            try {
                                val location = document.get("location").toString()
                                val title = document.getString("title") ?: "Help Request"
                                val description = document.getString("description") ?: ""
                                val isHelpNeeded = document.getBoolean("isHelpNeeded") ?: true

                                val addresses = geocoder.getFromLocationName(location, 1)
                                if (addresses?.isNotEmpty() == true) {
                                    val address = addresses[0]
                                    val helpLocation = LatLng(
                                        address.latitude,
                                        address.longitude
                                    )
                                    MarkerData(
                                        helpLocation,
                                        title,
                                        description,
                                        if (isHelpNeeded) BitmapDescriptorFactory.HUE_RED else BitmapDescriptorFactory.HUE_GREEN
                                    )
                                } else null
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }

                    // Wait for all geocoding to complete
                    deferredResults.awaitAll()
                        .filterNotNull()
                        .forEach { markerData ->
                            markerDataList.add(markerData)
                            withContext(Dispatchers.Main) {
                                if (isAdded) {
                                    addMarkerToMap(markerData)
                                }
                            }
                        }

                    withContext(Dispatchers.Main) {
                        if (isAdded) {
                            cachedHelpRequests = markerDataList
                        }
                    }
                }
            }
    }

    private fun setEventListener(){

    }

    private fun setRequestComponentListener(){
        binding.requestComponent.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(NavigationUtil.FRAGMENT_KEY, NavigationUtil.FRAGMENT_REQUEST)
            findNavController().navigate(R.id.communityHelpFragment,bundle)
        }

    }

    private fun setHelpComponentListener(){
        binding.helpComponent.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(NavigationUtil.FRAGMENT_KEY, NavigationUtil.FRAGMENT_HELP)
            findNavController().navigate(R.id.communityHelpFragment,bundle)
        }
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
//                        cityTextView.paintFlags = cityTextView.paintFlags or
//                                Paint.UNDERLINE_TEXT_FLAG
//                        cityTextView.text = list[0].locality
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
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
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


}