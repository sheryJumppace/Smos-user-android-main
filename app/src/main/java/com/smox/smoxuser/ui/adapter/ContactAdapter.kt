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
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smox.smoxuser.data.ContactRepository
import com.smox.smoxuser.databinding.ListItemContactBinding
import com.smox.smoxuser.model.SmoxUser

class ContactAdapter : ListAdapter<SmoxUser, ContactAdapter.ViewHolder>(ContactDiffCallback()) {
    private lateinit var context:Context
    private var onItemClickListener: ItemClickListener? = null
    fun setItemClickListener(clickListener: ItemClickListener) {
        onItemClickListener = clickListener
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = getItem(position)
        holder.apply {
            bind(createOnClickListener(contact), contact)
            itemView.tag = contact
            btnFavorite.setOnClickListener {
                contact.isFavorite.set(!contact.isFavorite.get())
                ContactRepository.getInstance().updateFavoriteState(context ,contact.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            ListItemContactBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    private fun createOnClickListener(user: SmoxUser): View.OnClickListener {
        return View.OnClickListener {
            onItemClickListener?.onItemClick(it, user = user)
        }
    }
    private fun createOnFavoriteClickListener(user: SmoxUser): View.OnClickListener {
        return View.OnClickListener {
            onItemClickListener?.onItemClick(it, user)
        }
    }
    class ViewHolder(
        private val binding: com.smox.smoxuser.databinding.ListItemContactBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        var btnFavorite:ImageView = binding.btnFavorite
        fun bind(listener: View.OnClickListener, item: SmoxUser) {
            binding.apply {
                clickListener = listener
                user = item
                executePendingBindings()
            }
        }
    }

    interface ItemClickListener {
        fun onItemClick(view: View, user: SmoxUser)
    }
}

private class ContactDiffCallback : DiffUtil.ItemCallback<SmoxUser>() {

    override fun areItemsTheSame(oldItem: SmoxUser, newItem: SmoxUser): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: SmoxUser, newItem: SmoxUser): Boolean {
        return oldItem == newItem
    }
}
