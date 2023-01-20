package com.smox.smoxuser.ui.adapter

import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.smox.smoxuser.databinding.CartItemRowBinding
import com.smox.smoxuser.model.CartItems


class CartListAdapter(
    val context: Context,
    var cartList: ArrayList<CartItems>,
    var clickListener: updateQtyClickListner
) : androidx.recyclerview.widget.RecyclerView.Adapter<CartListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context);
        val binding = CartItemRowBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {

        val cartItem = cartList[pos]
        Log.e("TAG", "onBindViewHolder: $pos")
        holder.bind(cartItem)

        holder.binding.imgRemoveQty.setOnClickListener {
            clickListener.onRemoveQuantity(pos)
        }

        holder.binding.imgAddQty.setOnClickListener {
            clickListener.onAddQuantity(pos)
        }

        holder.binding.imgDelete.setOnClickListener {
            clickListener.onDeletCartItem(pos)
        }

    }

    class ViewHolder(val binding: CartItemRowBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItems: CartItems) {
            binding.apply {
                binding.cartItem=cartItems
                binding.txtPrice.paintFlags= Paint.STRIKE_THRU_TEXT_FLAG
            }
        }
    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    interface updateQtyClickListner {
        fun onAddQuantity(pos: Int)
        fun onRemoveQuantity(pos: Int)
        fun onDeletCartItem(pos: Int)
    }
}