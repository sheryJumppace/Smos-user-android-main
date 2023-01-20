package com.smox.smoxuser.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.AddressItemRowBinding
import com.smox.smoxuser.databinding.CartBarberRowBinding
import com.smox.smoxuser.model.AddressResponse
import com.smox.smoxuser.model.CartBarberItem


class AddressListAdapter(
    val context: Context,
    var addressList: ArrayList<AddressResponse.AddressData>,
    var clickListener: OnAddressSelect
) : androidx.recyclerview.widget.RecyclerView.Adapter<AddressListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context);
        val binding = AddressItemRowBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {

        val addressItem=addressList[pos]

        holder.binding.txtAddress.text=addressItem.defaultAddress()
        holder.binding.radioButton.isChecked= addressItem.make_default == "1"
        holder.binding.imgDelete.visibility=if (addressItem.make_default == "1") View.GONE else View.VISIBLE

        holder.itemView.setOnClickListener {
            clickListener.onAddressSelected(pos)
        }
        holder.binding.imgDelete.setOnClickListener {
            clickListener.onDeleteSelected(pos)
        }
        holder.binding.imgEdit.setOnClickListener {
            clickListener.onEditSelected(pos)
        }
        holder.binding.radioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                clickListener.onAddressSelected(pos)
        }

    }

    class ViewHolder(val binding: AddressItemRowBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
    }

    override fun getItemCount(): Int {
        return addressList.size
    }

    interface OnAddressSelect {
        fun onAddressSelected(pos: Int)
        fun onDeleteSelected(pos: Int)
        fun onEditSelected(pos: Int)

    }
}