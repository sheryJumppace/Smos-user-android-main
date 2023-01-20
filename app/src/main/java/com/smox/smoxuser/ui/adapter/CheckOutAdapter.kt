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
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.smox.smoxuser.databinding.ListItemServiceCheckoutBinding
import com.smox.smoxuser.model.Service


class CheckOutAdapter (private val items: ArrayList<Service>):
    RecyclerView.Adapter<CheckOutAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val service = items[position]
        holder.apply {
            bind(service)
            itemView.tag = service
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemServiceCheckoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    class ViewHolder(
        private val binding: ListItemServiceCheckoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Service) {
            binding.apply {
                service = item
                executePendingBindings()
            }
        }
    }
}


