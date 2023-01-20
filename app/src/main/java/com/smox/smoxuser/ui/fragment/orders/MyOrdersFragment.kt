package com.smox.smoxuser.ui.fragment.orders

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.smox.smoxuser.data.OrdersRepository
import com.smox.smoxuser.databinding.FragmentMyOrdersBinding
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Orders
import com.smox.smoxuser.ui.fragment.BaseFragment
import com.smox.smoxuser.viewmodel.OrdersListViewModel
import com.smox.smoxuser.viewmodel.OrdersListViewModelFactory

class MyOrdersFragment : BaseFragment() {

    private lateinit var binding: FragmentMyOrdersBinding
    private lateinit var viewModel: OrdersListViewModel
    private lateinit var listOrders: ArrayList<Orders>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMyOrdersBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = OrdersListViewModelFactory(OrdersRepository.getInsance())
        viewModel = ViewModelProvider(this, factory).get(OrdersListViewModel::class.java)

        val myOrdersAdapter = MyOrdersAdapter(object : MyOrdersAdapter.ItemClickListener {
            override fun OnItemClick(view: View, position: Int) {

            }

        })

        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recMyOrders.layoutManager = layoutManager
        binding.recMyOrders.adapter = myOrdersAdapter

        viewModel.productOrderes.observe(this, Observer { productOrders ->
            binding!!.progressBar.visibility= View.GONE
            listOrders = ArrayList()
            if (productOrders != null) {
                if (productOrders.isNotEmpty()) {
                    listOrders = productOrders
                    myOrdersAdapter.setData(listOrders)
                    binding.recMyOrders.visibility = View.VISIBLE
                    binding.txtNoOrders.visibility = View.GONE
                } else {
                    binding.recMyOrders.visibility = View.GONE
                    binding.txtNoOrders.visibility = View.VISIBLE
                }
            }

        })

        viewModel.productStatus.observe(this, Observer { productStatus ->
            val status: String = productStatus
            Log.i("-status-", status)
            fetchOrderList()
        })

        fetchOrderList()

    }



    private fun fetchOrderList() {
        binding!!.progressBar.visibility= View.VISIBLE
        viewModel.fetchList(activity!!, Constants.API.myOrders)
    }
}
