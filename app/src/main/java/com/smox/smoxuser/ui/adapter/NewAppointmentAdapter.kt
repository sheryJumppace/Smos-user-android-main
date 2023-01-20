package com.smox.smoxuser.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.CustListItemAppointsBinding
import com.smox.smoxuser.model.Appointment
import com.smox.smoxuser.model.type.AppointmentType
import com.smox.smoxuser.utils.FULL_IMAGE_PATH

import java.util.ArrayList

class NewAppointmentAdapter(var context: Context, var itemClickListner: ItemClickListner, var editClickListner: EditClickListner) : RecyclerView.Adapter<NewAppointmentAdapter.ViewHolder>(){

    var appointmentList= arrayListOf<Appointment>()

    override fun getItemCount(): Int {
        return if (appointmentList == null) 0 else appointmentList!!.size
    }

    inner class ViewHolder : RecyclerView.ViewHolder {


        var custListItemAppointsBinding: CustListItemAppointsBinding? = null
        constructor(binding: CustListItemAppointsBinding) : super(binding.root) {
            custListItemAppointsBinding = binding
        }

        @SuppressLint("SetTextI18n")
        fun bind(appointment: Appointment) {
            custListItemAppointsBinding.apply {
                this!!.appointment=appointment
            }

            if (appointment.status == AppointmentType.Completed) {
                custListItemAppointsBinding!!.imgEditAppointment.visibility = View.GONE
                custListItemAppointsBinding!!.txtServiceTime.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_completed_icon,
                    0, 0, 0
                )

            }else {
                custListItemAppointsBinding!!.imgEditAppointment.visibility= View.VISIBLE
                custListItemAppointsBinding!!.txtServiceTime.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_baseline_access_time_filled_24,
                    0, 0, 0
                )
            }
            val options: RequestOptions =
                RequestOptions().placeholder(R.drawable.small_placeholder)
                    .error(R.drawable.small_placeholder)

            val url= if (appointment.user.image.startsWith("http"))
                appointment.user.image
            else
                FULL_IMAGE_PATH + appointment.user.image

            Glide.with(context)
                .load(url)
                .apply(options)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(custListItemAppointsBinding!!.imgProfile)

            Log.e("TAG", "timeslot: ${appointment.timeslot}" )
            var fromTime=""
            var toTime=""
            if (!appointment.timeslot.isNullOrEmpty()) {
                val start = appointment.timeslot[0]
                val end = appointment.timeslot[appointment.timeslot.size-1]
                fromTime=start.split("-")[0]
                toTime=end.split("-")[1]
            }

            custListItemAppointsBinding!!.txtServiceTime.text= "$fromTime-$toTime"

            custListItemAppointsBinding!!.imgEditAppointment.setOnClickListener {
                editClickListner.onEditClickListner(appointment)

            }

        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val binding: ViewDataBinding

        binding = DataBindingUtil.inflate<CustListItemAppointsBinding>(
            inflater,
            R.layout.cust_list_item_appoints, viewGroup, false
        )

        return ViewHolder(binding as CustListItemAppointsBinding)
    }

    fun setData(barber: ArrayList<Appointment>) {
        val startPos = this.appointmentList!!.size
        this.appointmentList!!.addAll(barber)
        notifyItemRangeInserted(startPos, barber.size)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val appointment: Appointment = appointmentList!![position]

            holder.bind(appointment)
            holder.itemView.setOnClickListener {
                itemClickListner.onItemClickListner(position, appointment.id,appointment)
            }

        }catch (ex:Exception){}
    }

    fun addMoreItems(appointment: List<Appointment>, isSameTab:Boolean) {
        if (isSameTab) {
            val startPos = this.appointmentList.size
            this.appointmentList.addAll(appointment)
            if (appointmentList.size>2)

            notifyItemRangeInserted(startPos, appointmentList.size)
            else
                notifyDataSetChanged()
        }else {
            appointmentList.clear()
            this.appointmentList.addAll(appointment)
            notifyDataSetChanged()
        }
    }

    fun clearList() {
        appointmentList.clear()
        notifyDataSetChanged()
    }

    interface ItemClickListner{
        fun onItemClickListner(pos: Int, appointId:Int, appointment: Appointment)
    }

    interface EditClickListner{
        fun onEditClickListner(appointment: Appointment)
    }
}
