package com.smox.smoxuser.ui.fragment.customer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.smox.smoxuser.data.BarberRepository
import com.smox.smoxuser.databinding.FragmentBarberSearchBinding
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Barber
import com.smox.smoxuser.ui.activity.customer.BarbersOnMapActivity
import com.smox.smoxuser.ui.activity.customer.DetailsActivity
import com.smox.smoxuser.ui.activity.customer.FavoritesActivity
import com.smox.smoxuser.ui.activity.home.Home2Activity
import com.smox.smoxuser.ui.adapter.BarberSearchAdapter
import com.smox.smoxuser.ui.adapter.FavoriteAdapter
import com.smox.smoxuser.ui.adapter.FavoriteAdapter.ItemFavClick
import com.smox.smoxuser.ui.fragment.BaseFragment
import com.smox.smoxuser.utils.listeners.OnLocFound
import com.smox.smoxuser.utils.listeners.OnSearchItem
import com.smox.smoxuser.viewmodel.BarberListViewModel
import com.smox.smoxuser.viewmodel.BarberListViewModelFactory
import org.json.JSONObject
import com.smox.smoxuser.R
import com.smox.smoxuser.utils.shortToast


class BarberSearchFragment : BaseFragment(), BarberSearchAdapter.ItemClickListner, OnSearchItem,
    OnLocFound {

    private val TAG = "BarberSearchFragment"

    private lateinit var viewModel: BarberListViewModel
    private lateinit var binding: FragmentBarberSearchBinding
    private lateinit var adapter: BarberSearchAdapter
    private lateinit var favoriteAdapter: FavoriteAdapter
    private var pageStart = 0
    private var isLastPage = false

    private var barberList: ArrayList<Barber> = ArrayList()
    var currentLatLng: LatLng? = null
    var isFirstCall = true
    lateinit var contextt: Context

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBarberSearchBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = BarberListViewModelFactory(BarberRepository.getInstance())
        viewModel = ViewModelProviders.of(this, factory).get(BarberListViewModel::class.java)


        if (sessionManager.apiKey?.isEmpty()!!)
            binding.llFavourite.visibility=View.GONE

        adapter = BarberSearchAdapter(requireContext(), this)
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvVertical.layoutManager = layoutManager
        binding.rvVertical.adapter = adapter

        binding.nestedScroll.viewTreeObserver?.addOnScrollChangedListener {
            val view = binding.nestedScroll.getChildAt(binding.nestedScroll.childCount - 1)
            val diff = view.bottom - (binding.nestedScroll.height + binding.nestedScroll.scrollY)

            if (diff == 0) {
                if (currentLatLng != null)
                    callBarberListData()
            }
        }

        val act = requireActivity() as Home2Activity
        act.onSearchClicked(this)
        act.onFoundLocation(this)

        viewModel.barbers.observe(viewLifecycleOwner, Observer { barbers ->
            binding.bottomProgressBar.visibility = View.GONE
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                Log.e(TAG, "onViewCreated: observer resumed")
            } else {
                Log.e(TAG, "onViewCreated: observer working")
            }
            if (barbers.isNotEmpty()) {
                isLastPage = false
                barberList.addAll(barbers)
                adapter.setData(barbers as java.util.ArrayList<Barber>)
            } else isLastPage = true

        })

        favoriteAdapter =
            FavoriteAdapter(requireContext(), object : FavoriteAdapter.ItemClickListner {
                override fun onItemClickListner(barber: Barber) {
                    val intent = Intent(activity, DetailsActivity::class.java)
                    intent.putExtra("barber_id", barber.id)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(
                        R.anim.activity_enter,
                        R.anim.activity_exit
                    )
                }
            }, object : ItemFavClick {
                override fun onRemoveFav(barber: Barber) {
                    updateFavoriteStatus(barber)
                }
            })

        binding.rvHorizontal.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvHorizontal.adapter = favoriteAdapter

        viewModel.favBarbers.observe(viewLifecycleOwner, Observer { barbers ->
            favoriteAdapter.clearData()
            favoriteAdapter.setData(barbers as java.util.ArrayList<Barber>)

        })

        binding.fabButton.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    BarbersOnMapActivity::class.java
                ).putExtra("calledFrom", "homePage")
            )
        }

        binding.txtViewAll.setOnClickListener {
            startActivity(Intent(requireContext(), FavoritesActivity::class.java))
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            if (currentLatLng != null) {
                BarberRepository.getInstance().clearMainList()
                pageStart = 0
                isLastPage = false
                barberList.clear()
                adapter.clearData()
                callBarberListData()
                callFavouriteBarberApi()
            }
        }
    }

    private fun callFavouriteBarberApi() {
        if (sessionManager.apiKey?.isNotEmpty()!!){
            viewModel.fetchList(requireContext(), currentLatLng, isFavorite = true, page = "1")
        }
    }


    private fun callBarberListData() {
        if (!this.isLastPage) {
            pageStart += 1
            if (pageStart != 1) {
                binding.bottomProgressBar.visibility = View.VISIBLE
            }
            Log.e(TAG, "loadMoreItems: called pageStart: $pageStart   isLastPage: $isLastPage")
            viewModel.fetchList(contextt, currentLatLng, page = pageStart.toString())
        }

    }

    public fun updateMap() {
        enableMyLocation()
        //startLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume: ")
        hideKeyBoard()
        activity?.findViewById<LinearLayoutCompat>(R.id.llCurrInfo)?.visibility = View.VISIBLE

        if (currentLatLng != null) {
            Log.e(TAG, "onResume: api called")
            pageStart = 0
            isLastPage = false
            barberList.clear()
            adapter.clearData()
            BarberRepository.getInstance().clearMainList()
            callBarberListData()
            callFavouriteBarberApi()
        }

        val appUpdater = AppUpdater(requireActivity())
        appUpdater.setUpdateFrom(UpdateFrom.GOOGLE_PLAY)
        appUpdater.start()
    }


    override fun onLowMemory() {
        super.onLowMemory()
    }

    private fun updateFavoriteStatus(barber: Barber) {
        val params = HashMap<String, String>()
        params["barber_id"] = barber.id.toString()
        params["favorite"] = if (barber.isFavorite.get()) "0" else "1"

        progressHUD.show()

        APIHandler(
            requireContext(),
            Request.Method.POST,
            Constants.API.favorite_barber,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()

                    Log.e(TAG, "onResult: $result")

                    viewModel.fetchList(
                        requireContext(),
                        currentLatLng,
                        isFavorite = true,
                        page = "1"
                    )
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)

                }
            })
    }

    private fun enableMyLocation() {

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
    }

    private fun hideKeyBoard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onSearchText(searchText: String) {
        Log.e(TAG, "onSearchText: $searchText")
        Handler(Looper.getMainLooper()).postDelayed({
            if (searchText.isNotEmpty()) {
                isLastPage = true
                filterList(searchText)
            } else {
                isLastPage = false
                adapter.clearData()
                adapter.setData(barberList)
            }
        }, 700)
    }

    private fun filterList(searchText: String) {

        progressHUD.show()
        val tempList = ArrayList<Barber>()
        for (item in barberList) {
            if (item.firstName.contains(searchText, true) || item.lastName.contains(
                    searchText,
                    true
                )
            ) {
                tempList.add(item)
            }
        }

        progressHUD.dismiss()

        if (!tempList.isNullOrEmpty()) {
            adapter.showSearchList(tempList)
        } else {
            adapter.clearData()
        }
    }

    override fun onItemClickListner(barber: Barber) {
        /*val intent = Intent(activity, BarberDetailActivity::class.java)
        intent.putExtra("barber_id", barber.id)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)*/

        val intent = Intent(activity, DetailsActivity::class.java)
        intent.putExtra("barber_id", barber.id)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
    }

    fun getNewLocBarberList(place: Place) {
        currentLatLng = place.latLng
        Log.e(TAG, "getNewLocBarberList: ")
    }

    override fun onLocFound(latLng: LatLng, context: Context) {
        Log.e(TAG, "onLocFound-*-*-: $latLng")
        contextt = context
        currentLatLng = latLng
        //currentLatLng= LatLng(38.888003, -77.081876)
        isFirstCall = false
        callBarberListData()
        BarberRepository.getInstance().clearMainList()
        viewModel.fetchList(contextt, currentLatLng, isFavorite = true, page = "1")
    }
}

