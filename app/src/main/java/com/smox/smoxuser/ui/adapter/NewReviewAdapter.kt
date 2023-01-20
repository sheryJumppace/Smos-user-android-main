package com.smox.smoxuser.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.smox.smoxuser.databinding.ServiceReviewItemRowBinding
import com.smox.smoxuser.model.Review


class NewReviewAdapter(
    val list: ArrayList<Review>
) :
    androidx.recyclerview.widget.RecyclerView.Adapter<NewReviewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //return ViewHolder(v)
        return ViewHolder(
            ServiceReviewItemRowBinding.inflate(
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
    }

    class ViewHolder(var binding: ServiceReviewItemRowBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Review) {
            binding.apply {
                this.review = item
            }
        }
    }


}