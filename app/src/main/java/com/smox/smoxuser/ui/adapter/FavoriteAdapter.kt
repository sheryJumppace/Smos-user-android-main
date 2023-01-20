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
import com.smox.smoxuser.databinding.ItemHorizontalRowBinding
import com.smox.smoxuser.model.Barber
import com.smox.smoxuser.utils.FULL_IMAGE_PATH
import java.util.*

class FavoriteAdapter(
    var context: Context,
    var itemClickListner: ItemClickListner,
    var itemFavClick: ItemFavClick
) :
    RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {

    var barber: ArrayList<Barber>? = ArrayList()

    override fun getItemCount(): Int {
        return if (barber == null) 0 else barber!!.size
    }

    inner class ViewHolder : RecyclerView.ViewHolder {
        var itemFavoriteListBinding: ItemHorizontalRowBinding? = null

        constructor(binding: ItemHorizontalRowBinding) : super(binding.root) {
            itemFavoriteListBinding = binding
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val binding: ViewDataBinding

        binding = DataBindingUtil.inflate<ItemHorizontalRowBinding>(
            inflater,
            R.layout.item_horizontal_row, viewGroup, false
        )

        return ViewHolder(binding as ItemHorizontalRowBinding)
    }

    fun setData(barber: ArrayList<Barber>) {
        val startPos = this.barber!!.size
        this.barber!!.addAll(barber)
        if (this.barber!!.size > 1)
            notifyItemRangeInserted(startPos, barber.size)
        else
            notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val barber: Barber = barber!![position]
            holder.apply {
                itemFavoriteListBinding!!.barber = barber

                val options: RequestOptions =
                    RequestOptions().placeholder(R.drawable.big_placeholder)
                        .error(R.drawable.big_placeholder)

                val url = if (barber.image.startsWith("http"))
                    barber.image
                else
                    FULL_IMAGE_PATH + barber.image


                Glide.with(context)
                    .load(url)
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(itemFavoriteListBinding!!.imgFavImage)

                itemFavoriteListBinding!!.btnFavorite.setOnClickListener {
                    itemFavClick.onRemoveFav(barber)
                }
            }
            holder.itemView.setOnClickListener {
                itemClickListner.onItemClickListner(barber)
            }

        } catch (ex: Exception) {
        }
    }

    fun clearData() {
        this.barber!!.clear()
        notifyDataSetChanged()
    }

    fun showSearchList(searchList: ArrayList<Barber>) {
        this.barber!!.clear()
        this.barber!!.addAll(searchList)
        notifyDataSetChanged()
    }

    interface ItemClickListner {
        fun onItemClickListner(barber: Barber)
    }

    interface ItemFavClick {
        fun onRemoveFav(barber: Barber)
    }
}
