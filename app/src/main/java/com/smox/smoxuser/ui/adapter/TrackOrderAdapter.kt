package com.smox.smoxuser.ui.adapter

import android.content.Context
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.CartItemRowBinding
import com.smox.smoxuser.databinding.TrackOrderItemRowBinding
import com.smox.smoxuser.model.CartItems
import com.smox.smoxuser.model.TrackOrderStatus

class TrackOrderAdapter(val context: Context, var trackStatus: ArrayList<TrackOrderStatus>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<TrackOrderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context);
        val binding = TrackOrderItemRowBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        val trackItem = trackStatus[pos]
        holder.bind(trackItem, pos, context)
    }

    class ViewHolder(val binding: TrackOrderItemRowBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(trackStatus: TrackOrderStatus, pos: Int, context: Context) {
            binding.apply {
                txtStatusTitle.text = trackStatus.statusTitle
                txtStatusDate.text = trackStatus.statusDate
                txtStatusMsg.text = trackStatus.statusMsg

                when (pos) {
                    0 -> {
                        setStatusIcon(AppCompatResources.getDrawable(context,
                            R.drawable.ordered_yellow), context)
                    }
                    1 -> {
                        if (trackStatus.isCompleted) setStatusIcon(AppCompatResources.getDrawable(
                            context,
                            R.drawable.packed_yellow), context)
                        else setStatusIcon(AppCompatResources.getDrawable(context,
                            R.drawable.packed_grey), context)
                    }
                    2 -> {
                        if (trackStatus.isCompleted) setStatusIcon(AppCompatResources.getDrawable(
                            context,
                            R.drawable.out_delivery_yellow), context)
                        else setStatusIcon(AppCompatResources.getDrawable(context,
                            R.drawable.out_delivery_grey), context)
                    }
                    3 -> {
                        binding.view1.visibility = View.GONE
                        if (trackStatus.isCompleted) setStatusIcon(AppCompatResources.getDrawable(
                            context,
                            R.drawable.delivered_yellow), context)
                        else setStatusIcon(AppCompatResources.getDrawable(context,
                            R.drawable.delivered_grey), context)
                    }
                }
            }
        }

        private fun setStatusIcon(drawable: Drawable?, context: Context) {
            Glide.with(context).load(drawable).transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.imgStatus)
        }
    }

    override fun getItemCount(): Int {
        return trackStatus.size
    }
}