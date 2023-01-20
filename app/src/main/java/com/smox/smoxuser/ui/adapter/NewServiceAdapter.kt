package com.smox.smoxuser.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ServiceTypeItemRowBinding
import com.smox.smoxuser.model.Service
import com.smox.smoxuser.utils.FULL_IMAGE_PATH


class NewServiceAdapter(val context: Context,
    val list: ArrayList<Service>,
    val isSelectable: Boolean,
    val onServiceClicked: OnServiceClicked
) :
    androidx.recyclerview.widget.RecyclerView.Adapter<NewServiceAdapter.ViewHolder>() {

    var selectedPos = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ServiceTypeItemRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), isSelectable, parent
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.apply {
            bind(context,item, onServiceClicked, position)
        }
    }

    class ViewHolder(
        var binding: ServiceTypeItemRowBinding,
        var isSelect: Boolean,
        var parent: ViewGroup
    ) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(context: Context,item: Service, onServiceClicked: OnServiceClicked, position: Int) {
            binding.apply {
                this.service = item
                this.isSelectable = isSelect
            }

            val options: RequestOptions =
                RequestOptions().placeholder(R.drawable.ic_servive_sess)
                    .error(R.drawable.ic_servive_sess)

            val url= if (item.image.startsWith("http"))
                item.image
            else
                FULL_IMAGE_PATH + item.image

            Glide.with(context)
                .load(url)
                .apply(options)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.imgService)

            binding.imgRadio.setOnClickListener {
                item.isSelected.set(!item.isSelected.get())
                onServiceClicked.onServiceClicked(position)
            }
        }
    }

    interface OnServiceClicked {
        fun onServiceClicked(pos: Int)
    }
}

