package com.smox.smoxuser.ui.activity

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.android.synthetic.main.activity_location_picker.*
import java.io.IOException
import com.smox.smoxuser.R
import java.util.*


class LocationPickerActivity : BaseActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {
    private var map: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    private var predictions: List<AutocompletePrediction>? = null
    private val items = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private var currentMarker: Marker? = null
    private var currentLocation: Location? = null
    private val defaultZoom = 12f
    private var isAddMyLocation = false

    private lateinit var placesClient: PlacesClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_picker)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
        }
        progressBar.visibility = View.GONE

        searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                btnSelectAddress.visibility = View.GONE
                if (newText != null && newText.length > 2) {
                    fetchPlaces(newText)
                }
                return false
            }

        })
        createLocationRequest()

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)

        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            predictions?.apply {
                val prediction = this[position]
                fetchLocationFromPLACEID(prediction.placeId)
            }
        }

        btnSelectAddress.setOnClickListener {
            val returnIntent = Intent()
            returnIntent.putExtra("address", txtAddress.text)
            currentLocation?.apply {
                returnIntent.putExtra("lat", latitude)
                returnIntent.putExtra("lng", longitude)
            }
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
        btnSelectAddress.visibility = View.GONE


        Places.initialize(applicationContext, getString(R.string.google_maps_key))

        // Create a new Places client instance.
        placesClient = Places.createClient(this)
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

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMyLocationButtonEnabled = true
            setOnMarkerClickListener(this@LocationPickerActivity)

            setPadding(0, (searchView.height.toFloat() * 1.5).toInt(), 0, 0)
            try {
                // Customise the styling of the base map using a JSON object defined
                // in a raw resource file.
                val success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        applicationContext, R.raw.style
                    )
                )

                if (success) {
                } else {
                    Log.e("Map", "Style parsing failed.")
                }
            } catch (e: Resources.NotFoundException) {
                Log.e("Map", "Can't find style. Error: ", e)
            }
            setUpMap()
        }

    }

    override fun onMarkerClick(p0: Marker?) = false

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        map?.isMyLocationEnabled = true
        map?.mapType = GoogleMap.MAP_TYPE_NORMAL

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                if (!isAddMyLocation) {
                    isAddMyLocation = true
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    val address = getAddress(currentLatLng)
                    setNewMapMarker(currentLatLng, address)
                }
            }
        }
    }

    private fun setNewMapMarker(latLng: LatLng, address: String) {
        btnSelectAddress.visibility = View.VISIBLE
        txtAddress.text = address
        txtLocation.text = String.format("%f, %f", latLng.latitude, latLng.longitude)

        if (map != null) {
            if (currentMarker != null) {
                currentMarker!!.remove()
            }
            val cameraPosition = CameraPosition.Builder().target(latLng)
                .zoom(defaultZoom)
                .build()
            map!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            currentMarker = addMarker(latLng, address)
            map!!.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
                override fun onMarkerDragStart(marker: Marker) {
                    btnSelectAddress.visibility = View.GONE
                }

                override fun onMarkerDrag(marker: Marker) {}

                override fun onMarkerDragEnd(marker: Marker) {
                    if (currentLocation == null) {
                        currentLocation = Location("network")
                    }
                    currentLocation!!.longitude = marker.position.longitude
                    currentLocation!!.latitude = marker.position.latitude
                    val line = getAddress(marker.position)
                    txtAddress.text = line
                    btnSelectAddress.visibility = View.VISIBLE
                }
            })
        }

        if (currentLocation == null) {
            currentLocation = Location("network")
        }
        currentLocation!!.longitude = latLng.longitude
        currentLocation!!.latitude = latLng.latitude
    }

    private fun addMarker(latLng: LatLng, address: String): Marker {
        return map!!.addMarker(
            MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title(address)
        )
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
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
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
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
                        this@LocationPickerActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun fetchPlaces(place: String) {
        progressBar.visibility = View.VISIBLE

        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        val token = AutocompleteSessionToken.newInstance()
        // Use the builder to create a FindAutocompletePredictionsRequest.
        val request = FindAutocompletePredictionsRequest.builder()
            .setTypeFilter(TypeFilter.ADDRESS)
            .setSessionToken(token)
            .setQuery(place)
            .build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            predictions = response.autocompletePredictions
            items.clear()
            for (prediction in predictions!!) {
                items.add(prediction.getPrimaryText(null).toString())
            }
            adapter.notifyDataSetChanged()
            progressBar.visibility = View.GONE

        }.addOnFailureListener { exception ->
            if (exception is ApiException) {
                Log.e("LOCATION", "Place not found: " + exception.statusCode)
            }
            progressBar.visibility = View.GONE
        }

    }

    private fun fetchLocationFromPLACEID(placeID: String) {
        val placeFields = Arrays.asList(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )

        // Construct a request object, passing the place ID and fields array.
        val request = FetchPlaceRequest.builder(placeID, placeFields)
            .build()

        // Add a listener to handle the response.
        placesClient.fetchPlace(request).addOnSuccessListener { response ->
            val place = response.place
            place.latLng?.apply {
                setNewMapMarker(this, place.address!!)
                items.clear()
                adapter.notifyDataSetChanged()
            }
        }.addOnFailureListener {
            items.clear()
            adapter.notifyDataSetChanged()
        }
    }

    private fun getAddress(latLng: LatLng): String {
        // 1
        val geocoder = Geocoder(this@LocationPickerActivity)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {
            // 2
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            // 3
            if (null != addresses && addresses.isNotEmpty()) {
                address = addresses[0]
                val addressFragments = with(address) {
                    (0..maxAddressLineIndex).map { getAddressLine(it) }
                }
                addressText = addressFragments.joinToString(separator = "\n")
                currentMarker?.title = addressText
//                for (i in 0 until address.maxAddressLineIndex) {
//                    addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(i)
//                }
            }
        } catch (e: IOException) {
            Log.e("LocationPickerActivity", e.localizedMessage)
        }

        return addressText
    }
}
