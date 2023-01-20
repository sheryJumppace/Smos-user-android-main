package com.smox.smoxuser.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ItemListCategoriesBinding
import com.smox.smoxuser.utils.listeners.OnItemClicked
import kotlinx.android.synthetic.main.item_list_categories.view.*


class CategoryNameAdapter(
    val context: Context,
    val onItemClicked: OnItemClicked,
    val list: ArrayList<String>
) :
    androidx.recyclerview.widget.RecyclerView.Adapter<CategoryNameAdapter.ViewHolder>() {

    var selectedPos = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.item_list_categories, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (selectedPos == position) {
            holder.binding.txtCatName.background = ContextCompat.getDrawable(context, R.drawable.round_corner)
            onItemClicked.onItemClick(position)
        } else {
            holder.binding.txtCatName.background = ContextCompat.getDrawable(context, R.drawable.round_cat)
        }
        holder.itemView.setOnClickListener {
            onItemClicked.onItemClick(position)
            selectedPos = position
            notifyDataSetChanged()
        }
        holder.binding.txtCatName.text = list[position]

    }

    class ViewHolder(itemView: View) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val binding: ItemListCategoriesBinding = DataBindingUtil.bind(itemView)!!
    }


}