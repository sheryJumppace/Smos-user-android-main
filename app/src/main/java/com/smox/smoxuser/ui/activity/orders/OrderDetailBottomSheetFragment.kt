package com.smox.smoxuser.ui.activity.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.OrderDetailBottomSheetBinding
import com.smox.smoxuser.model.OrderItem
import com.smox.smoxuser.ui.adapter.OrderDetailAdapter
import com.smox.smoxuser.ui.adapter.OrderListAdapter
import com.smox.smoxuser.viewmodel.OrderViewModel

class OrderDetailBottomSheetFragment(
    val orderItem: OrderItem,
    private val orderViewModel: OrderViewModel,
    private val onCancelOrder: OnCancelOrder
) : BottomSheetDialogFragment() {


    lateinit var binding: OrderDetailBottomSheetBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = OrderDetailBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        binding.priceInfo = orderItem
        orderViewModel.orderDetailItem.value?.clear()
        //orderViewModel.getOrderProductList(requireActivity(), orderItem.id.toString())
    }

    private fun initObserver() {
        orderViewModel.orderDetailItem.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                val orderAdapter = OrderDetailAdapter(requireContext(), it)
                /*binding.rvUpOrderList.layoutManager = LinearLayoutManager(requireContext())
                binding.rvUpOrderList.adapter = orderAdapter
                binding.myProgress.visibility=View.GONE
                binding.rvUpOrderList.visibility=View.VISIBLE*/
            }
        })

        orderViewModel.orderCancelled.observe(viewLifecycleOwner, Observer {
            if (it) {
                onCancelOrder.onOrderCancelled()
                orderViewModel.orderCancelled.value = false
                this.dismiss()
            }
        })

        binding.txtCancelOrder.setOnClickListener {
            AlertDialog.Builder(requireContext()).setTitle(getString(R.string.cancelOrder))
                .setCancelable(false).setMessage(getString(R.string.cancelOrderMsg))
                .setPositiveButton(getString(R.string.ok)) { _, _ ->

                    orderViewModel.cancelOrder(
                        requireActivity(),
                        (activity as OrdersActivity).progressBar,
                        orderItem.id.toString()
                    )
                }
                .setNegativeButton(getString(R.string.cancel)) { _, _ ->

                }.create().show()

        }
    }

    interface OnCancelOrder {
        fun onOrderCancelled()
    }
}