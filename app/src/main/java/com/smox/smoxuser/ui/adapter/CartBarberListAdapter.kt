package com.smox.smoxuser.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.CartBarberRowBinding
import com.smox.smoxuser.model.CartBarberItem


class CartBarberListAdapter(
    val context: Context,
    var cartBarberList: ArrayList<CartBarberItem>,
    var clickListener: OnCartBarberSelect
) : androidx.recyclerview.widget.RecyclerView.Adapter<CartBarberListAdapter.ViewHolder>() {

    val options: RequestOptions =
        RequestOptions().placeholder(R.drawable.small_placeholder)
            .error(R.drawable.small_placeholder)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context);
        val binding = CartBarberRowBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {


        holder.binding.txtBarberName.text =
            cartBarberList[pos].firstName + " " + cartBarberList[pos].lastName
        holder.binding.txtCount.text =
            context.getString(R.string.format_two_digit, cartBarberList[pos].cartCount)


        Glide.with(context)
            .load(cartBarberList[pos].image)
            .apply(options)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.binding.imgBarberImage)

        holder.itemView.setOnClickListener {
            clickListener.onCartBarberSelected(pos)
        }

    }

    class ViewHolder(val binding: CartBarberRowBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
    }

    override fun getItemCount(): Int {
        return cartBarberList.size
    }

    interface OnCartBarberSelect {
        fun onCartBarberSelected(pos: Int)
    }
}