package com.smox.smoxuser.ui.activity.customer

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.smox.smoxuser.R
import com.smox.smoxuser.data.BarberRepository
import com.smox.smoxuser.databinding.ActivityFavoritesBinding
import com.smox.smoxuser.model.Barber
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.adapter.FavoriteAdapter
import com.smox.smoxuser.utils.PaginationScrollListner
import com.smox.smoxuser.utils.openKeyboard
import com.smox.smoxuser.viewmodel.BarberListViewModel
import com.smox.smoxuser.viewmodel.BarberListViewModelFactory
import kotlinx.android.synthetic.main.activity_login.*

class FavoritesActivity : BaseActivity() {
    private val TAG = "FavoritesActivity"

    lateinit var binding: ActivityFavoritesBinding
    private lateinit var viewModel: BarberListViewModel
    private lateinit var favoriteAdapter: FavoriteAdapter
    private var barberFavList: ArrayList<Barber> = ArrayList()
    private val totalPage = 100
    private var pageStart = 1
    private var isLastPagee = false
    var isLoadingg = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_favorites)
        val factory = BarberListViewModelFactory(BarberRepository.getInstance())
        viewModel = ViewModelProviders.of(this, factory).get(BarberListViewModel::class.java)

        favoriteAdapter = FavoriteAdapter(this, object : FavoriteAdapter.ItemClickListner {
            override fun onItemClickListner(barber: Barber) {
                val intent = Intent(this@FavoritesActivity, DetailsActivity::class.java)
                intent.putExtra("barber_id", barber.id)
                startActivity(intent)
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
            }
        }, object : FavoriteAdapter.ItemFavClick {
            override fun onRemoveFav(barber: Barber) {

            }
        })

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvFavorites.layoutManager = layoutManager
        binding.rvFavorites.adapter = favoriteAdapter

        viewModel.favBarbers.observe(this, Observer { barbers ->
            binding.bottomProgressBar.visibility = View.GONE
            progressHUD.dismiss()
            if (barbers.isNotEmpty()) {
                isLastPagee = false
                barberFavList.addAll(barbers)
            } else isLastPagee = true

            isLoadingg = false
            favoriteAdapter.setData(barbers as java.util.ArrayList<Barber>)
            if (pageStart==1&&barbers.isEmpty()){
                binding.rvFavorites.visibility=View.GONE
                binding.tvNoDataFound.visibility=View.VISIBLE
            }else{
                binding.rvFavorites.visibility=View.VISIBLE
                binding.tvNoDataFound.visibility=View.GONE
            }

        })

        binding.rvFavorites.addOnScrollListener(object : PaginationScrollListner(layoutManager) {
            override fun loadMoreItems() {
                if (!isLastPagee) {
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

        binding.imgSearch.setOnClickListener {
            binding.llTitleBar.visibility = View.GONE
            binding.llSearchBar.visibility = View.VISIBLE
            binding.etSearchText.requestFocus()
            openKeyboard(this)
        }
        binding.imgCloseSearch.setOnClickListener {
            binding.llTitleBar.visibility = View.VISIBLE
            binding.llSearchBar.visibility = View.GONE
            binding.etSearchText.setText("")
            hideKeyboard()
        }

        binding.etSearchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(
                searchText: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                Handler(Looper.getMainLooper()).postDelayed({
                    if (searchText!!.isNotEmpty()) {
                        isLastPagee = true
                        filterList(searchText.toString())
                    } else {
                        isLastPagee = false
                        favoriteAdapter.clearData()
                        favoriteAdapter.setData(barberFavList)
                    }
                }, 700)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun filterList(searchText: String) {
        progressHUD.show()
        val tempList = ArrayList<Barber>()
        for (item in barberFavList) {
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
            favoriteAdapter.showSearchList(tempList)
        } else {
            favoriteAdapter.clearData()
        }
    }

    override fun onResume() {
        super.onResume()
        progressHUD.show()
        isLastPagee=false
        pageStart=0
        favoriteAdapter.clearData()
        barberFavList.clear()
        BarberRepository.getInstance().clearMainList()
        callBarberListData()
    }

    private fun callBarberListData() {
        if (!this.isLastPagee) {
            isLoadingg = true
            pageStart += 1
            if (pageStart != 1) {
                binding.bottomProgressBar.visibility = View.VISIBLE
            }
            Log.e(TAG, "loadMoreItems: called pageStart: $pageStart   isLastPage: $isLastPagee")

            viewModel.fetchList(this, isFavorite = true, page = pageStart.toString())
        }

    }
}