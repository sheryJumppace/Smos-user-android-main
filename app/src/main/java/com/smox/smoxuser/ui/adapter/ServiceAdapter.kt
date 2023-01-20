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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smox.smoxuser.databinding.ListItemServiceBinding
import com.smox.smoxuser.model.Service

class ServiceAdapter(
    private val isSmall: Boolean,
    private val isSelect: Boolean = false,
    private val isEdit: Boolean = false
) : ListAdapter<Service, ServiceAdapter.ViewHolder>(ServiceDiffCallback()) {
    private lateinit var context: Context
    private var onItemClickListener: ItemClickListener? = null

    fun setItemClickListener(clickListener: ItemClickListener) {
        onItemClickListener = clickListener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val service = getItem(position)
        holder.apply {
            bind(createOnClickListener(service,position), service)
            itemView.tag = service
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            ListItemServiceBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), isSmall, isSelect, isEdit
        )
    }

    private fun createOnClickListener(
        service: Service,
        position: Int
    ): View.OnClickListener {
        return View.OnClickListener {
            if (isSelect) {
                service.isSelected.set(!service.isSelected.get())
            } else {
                onItemClickListener?.onItemClick(it, service.id,position)
            }
        }
    }

    class ViewHolder(
        private val binding: ListItemServiceBinding,
        private val small: Boolean,
        private val select: Boolean,
        private val edit: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(listener: View.OnClickListener, item: Service) {
            binding.apply {
                clickListener = listener
                service = item
                this.isSmall = small
                this.isSelect = select
                isEdit = edit
                executePendingBindings()
            }
        }
    }

    interface ItemClickListener {
        fun onItemClick(view: View, serviceId: Int, position: Int)
    }
}

private class ServiceDiffCallback : DiffUtil.ItemCallback<Service>() {

    override fun areItemsTheSame(oldItem: Service, newItem: Service): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Service, newItem: Service): Boolean {
        return oldItem == newItem
    }
}

