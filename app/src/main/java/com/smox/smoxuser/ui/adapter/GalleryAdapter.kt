package com.smox.smoxuser.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ServiceGalleryItemBinding


class GalleryAdapter(
    val context: Context,
    val list: ArrayList<String>,
    val onGalleryItemClicked: OnGalleryItemClicked
) :
    androidx.recyclerview.widget.RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    var selectedPos = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ServiceGalleryItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), parent
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.apply {
            bind(context, item)
        }

        holder.itemView.setOnClickListener {
            onGalleryItemClicked.onGalleryItemClicked(position)
        }
    }

    class ViewHolder(
        var binding: ServiceGalleryItemBinding,
        var parent: ViewGroup
    ) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(context: Context, item: String) {

            val options: RequestOptions =
                RequestOptions().placeholder(R.drawable.big_placeholder)
                    .error(R.drawable.big_placeholder)

            Glide.with(context)
                .load(item)
                .apply(options)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.galleryImg)

        }
    }

    interface OnGalleryItemClicked {
        fun onGalleryItemClicked(pos: Int)
    }
}

