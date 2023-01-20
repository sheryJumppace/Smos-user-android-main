package com.smox.smoxuser.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.smox.smoxuser.databinding.ListItemCustomerMenuBinding
import com.smox.smoxuser.model.CustomMenu

class CustomMenuAdapter(private val products: List<CustomMenu>) : BaseAdapter() {

    override fun getCount(): Int {
        return products.size
    }

    override fun getItemId(position: Int): Long {
        return products[position].icon.toLong()
    }

    override fun getItem(position: Int): CustomMenu {
        return products[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        if (convertView == null) {
            val binding = ListItemCustomerMenuBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
            holder = ViewHolder(binding)
            holder.view.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        val  product = getItem(position)
        holder.bind( product)
        return holder.view
    }

    internal class ViewHolder(private val binding: ListItemCustomerMenuBinding) {
        val view = binding.root
        fun bind(customMenu: CustomMenu) {
            binding.apply {
                this.menu = customMenu
                executePendingBindings()
            }
        }
    }
}
