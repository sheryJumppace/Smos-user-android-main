package com.smox.smoxuser.ui.fragment.orders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ListItemOrdersBinding
import com.smox.smoxuser.model.Orders
import java.util.ArrayList

class MyOrdersAdapter(clickListener: ItemClickListener): RecyclerView.Adapter<MyOrdersAdapter.ViewHolder>(){
    var  orderList: ArrayList<Orders>? = ArrayList()
    private var onItemClickListener: ItemClickListener? = null

    init {
        this.onItemClickListener = clickListener
    }

    override fun getItemCount(): Int {
        return if (orderList == null) 0 else orderList!!.size
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyOrdersAdapter.ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val binding: ViewDataBinding

        binding = DataBindingUtil.inflate<ListItemOrdersBinding>(
            inflater,
            R.layout.list_item_orders, viewGroup, false
        )

        return ViewHolder(binding as ListItemOrdersBinding)
    }

    fun setData(OrderList:ArrayList<Orders>) {
        orderList!!.addAll(OrderList)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyOrdersAdapter.ViewHolder, position: Int) {
        try {
            val order: Orders = orderList!![position]
            holder.listItemOrdersBinding!!.orders= order
        }catch (ex:Exception){
            ex.printStackTrace()
        }
    }

    interface ItemClickListener {
        fun OnItemClick(view: View, position : Int)
    }

    inner class ViewHolder : RecyclerView.ViewHolder {
        var listItemOrdersBinding: ListItemOrdersBinding? = null
        constructor(binding: ListItemOrdersBinding) : super(binding.root) {
            listItemOrdersBinding = binding
        }
    }


}