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

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ListItemOpenHoursBinding
import com.smox.smoxuser.model.OpenDay

class OpenHourAdapter : ListAdapter<OpenDay, OpenHourAdapter.ViewHolder>(OpenHourDiffCallback()) {
    private lateinit var context:Context
    private var onItemClickListener: ItemClickListener? = null

    fun setItemClickListener(clickListener: ItemClickListener) {
        onItemClickListener = clickListener
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = getItem(position)
       /* holder.apply {
            bind(createOnClickListener(position),
                //createOnChangeListener(position),
                contact)
            itemView.tag = contact
        }*/
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            ListItemOpenHoursBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    private fun createOnClickListener(position: Int): View.OnClickListener {
        return View.OnClickListener {
            val isStart = it.id == R.id.txtStartTime
            onItemClickListener?.onItemClick(it, position, isStart)
        }
    }


    class ViewHolder(
        private val binding: ListItemOpenHoursBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(listener: View.OnClickListener, switchListener: SwitchCompat, openHour: OpenDay) {
            binding.apply {
                clickListener = listener
                //switchButton.setOnCheckedChangeListener(switchListener)
                item = openHour
                executePendingBindings()
            }
        }
    }

    interface ItemClickListener {
        fun onItemClick(view: View, position: Int, isStart:Boolean)
        fun onDayChange(view: View, position: Int, isOpen:Boolean)
    }
}

private class OpenHourDiffCallback : DiffUtil.ItemCallback<OpenDay>() {

    override fun areItemsTheSame(oldItem: OpenDay, newItem: OpenDay): Boolean {
        return oldItem.day == newItem.day
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: OpenDay, newItem: OpenDay): Boolean {
        return oldItem == newItem
    }
}

