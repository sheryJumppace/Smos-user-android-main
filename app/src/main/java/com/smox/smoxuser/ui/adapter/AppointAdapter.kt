package com.smox.smoxuser.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.CustListItemAppointsBinding
import com.smox.smoxuser.manager.SessionManager
import com.smox.smoxuser.model.Appointment
import com.smox.smoxuser.model.type.AppointmentType
import kotlinx.android.synthetic.main.list_item_appoint.view.*

class AppointAdapter : ListAdapter<Appointment, AppointAdapter.ViewHolder>(AppointDiffCallback()) {
    var isEditable = false
    var canShowService = false
    var context: Context? = null
    var appointmentList= arrayListOf<Appointment>()

    interface ItemClickListener {
        fun onItemClick(view: View, appointmentId: Int)
    }

    private var onItemClickListener: ItemClickListener? = null

    fun setItemClickListener(clickListener: ItemClickListener) {
        onItemClickListener = clickListener
    }

    fun setShowService(showService: Boolean) {
        this.canShowService = showService
    }

    override fun submitList(list: List<Appointment>?) {

        val startPos=appointmentList.size
        appointmentList.addAll(list!!)
        super.submitList(appointmentList)
        //notifyItemRangeInserted(startPos,appointmentList.size)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = getItem(position)

        val isCanDelete =
            isEditable && appointment.customerId == 0 && appointment.status == AppointmentType.Approved
        holder.itemView.txtName.text =
            if (appointment.customerId == SessionManager.getInstance(context!!).userId) appointment.user.name else "USER0$position"

        holder.apply {
            bind(createOnClickListener(appointment.id), appointment, isCanDelete, canShowService)
            itemView.tag = appointment
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            CustListItemAppointsBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    private fun createOnClickListener(appointmentId: Int): View.OnClickListener {
        return View.OnClickListener {
            onItemClickListener?.onItemClick(it, appointmentId)
        }
    }

    fun addMoreItems(appointments: List<Appointment>) {

    }

    class ViewHolder(
        private val binding: CustListItemAppointsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            listener: View.OnClickListener,
            item: Appointment,
            canDelete: Boolean,
            canShowService: Boolean
        ) {
            binding.apply {
                clickListener = listener
                isCanDelete = canDelete
                isShowService = canShowService
                appointment = item
                executePendingBindings()
            }
            if (item.status.equals(AppointmentType.Completed))
            binding.txtServiceTime.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_clock,
                0,0,0)
            else
                binding.txtServiceTime.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_access_time_filled_24,
                    0,0,0)
        }
    }
}

private class AppointDiffCallback : DiffUtil.ItemCallback<Appointment>() {

    override fun areItemsTheSame(oldItem: Appointment, newItem: Appointment): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Appointment, newItem: Appointment): Boolean {
        return oldItem == newItem
    }
}

