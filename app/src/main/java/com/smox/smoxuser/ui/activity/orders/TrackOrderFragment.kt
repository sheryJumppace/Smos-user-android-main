package com.smox.smoxuser.ui.activity.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.smox.smoxuser.databinding.FragmentTrackOrderBinding
import com.smox.smoxuser.manager.Constants.API.ORDER_ITEM
import com.smox.smoxuser.model.OrderItem
import com.smox.smoxuser.model.TrackOrderStatus
import com.smox.smoxuser.ui.adapter.TrackOrderAdapter

class TrackOrderFragment : Fragment() {
    lateinit var binding: FragmentTrackOrderBinding
    lateinit var orderItem: OrderItem
    var trackOrderList = arrayListOf<TrackOrderStatus>()
    lateinit var trackOrderAdapter: TrackOrderAdapter
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentTrackOrderBinding.inflate(inflater, container, false)
        (activity as OrdersActivity).imgSearch.visibility = View.GONE
        (activity as OrdersActivity).txtTitle.text = "Track Order"
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderItem = Gson().fromJson(arguments?.getString(ORDER_ITEM), OrderItem::class.java)
        binding.orderItem = orderItem
        createTempList()
        setAdapter()


    }

    private fun setAdapter() {
        trackOrderAdapter = TrackOrderAdapter(requireContext(), trackOrderList)
        binding.rvTrackOrder.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTrackOrder.adapter = trackOrderAdapter
    }

    private fun createTempList() {
        trackOrderList.add(TrackOrderStatus("Order Places",
            orderItem.getOrderOnlyDate(),
            "Your order has been placed",
            true))
        trackOrderList.add(TrackOrderStatus("Packed & Shipped",
            orderItem.getOrderShipDate(),
            "Your item is shipped",
            true))
        trackOrderList.add(TrackOrderStatus("Out for Delivery",
            orderItem.getDeliveryDate(),
            "Your order is out for delivery",
            true))
        trackOrderList.add(/*TrackOrderStatus(
                "Delivered",
                "Jun 20, 2021",
                "Your order is delivered",
                false
            )*/
            TrackOrderStatus("Delivered", "", "", false)

        )
    }
}