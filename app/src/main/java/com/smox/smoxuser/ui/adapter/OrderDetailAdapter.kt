package com.smox.smoxuser.ui.adapter

import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.smox.smoxuser.databinding.CartItemRowBinding
import com.smox.smoxuser.databinding.MyOrderItemRowBinding
import com.smox.smoxuser.databinding.OrderDetailItemRowBinding
import com.smox.smoxuser.model.CartItems
import com.smox.smoxuser.model.OrderDetailItem
import com.smox.smoxuser.model.OrderItem


class OrderDetailAdapter(
    val context: Context,
    var orderList: ArrayList<OrderDetailItem>
) : androidx.recyclerview.widget.RecyclerView.Adapter<OrderDetailAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context);
        val binding =  OrderDetailItemRowBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {

        val orderItem = orderList[pos]
        Log.e("TAG", "onBindViewHolder: $pos")
        holder.bind(orderItem)
    }

    class ViewHolder(val binding: OrderDetailItemRowBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bind(orderItem: OrderDetailItem) {
            binding.apply {
                binding.order=orderItem
                binding.txtDiscPrice.paintFlags= Paint.STRIKE_THRU_TEXT_FLAG
            }
        }
    }

    override fun getItemCount(): Int {
        return orderList.size
    }
}