package com.smox.smoxuser.ui.adapter

import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.smox.smoxuser.databinding.CartItemRowBinding
import com.smox.smoxuser.databinding.MyOrderItemRowBinding
import com.smox.smoxuser.model.CartItems
import com.smox.smoxuser.model.OrderItem


class OrderListAdapter(
    val context: Context,
    var orderList: ArrayList<OrderItem>,
    var isUpcoming:Boolean,
    var clickListener: OnOrderClickListner
) : androidx.recyclerview.widget.RecyclerView.Adapter<OrderListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context);
        val binding =  MyOrderItemRowBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {

        val orderItem = orderList[pos]
        holder.binding.txtViewItems.setOnClickListener {
            clickListener.onDetailClicked(pos)
        }

        if (!isUpcoming) {
            holder.binding.txtTrackOrder.visibility = View.GONE
            holder.binding.txtOrderStatus.visibility=View.VISIBLE
        }
        else {
            holder.binding.txtTrackOrder.visibility = View.VISIBLE
            holder.binding.txtOrderStatus.visibility=View.GONE
        }

        holder.binding.txtTrackOrder.setOnClickListener {
            clickListener.onTrackOrderClicked(pos)
        }
        holder.binding.orderItem=orderItem
    }

    class ViewHolder(val binding: MyOrderItemRowBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bind(orderItem: OrderItem) {
            binding.apply {
                binding.orderItem=orderItem
            }
        }
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    fun addData(newList:ArrayList<OrderItem>) {
        val oldSize=orderList.size
        orderList.addAll(newList)
        notifyItemRangeInserted(oldSize, orderList.size)
    }

    fun clearData(newList:ArrayList<OrderItem>) {
        orderList.clear()
        orderList.addAll(newList)
        notifyDataSetChanged()
    }

    fun removeItem(pos: Int) {
        orderList.removeAt(pos)
        notifyDataSetChanged()
    }

    interface OnOrderClickListner {
        fun onTrackOrderClicked(pos: Int)
        fun onDetailClicked(pos: Int)
    }

}