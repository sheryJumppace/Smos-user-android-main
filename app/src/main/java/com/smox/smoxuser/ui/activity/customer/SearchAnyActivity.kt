package com.smox.smoxuser.ui.activity.customer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import com.smox.smoxuser.App
import com.smox.smoxuser.R
import com.smox.smoxuser.data.BarberRepository
import com.smox.smoxuser.databinding.ActivitySearchAnyBinding
import com.smox.smoxuser.model.Barber
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.adapter.BarberSearchAdapter
import com.smox.smoxuser.utils.PaginationScrollListner
import com.smox.smoxuser.viewmodel.BarberListViewModel
import com.smox.smoxuser.viewmodel.BarberListViewModelFactory
import java.util.*


class SearchAnyActivity : BaseActivity(), BarberSearchAdapter.ItemClickListner {
    private val TAG = "SearchAnyActivity"
    lateinit var binding: ActivitySearchAnyBinding
    private lateinit var adapter: BarberSearchAdapter
    private lateinit var viewModel: BarberListViewModel
    private var barberList: ArrayList<Barber> = ArrayList()
    private val totalPage = 100
    private var pageStart = 0
    private var isLastPagee = false
    var isLoadingg = false
    var searchText = ""
    var currentLatLng: LatLng? = null
    var isFirstTime = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_any)
        val factory = BarberListViewModelFactory(BarberRepository.getInstance())
        viewModel = ViewModelProviders.of(this, factory).get(BarberListViewModel::class.java)

        BarberRepository.getInstance().clearMainList()

        binding.etSearchText.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                pageStart=0
                if (binding.etSearchText.text.length > 2) {
                    barberList.clear()
                    adapter.clearData()
                    searchText = binding.etSearchText.text.toString()
                    callBarberListData()
                }
                return@OnEditorActionListener true
            }
            false
        })

        currentLatLng = App.instance.myLocation

        adapter = BarberSearchAdapter(this, this)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvSearch.layoutManager = layoutManager
        binding.rvSearch.setHasFixedSize(false)
        binding.rvSearch.adapter = adapter

        viewModel.barbers.observe(this, Observer { barbers ->
            if (!isFirstTime) {
                progressHUD.dismiss()
                binding.bottomProgressBar.visibility = View.GONE
                if (barbers.isNotEmpty()) {
                    isLastPagee = false
                    barberList.addAll(barbers)
                    adapter.setData(barbers as ArrayList<Barber>)
                    adapter.notifyDataSetChanged()
                } else isLastPagee = true
            }
        })

        binding.rvSearch.addOnScrollListener(object : PaginationScrollListner(layoutManager) {
            override fun loadMoreItems() {
                if (!isLastPage) {
                    callBarberListData()
                }
            }

            override fun getTotalPageCount(): Int {
                return totalPage
            }

            override fun isLastPage(): Boolean {
                return isLastPagee
            }

            override fun isLoading(): Boolean {
                return isLoadingg
            }
        })

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
        binding.imgCloseSearch.setOnClickListener {
            pageStart=0
            hideKeyboard()
            binding.etSearchText.setText("")
            barberList.clear()
            adapter.clearData()
        }
    }


    private fun callBarberListData() {
        if (!isLastPagee) {
            isFirstTime=false
            isLoadingg = true
            pageStart += 1
            if (pageStart != 1) {
                binding.bottomProgressBar.visibility = View.VISIBLE
            }else{
                progressHUD.show()
            }
            Log.e("TAG", "loadMoreItems: called pageStart: $pageStart   isLastPage: $isLastPagee")

            viewModel.fetchList(
                this,
                page = "1",
                isFavorite = false,
                location = currentLatLng,
                query = searchText
            )
        }

    }

    override fun onItemClickListner(barber: Barber) {
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("barber_id", barber.id)
        startActivity(intent)
        overridePendingTransition(
            R.anim.activity_enter,
            R.anim.activity_exit
        )
    }
}