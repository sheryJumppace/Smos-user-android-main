/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smox.smoxuser.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smox.smoxuser.databinding.ListItemBarberBinding
import com.smox.smoxuser.model.Barber


class BarberAdapter : ListAdapter<Barber, BarberAdapter.ViewHolder>(BarberDiffCallback()) {
    interface ItemClickListener {
        fun onItemClick(view: View, barberId: Int)
    }
    private var onItemClickListener: ItemClickListener? = null

    fun setItemClickListener(clickListener: ItemClickListener) {
        onItemClickListener = clickListener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val barber = getItem(position)
        holder.apply {
            bind(createOnClickListener(barber.id), barber)
            itemView.tag = barber
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemBarberBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    private fun createOnClickListener(barberId: Int): View.OnClickListener {
        return View.OnClickListener {
            onItemClickListener?.onItemClick(it, barberId)
        }
    }

    class ViewHolder(
        private val binding: ListItemBarberBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listener: View.OnClickListener, item: Barber) {
            binding.apply {
                clickListener = listener
                barber = item
                executePendingBindings()
            }
        }
    }
}

private class BarberDiffCallback : DiffUtil.ItemCallback<Barber>() {

    override fun areItemsTheSame(oldItem: Barber, newItem: Barber): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Barber, newItem: Barber): Boolean {
        return oldItem == newItem
    }
}

