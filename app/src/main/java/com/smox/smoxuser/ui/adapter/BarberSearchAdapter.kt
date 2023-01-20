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
import com.smox.smoxuser.databinding.ItemVerticalRowBinding
import com.smox.smoxuser.model.Barber
import com.smox.smoxuser.utils.FULL_IMAGE_PATH
import java.util.*

class BarberSearchAdapter(var context: Context, itemClickListner: ItemClickListner) :
    RecyclerView.Adapter<BarberSearchAdapter.ViewHolder>() {

    var barber: ArrayList<Barber> = ArrayList()
    var itemClickListner: ItemClickListner? = null

    init {
        this.itemClickListner = itemClickListner
    }

    override fun getItemCount(): Int {
        return barber.size
    }

    inner class ViewHolder : RecyclerView.ViewHolder {
        var itemStylerViewBinding: ItemVerticalRowBinding? = null

        constructor(binding: ItemVerticalRowBinding) : super(binding.root) {
            itemStylerViewBinding = binding
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val binding: ViewDataBinding

        binding = DataBindingUtil.inflate<ItemVerticalRowBinding>(
            inflater,
            R.layout.item_vertical_row, viewGroup, false
        )

        return ViewHolder(binding as ItemVerticalRowBinding)
    }

    fun setData(barber: ArrayList<Barber>) {
        val startPos = this.barber.size
        this.barber.addAll(barber)
        notifyItemRangeInserted(startPos, barber.size)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val barber: Barber = barber[position]
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
                        .into(itemStylerViewBinding!!.imgBarberImage)
            }

            holder.itemView.setOnClickListener {
                itemClickListner!!.onItemClickListner(barber)
            }


        } catch (ex: Exception) {
        }
    }

    fun clearData() {
        this.barber.clear()
        notifyDataSetChanged()
    }

    fun showSearchList(searchList: ArrayList<Barber>) {
        this.barber.clear()
        this.barber.addAll(searchList)
        notifyDataSetChanged()
    }

    interface ItemClickListner {
        fun onItemClickListner(barber: Barber)
    }
}
