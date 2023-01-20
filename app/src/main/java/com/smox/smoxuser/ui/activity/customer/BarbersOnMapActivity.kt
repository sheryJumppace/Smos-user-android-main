package com.smox.smoxuser.ui.activity.customer

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.smox.smoxuser.R
import com.smox.smoxuser.data.BarberRepository
import com.smox.smoxuser.databinding.ActivityBarbersOnMapBinding
import com.smox.smoxuser.model.Barber
import com.smox.smoxuser.ui.adapter.BarberMapAdapter
import com.smox.smoxuser.viewmodel.BarberListViewModel
import com.smox.smoxuser.viewmodel.BarberListViewModelFactory

class BarbersOnMapActivity : AppCompatActivity(), BarberMapAdapter.ItemClickListner,
    OnMapReadyCallback {
    private val TAG = "BarbersOnMapActivity"
    lateinit var binding: ActivityBarbersOnMapBinding
    private lateinit var viewModel: BarberListViewModel
    private var barberList: ArrayList<Barber> = ArrayList()
    lateinit var adapter: BarberMapAdapter
    var map: GoogleMap? = null
    var markerIdList = arrayListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_barbers_on_map)

        var calledFrom=intent.getStringExtra("calledFrom")
        if (calledFrom.equals("homePage")){
            val factory = BarberListViewModelFactory(BarberRepository.getInstance())
            viewModel = ViewModelProviders.of(this, factory).get(BarberListViewModel::class.java)
            viewModel.barbers.observe(this, Observer { barbers ->
                if (barbers.isNotEmpty()) {
                    barberList.addAll(barbers)
                    addMarkerOnMap()
                }
                adapter.setData(barberList)
            })
            binding.rvBarbersList.visibility=View.VISIBLE
        }else{
            binding.rvBarbersList.visibility=View.GONE
        }

        val supportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        supportMapFragment!!.getMapAsync(this)

        adapter = BarberMapAdapter(this,this)
        binding.rvBarbersList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvBarbersList.adapter = adapter
        var snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvBarbersList)

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun addMarkerOnMap() {
        if (map != null && barberList.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.marker_icon)
            val bitmapDescriptor: BitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)

            for (barberItem in barberList) {
                markerIdList.add(
                    map?.addMarker(
                        MarkerOptions().position(LatLng(barberItem.latitude, barberItem.longitude))
                            .icon(bitmapDescriptor)
                    )?.id.toString()
                )
            }

            map?.setOnMarkerClickListener { marker->
                if (markerIdList.contains(marker.id)){
                    var index=markerIdList.indexOf(marker.id)
                    binding.rvBarbersList.smoothScrollToPosition(index)
                }
                false
            }
        }

        Log.e(TAG, "addMarkerOnMap: ${markerIdList.size}")

    }

    override fun onItemClickListner(barber: Barber) {
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("barber_id", barber.id)
        startActivity(intent)
        overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map!!.uiSettings.isCompassEnabled = false

        addMarkerOnMap()


    }
}