package com.smox.smoxuser.ui.activity.customer

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.smox.smoxuser.App
import com.smox.smoxuser.utils.listeners.OnGetAddress
import java.util.*

open class LocationActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    var currAddress = ""
    var currLatLon = LatLng(0.0, 0.0)
    lateinit var onGetAddress: OnGetAddress

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
        const val REQUEST_CHECK_SETTINGS = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("++--++", "LocationActivity is called")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
                lastLocation?.apply {
                    //currLatLon = LatLng(28.626153, 77.372920)
                    currLatLon = LatLng(latitude, longitude)
                    App.instance.myLocation = currLatLon
                }
                Log.e(
                    "LocationActivity",
                    "onLocationResult: ${lastLocation!!.latitude}, ${lastLocation!!.longitude}",
                )
                getAddressFromLocation()

            }
        }
        createLocationRequest()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    fun locListner(onGetAddress: OnGetAddress) {
        this.onGetAddress = onGetAddress
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // Request for camera permission.
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                // Permission request was denied.
                //startLocationUpdates()
            }
        }
    }

    private fun getAddressFromLocation() {
        val addresses: List<Address>
        val geocoder = Geocoder(this, Locale.getDefault())

        try {
            addresses =
                geocoder.getFromLocation(lastLocation!!.latitude, lastLocation!!.longitude, 1)

            val address: String =
                addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

            val city: String = addresses[0].locality ?: ""
            val state: String = addresses[0].adminArea ?: ""
            val country: String = addresses[0].countryName
//            val postalCode: String = addresses[0].postalCode
            val knownName: String = addresses[0].featureName ?: ""

//            Log.e("TAG", "getAddressFromLocation: $city $state $country $postalCode $knownName")
            Log.e("TAG", "getAddressFromLocation: $city $state $country  $knownName")

            currAddress = "$city, $state"
            onGetAddress.onGetAddress(currAddress, currLatLon)
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            Log.e("LocationActivity", "-----> getAddressFromLocation: tryCatch " + e.message)
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    public override fun onResume() {
        super.onResume()
        //if (!locationUpdateState) {
        startLocationUpdates()
        //}
    }

    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()!! /* Looper */
        )
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
            }
            lastLocation?.apply {
                val currentLatLng = LatLng(latitude, longitude)
                App.instance.myLocation = currentLatLng
            }
        }
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(
                        this@LocationActivity, REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }


}
