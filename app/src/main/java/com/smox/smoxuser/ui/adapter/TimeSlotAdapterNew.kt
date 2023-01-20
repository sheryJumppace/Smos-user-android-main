package com.smox.smoxuser.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ItemRowTimeSlotsBinding
import com.smox.smoxuser.model.TimeSlotResult
import com.smox.smoxuser.utils.shortToast
import kotlinx.android.synthetic.main.item_row_time_slots.view.*


class TimeSlotAdapterNew :
    ListAdapter<TimeSlotResult, TimeSlotAdapterNew.ViewHolder>(TimeSlotDiffCallbackk()) {
    private lateinit var context: Context
    private var selectedSlotCountFixed: Int = 0
    private var selectedSlotCountActual: Int = 0
    private var timeSlotList = arrayListOf<TimeSlotResult>()

    interface ItemClickListener {
        fun onItemClick(slot: TimeSlotResult)
    }

    private var onItemClickListener: ItemClickListener? = null

    fun setItemClickListener(clickListener: ItemClickListener) {
        onItemClickListener = clickListener
    }

    override fun submitList(list: List<TimeSlotResult?>?) {
        super.submitList(list?.let { ArrayList(it) })
        //timeSlotList.clear()
        timeSlotList = list as ArrayList<TimeSlotResult>
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val slot = getItem(position)
        holder.apply {
            bind(slot)
            itemView.tag = slot
        }

        if (slot.isSelected) {
            holder.itemView.txtTimeSlot.setBackgroundResource(R.drawable.bg_slot_selected)
            onItemClickListener?.onItemClick(slot)
        } else {
            if (slot.status == 2)
                holder.itemView.setBackgroundResource(R.drawable.bg_slot_un_selected_pink)
            else
                holder.itemView.txtTimeSlot.setBackgroundResource(R.drawable.bg_slot_un_select)
        }

        if (slot.status == 0) {
            holder.itemView.txtTimeSlot.setBackgroundResource(R.drawable.bg_slot_unavailable)
            holder.itemView.isEnabled = false
        }

        holder.itemView.setOnClickListener {
            if (selectedSlotCountFixed > 0) {
                Log.e(
                    "TAG",
                    "onBindViewHolder: actual: $selectedSlotCountActual  fixed: $selectedSlotCountFixed",
                )

                val endPos: Int = position + selectedSlotCountFixed
                if (endPos <= timeSlotList.size) {
                    var i = 0
                    var booked: Boolean
                    while (i < timeSlotList.size) {

                        if (i == position) {
                            booked = false
                            for (k in position until endPos) {
                                if (timeSlotList[k].status == 1 || timeSlotList[k].status == 2) {
                                    timeSlotList[k].isSelected = true
                                } else {
                                    booked = true
                                }
                                i++
                            }
                            if (booked) {
                                if (selectedSlotCountFixed > 1)
                                    shortToast("Middle slot is already booked, please choose different time slot.")
                                else
                                    shortToast("Slot is already booked, please choose different time slot.")

                                for (k in position until endPos) {
                                    timeSlotList[k].isSelected = false
                                }
                                refreshTimeslot()
                            }
                            continue
                        }
                        timeSlotList[i].isSelected = false
                        i++
                    }
                    notifyDataSetChanged()
                } else {
                    shortToast("Time exceeds, please choose different time slot.")
                }
            } else
                shortToast("Please select services first.")
        }
        holder.itemView.isEnabled = slot.timeslot != "Closed"
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemRowTimeSlotsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    fun canSelectSlotCount(canSelectSlotCount: Int) {
        selectedSlotCountFixed = canSelectSlotCount
        refreshTimeslot()
    }

    private fun refreshTimeslot() {
        for (item in timeSlotList) {
            item.isSelected = false
        }
        notifyDataSetChanged()
    }

    class ViewHolder(
        private val binding: ItemRowTimeSlotsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TimeSlotResult) {
            binding.apply {
                time = item.timeslot
                executePendingBindings()
            }
        }
    }
}

private class TimeSlotDiffCallbackk : DiffUtil.ItemCallback<TimeSlotResult>() {

    override fun areItemsTheSame(oldItem: TimeSlotResult, newItem: TimeSlotResult): Boolean {
        return oldItem.timeslot == newItem.timeslot
    }

    override fun areContentsTheSame(oldItem: TimeSlotResult, newItem: TimeSlotResult): Boolean {
        return oldItem == newItem
    }

}

