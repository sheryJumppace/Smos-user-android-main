package com.smox.smoxuser.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.smox.smoxuser.databinding.ListItemCustomerMenuBinding
import com.smox.smoxuser.model.CustomMenu
import com.smox.smoxuser.utils.listeners.OnItemClicked


class NewSideMenuAdapter(
    val list: ArrayList<CustomMenu>, val onItemClicked: OnItemClicked
) :
    androidx.recyclerview.widget.RecyclerView.Adapter<NewSideMenuAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //return ViewHolder(v)
        return ViewHolder(
            ListItemCustomerMenuBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.apply {
            bind(item)
        }
        holder.itemView.setOnClickListener {
            onItemClicked.onItemClick(position)
        }
    }

    class ViewHolder(var binding: ListItemCustomerMenuBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CustomMenu) {
            binding.apply {
                this.menu = item
            }
        }
    }

}