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
import org.brightmindenrichment.street_care.R
import org.brightmindenrichment.street_care.databinding.FragmentCommunityBinding
import org.brightmindenrichment.street_care.ui.community.adapter.CommunityActivityAdapter
import org.brightmindenrichment.street_care.ui.community.model.CommunityActivityObject
import org.brightmindenrichment.street_care.ui.community.model.CommunityPageName
import org.brightmindenrichment.street_care.ui.community.viewModel.CommunityViewModel
import org.brightmindenrichment.street_care.ui.visit.VisitDataAdapter
import java.util.*


private val TAG = "COMMUNITY_FRAGMENT"
private const val REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001
class CommunityFragment : Fragment()  {

    private lateinit var binding: FragmentCommunityBinding
    private lateinit var cityTextView: TextView
    private lateinit var allActivitiesBtn: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewModel: CommunityViewModel
    private lateinit var adapter: CommunityActivityAdapter
    private val permissionId = 2
    private val visitDataAdapter = VisitDataAdapter()

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