package com.smox.smoxuser.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ListItemAddressBinding
import com.smox.smoxuser.model.Address

class AddressAdapter: ListAdapter<Address, AddressAdapter.ViewHolder>(AddressDiffCallback()){

    private lateinit var context : Context
    private var onItemClickListener: ItemClickListener? = null
    private var lastCheckedPosition: Int = 0

    fun setItemClickListener(clickListener: ItemClickListener){
        onItemClickListener = clickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            ListItemAddressBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val address = getItem(position)
        holder.apply {
            bind(createOnClickListener(address, position), address)
            itemView.tag = address

            address.isSelected.set(position == lastCheckedPosition)
        }
    }

    private fun createOnClickListener(address: Address, position: Int): View.OnClickListener {
        return View.OnClickListener {
            if(it.id != R.id.imgAddressEdit && it.id != R.id.imgAddressDelete){
                address.isSelected.set(!address.isSelected.get())
                lastCheckedPosition = position
                notifyDataSetChanged();
            }
            onItemClickListener?.OnItemClick(it, position)
        }
    }

    class ViewHolder(
        private val binding: ListItemAddressBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(listner: View.OnClickListener, item: Address){
            binding.apply {
                clickListener = listner
                address = item
                executePendingBindings()
            }
        }
    }

    interface ItemClickListener {
        fun OnItemClick(view: View, position : Int)
    }

    private class AddressDiffCallback : DiffUtil.ItemCallback<Address>() {
        override fun areItemsTheSame(oldItem: Address, newItem: Address): Boolean {
            return oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Address, newItem: Address): Boolean {
            return oldItem == newItem
        }

    }

}