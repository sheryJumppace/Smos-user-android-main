package com.smox.smoxuser.ui.activity.orders

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.kaopiz.kprogresshud.KProgressHUD
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ActivityOrdersBinding
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.activity.home.Home2Activity
import com.smox.smoxuser.ui.activity.product.CartBarberListFragment
import com.smox.smoxuser.ui.adapter.ViewPagerAdapter
import com.smox.smoxuser.ui.fragment.customer.*
import com.smox.smoxuser.viewmodel.AddressViewModel
import com.smox.smoxuser.viewmodel.OrderViewModel

class OrdersActivity : BaseActivity() {
    lateinit var binding: ActivityOrdersBinding
    lateinit var txtTitle: TextView
    lateinit var orderViewmodel:OrderViewModel
    lateinit var imgSearch:ImageView
    lateinit var progressBar: KProgressHUD
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_orders)

        orderViewmodel = ViewModelProvider(this).get(OrderViewModel::class.java)
        txtTitle = binding.txtTitle
        imgSearch=binding.imgSearch
        progressBar = progressHUD
        val fragment = OrderListFragment()
        addFragment(fragment, isAddOrReplace = false, isAddToBackStack = false)
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    fun addFragment(fragment: Fragment, isAddOrReplace: Boolean, isAddToBackStack: Boolean) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        if (!isAddOrReplace)
            transaction.add(
                R.id.frameOrderContainer, fragment, fragment.javaClass.name
            ) else transaction.replace(R.id.frameOrderContainer, fragment, fragment.javaClass.name)

        if (isAddToBackStack)
            transaction.addToBackStack(fragment.javaClass.name)

        transaction.commit()
    }

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount
        if (count == 0) {
            super.onBackPressed()
        } else {
            supportFragmentManager.popBackStack()
        }
    }

}