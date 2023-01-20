package com.smox.smoxuser.ui.activity.orders

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayout
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.FragmentOrderListBinding
import com.smox.smoxuser.databinding.FragmentReviewBinding
import com.smox.smoxuser.model.type.AppointmentType
import com.smox.smoxuser.model.type.OrderType
import com.smox.smoxuser.ui.activity.product.ProductsActivity
import com.smox.smoxuser.ui.adapter.ViewPagerAdapter
import com.smox.smoxuser.ui.fragment.customer.*
import kotlinx.android.synthetic.main.fragment_appointments.*

class OrderListFragment : Fragment() {

    lateinit var binding: FragmentOrderListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOrderListBinding.inflate(inflater, container, false)
        (activity as OrdersActivity).imgSearch.visibility=View.GONE
        (activity as OrdersActivity).txtTitle.text = "My Orders"
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPagerAdapter()
    }

    private fun setPagerAdapter() {
        val adapter = ViewPagerAdapter(childFragmentManager)
        val upcomingFragment = UpcomingOrdersFragment()
        val pastFragment = PastOrdersFragment()

        adapter.addFragment(upcomingFragment, getString(R.string.upcomingOrders))
        adapter.addFragment(pastFragment, getString(R.string.pastOrders))

        binding.pager1.adapter = adapter
       // binding.pager1.offscreenPageLimit = 2
        tabLayout.setupWithViewPager(binding.pager1)
    }
}