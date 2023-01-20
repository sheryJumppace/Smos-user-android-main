package com.smox.smoxuser.ui.activity.product

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.kaopiz.kprogresshud.KProgressHUD
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ActivityProductsBinding
import com.smox.smoxuser.manager.Constants.API.BARBER_ID
import com.smox.smoxuser.manager.Constants.API.CALLED_FROM
import com.smox.smoxuser.manager.Constants.API.CART
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.viewmodel.AddressViewModel
import com.smox.smoxuser.viewmodel.ProductViewModel


class ProductsActivity : BaseActivity() {

    lateinit var binding: ActivityProductsBinding
    private var barberId: Int = 0
    lateinit var progressBar: KProgressHUD
    lateinit var imgCart: FrameLayout
    lateinit var txtTitle: TextView
    lateinit var imgSearch: ImageView
    lateinit var cartCount: TextView
    lateinit var productViewModel: ProductViewModel
    lateinit var addressViewModel: AddressViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_products)

        productViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)
        addressViewModel = ViewModelProvider(this).get(AddressViewModel::class.java)

        val calledFrom = intent.getStringExtra(CALLED_FROM)
        barberId = intent.getIntExtra(BARBER_ID, 0)

        productViewModel.barberId.value = barberId.toString()

        progressBar = progressHUD
        txtTitle = binding.txtTitle
        imgCart = binding.frameCart
        imgSearch = binding.imgSearch
        cartCount = binding.notiBadge

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val graphInflater = navHostFragment.navController.navInflater
        val navGraph = graphInflater.inflate(R.navigation.nav_graph)
        val navController = navHostFragment.navController

        val destination = if (calledFrom.equals(CART))
            R.id.cartBarberListFragment
        else
            R.id.productListFragment
        navGraph.startDestination = destination
        navController.graph = navGraph

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        Log.e("TAG", "onBackPressed: " + supportFragmentManager.backStackEntryCount)
        val count = supportFragmentManager.backStackEntryCount
        if (count == 0) {
            super.onBackPressed()
        } else {
            supportFragmentManager.popBackStack()
        }
    }

}