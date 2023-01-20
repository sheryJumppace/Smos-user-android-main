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
import com.smox.smoxuser.HolidayRes
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.HolidayItemRowBinding
import com.smox.smoxuser.manager.Constants
import java.text.SimpleDateFormat
import java.util.*


class HolidayAdapter(val context: Context) : RecyclerView.Adapter<HolidayAdapter.ViewHolder>() {

    var holiday: ArrayList<HolidayRes.Holidays> = ArrayList()
    override fun getItemCount(): Int {
        return if (holiday == null) 0 else holiday.size
    }

    inner class ViewHolder : RecyclerView.ViewHolder {
        var listItemEventBinding: HolidayItemRowBinding? = null

        constructor(binding: HolidayItemRowBinding) : super(binding.root) {
            listItemEventBinding = binding
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val binding: ViewDataBinding

        binding = DataBindingUtil.inflate<HolidayItemRowBinding>(
            inflater,
            R.layout.holiday_item_row, viewGroup, false
        )
        return ViewHolder(binding as HolidayItemRowBinding)
    }

    fun setData(event: ArrayList<HolidayRes.Holidays>) {
        this.holiday.addAll(event)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val event: HolidayRes.Holidays = holiday[position]
            holder.apply {
                listItemEventBinding!!.txtDate.text = getFormattedTime(event.date)
                listItemEventBinding!!.txtHolidayName.text = event.title
            }
        } catch (ex: Exception) {
        }
    }

    private fun getFormattedTime(date: String): String {

        var formatedDate = ""
        try {
            val formatter =
                SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
            val displayFormatter =
                SimpleDateFormat(Constants.KDateFormatter.displayFull, Locale.getDefault())
            val bookDate = formatter.parse(date)
            formatedDate = displayFormatter.format(bookDate)
        } catch (e: java.lang.Exception) {

        }
        return formatedDate
    }

    fun clearList() {
        this.holiday.clear()
        notifyDataSetChanged()
    }
}
