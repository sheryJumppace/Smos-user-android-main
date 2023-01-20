package com.smox.smoxuser.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ListItemBarberMapBinding
import com.smox.smoxuser.model.Barber
import com.smox.smoxuser.utils.FULL_IMAGE_PATH
import java.util.*

class BarberMapAdapter(val context: Context,itemClickListner: ItemClickListner) :
    RecyclerView.Adapter<BarberMapAdapter.ViewHolder>() {

    var barber: ArrayList<Barber>? = ArrayList()
    var itemClickListner: ItemClickListner? = null

    init {
        this.itemClickListner = itemClickListner
    }

    override fun getItemCount(): Int {
        return if (barber == null) 0 else barber!!.size
    }

    inner class ViewHolder : RecyclerView.ViewHolder {
        var itemStylerViewBinding: ListItemBarberMapBinding? = null

        constructor(binding: ListItemBarberMapBinding) : super(binding.root) {
            itemStylerViewBinding = binding
        }
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val binding: ViewDataBinding

        binding = DataBindingUtil.inflate<ListItemBarberMapBinding>(
            inflater,
            R.layout.list_item_barber_map, viewGroup, false
        )

        return ViewHolder(binding)
    }

    fun setData(barber: ArrayList<Barber>) {
        this.barber = barber
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val barber: Barber = barber!![position]
            holder.apply {
                itemStylerViewBinding!!.barber = barber

                val options: RequestOptions =
                    RequestOptions().placeholder(R.drawable.small_placeholder)
                        .error(R.drawable.small_placeholder)

                val url= if (barber.image.startsWith("http"))
                    barber.image
                else
                    FULL_IMAGE_PATH + barber.image

                Glide.with(context)
                    .load(url)
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(itemStylerViewBinding!!.imgProfile)
            }

            holder.itemView.setOnClickListener {
                itemClickListner!!.onItemClickListner(barber)
            }

        } catch (ex: Exception) {
        }
    }

    interface ItemClickListner {
        fun onItemClickListner(barber: Barber)
    }
}
