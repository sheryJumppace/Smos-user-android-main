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
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.EventsItemRowBinding
import com.smox.smoxuser.model.Event
import com.smox.smoxuser.utils.FULL_IMAGE_PATH
import com.smox.smoxuser.utils.listeners.OnItemClicked
import java.util.ArrayList


class EventAdapter(val context: Context, val onItemClicked: OnItemClicked) : RecyclerView.Adapter<EventAdapter.ViewHolder>(){

    var  event: ArrayList<Event> = ArrayList()
    override fun getItemCount(): Int {
        return if (event == null) 0 else event!!.size
    }

    inner class ViewHolder : RecyclerView.ViewHolder {
        var listItemEventBinding: EventsItemRowBinding? = null
        constructor(binding: EventsItemRowBinding) : super(binding.root) {
            listItemEventBinding = binding
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val binding: ViewDataBinding

        binding = DataBindingUtil.inflate<EventsItemRowBinding>(
            inflater,
            R.layout.events_item_row, viewGroup, false
        )
        return ViewHolder(binding as EventsItemRowBinding)
    }

    fun setData(event: ArrayList<Event>) {
        this.event.addAll(event)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val event: Event = event[position]
            holder.apply {
                listItemEventBinding!!.event = event
                val options: RequestOptions =
                    RequestOptions().placeholder(R.drawable.small_placeholder)
                        .error(R.drawable.small_placeholder)

                val url= if (event.image.startsWith("http"))
                    event.image
                else
                    FULL_IMAGE_PATH + event.image

                Glide.with(context)
                    .load(url)
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(listItemEventBinding!!.imgEvents)

                listItemEventBinding!!.cardFull.setOnClickListener {
                    onItemClicked.onItemClick(position)
                }
            }

        }catch (ex:Exception){}
    }

    fun clearList() {
        this.event.clear()
        notifyDataSetChanged()
    }
}
