package com.smox.smoxuser.ui.activity.customer

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.github.jhonnyx2012.horizontalpicker.DatePickerListener
import com.github.jhonnyx2012.horizontalpicker.Day
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.smox.smoxuser.HolidayRes
import com.smox.smoxuser.R
import com.smox.smoxuser.data.AppointmentRepository
import com.smox.smoxuser.data.BarberRepository
import com.smox.smoxuser.databinding.ActivityEditAppointmentBinding
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Appointment
import com.smox.smoxuser.model.Barber
import com.smox.smoxuser.model.TimeSlotResult
import com.smox.smoxuser.model.UpdateAppointmentResponse
import com.smox.smoxuser.retrofit.ApiRepository
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.adapter.ServiceBookingAdapter
import com.smox.smoxuser.ui.adapter.TimeSlotAdapterNew
import com.smox.smoxuser.utils.getCurrentStartTime
import com.smox.smoxuser.utils.shortToast
import com.smox.smoxuser.viewmodel.AppointmentViewModel
import com.smox.smoxuser.viewmodel.AppointmentViewModelFactory
import com.smox.smoxuser.viewmodel.TimeSlotListViewModel
import com.smox.smoxuser.viewmodel.TimeSlotListViewModelFactory
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.json.JSONObject
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*


class EditAppointmentActivity : BaseActivity(), DatePickerListener,TimeSlotAdapterNew.ItemClickListener {
    private val TAG = "EditAppointmentActivity"
    lateinit var binding: ActivityEditAppointmentBinding
    private lateinit var viewModel: AppointmentViewModel
    private lateinit var adapter: ServiceBookingAdapter
    private lateinit var appointment: Appointment
    private lateinit var slotViewModel: TimeSlotListViewModel
    private val dateSelectCalender = Calendar.getInstance()
    lateinit var barber: Barber
    var slotTime: TimeSlotResult? = null
    var selectedDate: String = ""
    var dateSelected = Date()
    private var userId:String=""
    private var appointmentId:Int?=null
    private val slotAdapter = TimeSlotAdapterNew()
    var timeSlotList = arrayListOf<TimeSlotResult>()
    var holidayList = arrayListOf<HolidayRes.Holidays>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_appointment)

        if (intent.hasExtra(Constants.API.APPOINTMENT)) {
            appointment = intent.getSerializableExtra(Constants.API.APPOINTMENT) as Appointment
            appointmentId=appointment.id
            getHours(appointment.id.toString())
        }
        var isFirstTime=true

        val barberId=intent.getIntExtra(Constants.API.BARBER_ID,0)
        val barberRepository=BarberRepository.getInstance()
        barberRepository.barber.observe(this, androidx.lifecycle.Observer {
            barber=it
            if (BarberRepository.getInstance().barberHolidayList.value != null) {
                holidayList = BarberRepository.getInstance().barberHolidayList.value!!
            }
            val modelFactory = TimeSlotListViewModelFactory(barber)
            slotViewModel = ViewModelProviders.of(this, modelFactory).get(TimeSlotListViewModel::class.java)
            slotViewModel.timeSlotList.observe(this, androidx.lifecycle.Observer { itt->
                progressHUD.dismiss()
                timeSlotList = itt as ArrayList<TimeSlotResult>
                slotAdapter.submitList(itt)
            })
            if (isFirstTime) {
                isFirstTime=false
                binding.datePicker
                    .setListener(this)
                    .setDateSelectedColor(ContextCompat.getColor(this, R.color.gold))
                    .setTodayDateBackgroundColor(ContextCompat.getColor(this, R.color.gold))
                    .setOffset(3)
                    .init()

                binding.datePicker.setDate(DateTime())
            }
        })
        
        if (barberId != 0) {
            barberRepository.fetchBarberDetail(this, barberId)
            barberRepository.fetchBarberHoiliday(this, barberId)
        }
        
        Log.e(TAG, "onCreate: ${appointment.appointmentDate}" )

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val factory = AppointmentViewModelFactory(
            AppointmentRepository.getInstance(),
            appointmentId!!
        )

        viewModel = ViewModelProvider(this, factory).get(AppointmentViewModel::class.java)

        viewModel.appointment.observe(this) {
            if (it != null) {
                appointment = it
                //Log.e("Appointment_Detail", appointment.user.toString())
                binding.serviceList.adapter = adapter
                adapter.submitList(appointment.services)
                userId=appointment.customerId.toString()
                var duration=0
                for (item in appointment.services) {
                    duration += item.duration
                }
                var canSelectSlotCount = 0
                if (duration > 0) {
                    canSelectSlotCount = duration / 10
                    val oddDuration=duration%10
                    if (oddDuration!=0)
                        canSelectSlotCount++

                }
                slotAdapter.canSelectSlotCount(canSelectSlotCount)

                binding.txtName.text = appointment.user.firstName
                binding.txtContact.text = appointment.user.phone
                //binding.txtTimeSlot.text = appointment.getSlots()
                //binding.txtComment.text = appointment.comment
                //binding.txtDate.text = appointment.appointment_date
                //binding.txtTotal.text = "$" + appointment.price.toString() + "0"

                Glide.with(this)
                    .load(appointment.user.image)
                    .apply(
                        RequestOptions()
                        .placeholder(R.drawable.small_placeholder)
                        .error(R.drawable.small_placeholder)
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                    .into(binding.imageCircle)

            }
        }
        viewModel.fetchList(this)
        adapter = ServiceBookingAdapter()
        binding.serviceList.layoutManager = LinearLayoutManager(this)


        val timeSelected = arrayListOf<String>()

        binding.btnNext.setOnClickListener {
            timeSelected.clear()
            for (slot in timeSlotList) {
                if (slot.isSelected)
                    timeSelected.add(slot.timeslot)

            }
            updateAppointment(timeSelected)
        }

        binding.rvTimeSlot.adapter = slotAdapter

        slotAdapter.setItemClickListener(this)

    }


    private fun updateAppointment(times: ArrayList<String>) {
        val arr = JsonArray()
        for (item in times) {
            arr.add(item)
        }
        val dateFormat = SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
        val serverDate = SimpleDateFormat(Constants.KDateFormatter.server, Locale.getDefault())
        if (times.isNullOrEmpty()) {
            shortToast("Select a Time Slot for Book Appointment")
            return
        }

        val jsonObject = JsonObject()
        jsonObject.add("timeslots", arr)
        jsonObject.addProperty("barber_id", barber.id.toString())
        jsonObject.addProperty("appointment_id", appointmentId.toString())
        jsonObject.addProperty("user_id", userId)
        jsonObject.addProperty("date", selectedDate)

        progressHUD.show()
        //val params = JSONObject(jsonObject.toString())

        ApiRepository(this).updateAppointment(jsonObject)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<UpdateAppointmentResponse> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(res: UpdateAppointmentResponse) {
                    progressHUD.dismiss()
                    if (res.error) {
                        shortToast(res.result.message)
                    } else {
                        showDialog(
                            1,
                            "Your Appointment has been Reschedule successfully",
                            R.drawable.ic_baseline_check_circle_24,
                            "Back to Home",
                            false
                        )
                    }
                }

                override fun onError(e: Throwable) {
                    progressHUD.dismiss()
                    Log.e("TAG", "onError: ${e.message}")
                    if ((e as HttpException).code()==401) {
                        shortToast(getString(R.string.authError))
                        APIHandler(this@EditAppointmentActivity).logout()
                    }
                    else
                        shortToast(e.message())
                }

                override fun onComplete() {

                }

            })
    }

    fun showDialog(ids: Int, mess: String, id: Int, btmText: String, isClick: Boolean) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.message_dailog_fragment)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val txtMessage = dialog.findViewById<TextView>(R.id.txtMessage)
        val txtApprove = dialog.findViewById<TextView>(R.id.txtBtn)
        txtApprove.text = btmText
        val img = dialog.findViewById<ImageView>(R.id.imgMessage)
        img.setImageResource(id)
        txtMessage.text = mess

        txtApprove.setOnClickListener {
            finish()
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onDateSelected(dateSelectedd: DateTime?) {
        Log.e("TAG", "onDateSelected: $dateSelectedd")
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val localDateFormat = SimpleDateFormat(Constants.KDateFormatter.local_full_time, Locale.getDefault())
        val curDateFormat =
            SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
        val date = inputFormat.parse(dateSelectedd.toString())
        val selectDate = outputFormat.format(date!!)
        val timeFormat = SimpleDateFormat(Constants.KDateFormatter.hourAM, Locale.getDefault())
        dateSelected = outputFormat.parse(selectDate)!!
        val currDate = outputFormat.parse(outputFormat.format(Date()))
        selectedDate = curDateFormat.format(dateSelected)
        dateSelectCalender.time = date
        Log.e("TAG", "onDateSelected: formatted date : $selectDate $dateSelected $currDate")

        if (dateSelected >= currDate) {
            binding.datePicker.setDateSelectedTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.SelectedColor
                )
            )

            var newStartTime = "-"
            var newEndTime = "-"
            val openDay = barber.openDays[dateSelectCalender.get(Calendar.DAY_OF_WEEK) - 1]
            if (!openDay.isClosed) {
                if (dateSelected == currDate) {

                    val barberStartDateTime =
                        localDateFormat.parse(selectDate + " " + openDay.startTime)
                    var barberEndTime = localDateFormat.parse(selectDate + " " + openDay.endTime)

                    val cal = Calendar.getInstance()
                    cal.time = barberEndTime!!
                    cal.add(Calendar.MINUTE, -10)// close the store before 10 min
                    barberEndTime = cal.time

                    val timeOnly=timeFormat.format(Date())
                    val curTime=localDateFormat.parse(selectDate + " " + timeOnly)
                    Log.e(TAG, "onDateSelected: $barberStartDateTime $barberEndTime $curTime")
                    if (curTime!! > barberStartDateTime && curTime < barberEndTime) {
                        newStartTime = getCurrentStartTime()
                        newEndTime = openDay.endTime
                    }else if (curTime < barberStartDateTime){
                        newStartTime = openDay.startTime
                        newEndTime = openDay.endTime
                    }
                } else {
                    newStartTime = openDay.startTime
                    newEndTime = openDay.endTime
                }
            }

            var isHoliday=false
            for (item in holidayList) {
                isHoliday = selectedDate==item.date
            }

            if (!isHoliday) {
                if ((newStartTime != "-" && newEndTime != "-")&&(newStartTime !=newEndTime)) {
                    progressHUD.show()
                    val formatter =
                        SimpleDateFormat(Constants.KDateFormatter.second, Locale.getDefault())
                    val fullTime = formatter.format(Date())

                    slotViewModel.fetchList(
                        applicationContext,
                        date,
                        newStartTime,
                        newEndTime,
                        selectedDate,
                        fullTime,
                        appointmentId.toString()
                    )
                } else {
                    slotViewModel.timeSlotList.value =
                        arrayListOf(TimeSlotResult("Closed", 0, false))
                }
            }else{
                slotViewModel.timeSlotList.value =
                    arrayListOf(TimeSlotResult("Closed", 0, false))
            }

        } else {
            binding.datePicker.setDateSelectedTextColor(ContextCompat.getColor(this, R.color.black))
            slotViewModel.timeSlotList.value = arrayListOf()
            binding.datePicker.setDate(DateTime())
            binding.datePicker.onDateSelected(Day(DateTime()))
            shortToast("Select valid date")
        }

    }


    override fun onItemClick(slot: TimeSlotResult) {
        Log.e(TAG, "onItemClick: slot selected : ${slot.isSelected} ${slot.timeslot}")
        slotTime = slot

    }

    private fun getHours(barber:String) {
        val params = HashMap<String, String>()
        progressHUD.show()
        APIHandler(
            applicationContext,
            Request.Method.GET,
            Constants.API.hours_list + "/" + barber,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    if (result.has("result")) {
                        val data = result.getJSONObject("result")
                        Log.e(TAG, "Open days Data:- $data")
                        sessionManager.userDataOpenDays = data.toString()
                        //Log.e(TAG, "New Open days data:- " + sessionManager.userDataOpenDays!!)


                    }

                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    /* Toast.makeText(
                         applicationContext,
                         error, Toast.LENGTH_LONG
                     ).show()*/
                }
            })
    }

}