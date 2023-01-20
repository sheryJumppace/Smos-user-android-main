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
import com.smox.smoxuser.databinding.ListItemVerifyBinding
import com.smox.smoxuser.model.VerifyField


class VerifyAdapter (private val items: ArrayList<VerifyField>):
    RecyclerView.Adapter<VerifyAdapter.ViewHolder>() {
    fun getVerifyFields(): ArrayList<VerifyField> {
        return items
    }
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val verifyField = items[position]
        holder.apply {
            bind(verifyField)
            itemView.tag = verifyField
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemVerifyBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    class ViewHolder(
        private val binding: ListItemVerifyBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(verifyField: VerifyField) {
            binding.apply {
                this.item = verifyField
                executePendingBindings()
            }
        }
    }
}


