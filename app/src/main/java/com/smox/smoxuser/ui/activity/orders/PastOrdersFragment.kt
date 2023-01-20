package com.smox.smoxuser.ui.activity.orders

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.FragmentPastOrdersBinding
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.OrderItem
import com.smox.smoxuser.ui.adapter.OrderListAdapter
import com.smox.smoxuser.utils.PaginationScrollListner
import com.smox.smoxuser.utils.Prefrences
import com.smox.smoxuser.utils.shortToast
import com.smox.smoxuser.viewmodel.OrderViewModel

class PastOrdersFragment : Fragment(), OrderListAdapter.OnOrderClickListner,
    OrderDetailBottomSheetFragment.OnCancelOrder {

    lateinit var binding:FragmentPastOrdersBinding
    lateinit var orderViewModel: OrderViewModel
    lateinit var orderAdapter: OrderListAdapter
    var orderList = arrayListOf<OrderItem>()
    var page = 1
    private val totalPage = 100
    private var isLastPagee = false
    var isLoadingg = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentPastOrdersBinding.inflate(inflater, container, false)
        orderViewModel = ViewModelProvider(this).get(OrderViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            page = 1
            callOrderListApi()
        }

        setObservers()
        orderAdapter = OrderListAdapter(requireContext(), orderList, false,this)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvPastOrderList.layoutManager = layoutManager
        binding.rvPastOrderList.adapter = orderAdapter

        callOrderListApi()

        binding.rvPastOrderList.addOnScrollListener(object : PaginationScrollListner(layoutManager) {
            override fun loadMoreItems() {
                if (!isLastPage) {
                    page++
                    isLoadingg = true
                    callOrderListApi()
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

    }


    private fun setObservers() {
        orderViewModel.orderItem.observe(viewLifecycleOwner, Observer {
            isLoadingg = false
            if (it.isNotEmpty()) {
                isLastPagee = false
                if (page == 1)
                    orderAdapter.clearData(it)
                else
                    orderAdapter.addData(it)
                binding.rvPastOrderList.visibility = View.VISIBLE
                binding.txtNoOrders.visibility = View.GONE
            } else if (page == 1) {
                isLastPagee = true
                binding.rvPastOrderList.visibility = View.GONE
                binding.txtNoOrders.visibility = View.VISIBLE
            } else {
                isLastPagee = true
                if (isResumed)
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        "All orders found.",
                        Snackbar.LENGTH_LONG
                    ).show()
            }
        })
    }

    private fun callOrderListApi() {
        orderViewModel.getUpcomingOrders(
            requireContext(),
            (activity as OrdersActivity).progressBar,
            "past", page
        )
    }

    override fun onResume() {
        super.onResume()
        if (Prefrences.getBoolean(Constants.API.ORDER_CANCELLED)) {
            page = 1
            callOrderListApi()
            Prefrences.saveBoolean(Constants.API.ORDER_CANCELLED, false)
        }
    }

    override fun onTrackOrderClicked(pos: Int) {

    }

    override fun onDetailClicked(pos: Int) {
        val orderDetail = OrderDetailBottomSheetFragment(orderList[pos], orderViewModel, this)
        orderDetail.show(requireActivity().supportFragmentManager, orderDetail.tag)
    }

    override fun onOrderCancelled() {

    }
}