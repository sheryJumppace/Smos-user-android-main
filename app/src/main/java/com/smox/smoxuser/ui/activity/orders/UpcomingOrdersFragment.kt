package com.smox.smoxuser.ui.activity.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.smox.smoxuser.databinding.FragmentUpcomingOrdersBinding
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.OrderItem
import com.smox.smoxuser.ui.adapter.OrderListAdapter
import com.smox.smoxuser.utils.PaginationScrollListner
import com.smox.smoxuser.utils.Prefrences
import com.smox.smoxuser.viewmodel.OrderViewModel
import kotlinx.android.synthetic.main.activity_chat.*


class UpcomingOrdersFragment : Fragment(), OrderListAdapter.OnOrderClickListner,
    OrderDetailBottomSheetFragment.OnCancelOrder {
    lateinit var binding: FragmentUpcomingOrdersBinding
    lateinit var orderViewModel: OrderViewModel
    lateinit var orderAdapter: OrderListAdapter
    var orderList = arrayListOf<OrderItem>()
    var page = 1
    private val totalPage = 100
    private var isLastPagee = false
    var isLoadingg = false
    var mainPos=0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUpcomingOrdersBinding.inflate(inflater, container, false)
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

        orderViewModel.orderItem.value= arrayListOf()
        setObservers()
        orderAdapter = OrderListAdapter(requireContext(), orderList, true, this)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvUpOrderList.layoutManager = layoutManager
        binding.rvUpOrderList.adapter = orderAdapter

        callOrderListApi()

        binding.rvUpOrderList.addOnScrollListener(object : PaginationScrollListner(layoutManager) {
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

    private fun callOrderListApi() {
        orderViewModel.getUpcomingOrders(
            requireContext(),
            (activity as OrdersActivity).progressBar,
            "upcoming", page
        )
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
                binding.rvUpOrderList.visibility = View.VISIBLE
                binding.txtNoOrders.visibility = View.GONE
            } else if (page == 1) {
                isLastPagee = true
                binding.rvUpOrderList.visibility = View.GONE
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

    override fun onTrackOrderClicked(pos: Int) {
        val bundle = Bundle()
        val gson = Gson()
        val orderItem = gson.toJson(orderList[pos])
        bundle.putString(Constants.API.ORDER_ITEM, orderItem)
        val fragment = TrackOrderFragment()
        fragment.arguments=bundle
        (activity as OrdersActivity).addFragment(fragment, isAddOrReplace = true, isAddToBackStack = true)
    }

    override fun onDetailClicked(pos: Int) {
        val orderDetail = OrderDetailBottomSheetFragment(orderList[pos], orderViewModel, this)
        orderDetail.show(requireActivity().supportFragmentManager, orderDetail.tag)
        mainPos=pos
    }

    override fun onOrderCancelled() {
        orderAdapter.removeItem(mainPos)
        Prefrences.saveBoolean(Constants.API.ORDER_CANCELLED, true)
    }
}