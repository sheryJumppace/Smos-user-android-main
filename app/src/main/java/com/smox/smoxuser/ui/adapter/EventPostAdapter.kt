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
import com.smox.smoxuser.databinding.ListItemEventPostBinding
import com.smox.smoxuser.model.Event


class EventPostAdapter internal constructor(eventAction:EventActions): ListAdapter<Event, EventPostAdapter.ViewHolder>(EventPostDiffCallback()) {
    var isEditable = false
    var eventAction:EventActions = eventAction

    interface ItemClickListener {
        fun onItemClick(view: View, appointmentId: Int)
    }
    private var onItemClickListener: ItemClickListener? = null

    fun setItemClickListener(clickListener: ItemClickListener) {
        onItemClickListener = clickListener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.apply {
            bind(createOnClickListener(item.id), item)
            itemView.tag = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemEventPostBinding.inflate(
                LayoutInflater.from(parent.context), parent, false),eventAction)
    }

    private fun createOnClickListener(appointmentId: Int): View.OnClickListener {
        return View.OnClickListener {
            onItemClickListener?.onItemClick(it, appointmentId)
        }
    }

    class ViewHolder(
        private val binding: ListItemEventPostBinding
   ,eventAction:EventActions ) : RecyclerView.ViewHolder(binding.root) {
        var eventAction:EventActions = eventAction
        fun bind(listener: View.OnClickListener, item: Event) {
            binding.apply {
                event = item
                executePendingBindings()
            }
            binding.ivDelete.setOnClickListener{

                eventAction.onDeleteClick(adapterPosition)

            }
            binding.ivEdit.setOnClickListener {
                eventAction.onEditClick(event = item ,pos =adapterPosition )

            }
        }

    }


    public interface EventActions {
        fun onDeleteClick(pos:Int)
        fun onEditClick(event: Event , pos:Int)

    }

}

private class EventPostDiffCallback : DiffUtil.ItemCallback<Event>() {

    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}

