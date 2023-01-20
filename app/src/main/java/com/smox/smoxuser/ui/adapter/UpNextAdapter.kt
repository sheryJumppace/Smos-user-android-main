package com.smox.smoxuser.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.smox.smoxuser.databinding.ListItemCustomerUpNextBinding
import com.smox.smoxuser.model.UpNext

class UpNextAdapter
    (private val upNexts: List<UpNext>) : BaseAdapter() {

    override fun getCount(): Int {
        return upNexts.size
    }

    override fun getItemId(position: Int): Long {
        return upNexts[position].id.toLong()
    }

    override fun getItem(position: Int): UpNext {
        return upNexts[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        if (convertView == null) {
            val binding = ListItemCustomerUpNextBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
            holder = ViewHolder(binding)
            holder.view.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        val  item = getItem(position)
        holder.bind( item)
        return holder.view
    }

    internal class ViewHolder(private val binding: ListItemCustomerUpNextBinding) {
        val view = binding.root
        fun bind(upNext: UpNext) {
            binding.apply {
                this.model = upNext
                executePendingBindings()
            }
        }
    }
}
