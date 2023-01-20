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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smox.smoxuser.databinding.ListItemTimeSlotBinding


class TimeSlotAdapter : ListAdapter<String, TimeSlotAdapter.ViewHolder>(TimeSlotDiffCallback()) {
    var selectedPosition = -1
    private lateinit var context:Context
    interface ItemClickListener {
        fun onItemClick(view: View, slot: String)
    }
    private var onItemClickListener: ItemClickListener? = null

    fun setItemClickListener(clickListener: ItemClickListener) {
        onItemClickListener = clickListener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val slot = getItem(position)
        holder.apply {
            bind(createOnClickListener(slot, position), slot, position == selectedPosition)
            itemView.tag = slot
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(ListItemTimeSlotBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    private fun createOnClickListener(slot: String, position: Int): View.OnClickListener {
        return View.OnClickListener {
            selectedPosition = position
            onItemClickListener?.onItemClick(it, slot)
            notifyDataSetChanged()
        }
    }

    class ViewHolder(
        private val binding: ListItemTimeSlotBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listener: View.OnClickListener, item: String, isSelected: Boolean) {
            binding.apply {
                clickListener = listener
                time = item
                txtTime.isSelected = isSelected
                executePendingBindings()
            }
        }
    }
}

private class TimeSlotDiffCallback : DiffUtil.ItemCallback<String>() {

    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}

