package com.smox.smoxuser.ui.activity.customer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.jhonnyx2012.horizontalpicker.DatePickerListener
import com.github.jhonnyx2012.horizontalpicker.Day
import com.smox.smoxuser.HolidayRes
import com.smox.smoxuser.R
import com.smox.smoxuser.data.BarberRepository
import com.smox.smoxuser.databinding.ActivityBookAppointmentBinding
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.manager.SessionManager
import com.smox.smoxuser.model.*
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.activity.auth.LoginActivity
import com.smox.smoxuser.ui.adapter.CategoryNameAdapter
import com.smox.smoxuser.ui.adapter.NewServiceAdapter
import com.smox.smoxuser.ui.adapter.TimeSlotAdapterNew
import com.smox.smoxuser.utils.getCurrentStartTime
import com.smox.smoxuser.utils.listeners.OnItemClicked
import com.smox.smoxuser.utils.shortToast
import com.smox.smoxuser.viewmodel.TimeSlotListViewModel
import com.smox.smoxuser.viewmodel.TimeSlotListViewModelFactory
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.text.SimpleDateFormat
import java.util.*


class BookAppointmentActivity : BaseActivity(), DatePickerListener, OnItemClicked,
    NewServiceAdapter.OnServiceClicked, TimeSlotAdapterNew.ItemClickListener {
    private val TAG = "BookAppointmentActivity"
    lateinit var binding: ActivityBookAppointmentBinding
    private lateinit var slotViewModel: TimeSlotListViewModel
    private lateinit var barber: Barber
    var items: ArrayList<Category> = ArrayList();
    private var catNameList = arrayListOf<String>()
    private var catServiceList = arrayListOf<Service>()
    private var serviceSelectedList = arrayListOf<Service>()
    lateinit var serviceAdapter: NewServiceAdapter
    private var dateSelectCalender = Calendar.getInstance()
    var slotTime: TimeSlotResult? = null
    var selectedDate: String = ""
    var dateSelected = Date()
    var timeSlotSelectCount = 0
    val slotAdapter = TimeSlotAdapterNew()
    var timeSlotList = arrayListOf<TimeSlotResult>()
    var holidayList = arrayListOf<HolidayRes.Holidays>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_book_appointment)

        if (intent.hasExtra("barber")) {
            barber = intent.getSerializableExtra("barber") as Barber
        }
        if (BarberRepository.getInstance().barberHolidayList.value != null) {
            holidayList = BarberRepository.getInstance().barberHolidayList.value!!
        }
        val bundle = intent.extras
        if (bundle != null && bundle.containsKey("catList")) {
            items = bundle.getSerializable("catList") as ArrayList<Category>;
            for (item in items) {
                catNameList.add(item.cat_name.toString())
            }
            setAdapter()
        }

        binding.datePicker
            .setListener(this)
            .setDateSelectedColor(ContextCompat.getColor(this, R.color.gold))
            .setTodayDateBackgroundColor(ContextCompat.getColor(this, R.color.gold))
            .setOffset(3)
            .init()

        binding.datePicker.setDate(DateTime(DateTimeZone.getDefault()).plusDays(0))

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.txtNext.setOnClickListener {
       Log.d("++--++","BookAppointmentActivity is called")
            if (sessionManager.apiKey?.isNotEmpty()!!) {
                val serviceList = arrayListOf<Service>()
                for (item in serviceSelectedList) {
                    //Log.e(TAG, "onCreate: service selected " + item.isSelected.get())
                    if (item.isSelected.get())
                        serviceList.add(item)
                }

                val timeSelected = arrayListOf<String>()

                for (slot in timeSlotList) {
                    if (slot.isSelected)
                        timeSelected.add(slot.timeslot)
                }
                if (serviceList.isNotEmpty() && timeSelected.isNotEmpty()) {
                    val appointment = Appointment()
                    appointment.services = serviceList
                    appointment.timeslot = timeSelected
                    appointment.preferredDate = dateSelected
                    appointment.officialDate = dateSelected
                    appointment.strOnlyDate = selectedDate
                    appointment.barberId = barber.id
                    appointment.barberName = barber.firstName + " " + barber.lastName

                    startActivity(
                        Intent(this, BookAppointmentPaymentActivity::class.java)
                            .putExtra("appointment", appointment)
                    )
                } else {
                    shortToast("Please select service or time slot first.")
                }
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
            }
        }
        initTimeSlotAdapter()

    }

    private fun initTimeSlotAdapter() {
        val factory = TimeSlotListViewModelFactory(barber)
        slotViewModel = ViewModelProviders.of(this, factory).get(TimeSlotListViewModel::class.java)
        binding.rvTimeSlot.adapter = slotAdapter
        slotAdapter.setItemClickListener(this)
        slotViewModel.timeSlotList.observe(this, androidx.lifecycle.Observer {
            progressHUD.dismiss()
            timeSlotList = it as ArrayList<TimeSlotResult>
            slotAdapter.submitList(it)

        })
    }
    private fun setAdapter() {
        serviceAdapter = NewServiceAdapter(this, catServiceList, true, this)
        binding.rvServiceList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvServiceList.setHasFixedSize(true)
        binding.rvServiceList.adapter = serviceAdapter

        val categoryAdapter = CategoryNameAdapter(this, this, catNameList)
        binding.rvCategoryName.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategoryName.setHasFixedSize(true)
        binding.rvCategoryName.adapter = categoryAdapter
    }

    override fun onDateSelected(dateSelectedd: DateTime?) {
        Log.e(TAG, "onDateSelected: selectedDate= $dateSelectedd")
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val localDateFormat =
            SimpleDateFormat(Constants.KDateFormatter.local_full_time, Locale.getDefault())
        val timeFormat = SimpleDateFormat(Constants.KDateFormatter.hourAM, Locale.getDefault())
        val curDateFormat =
            SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
        val date = inputFormat.parse(dateSelectedd.toString())
        val selectDate = outputFormat.format(date!!)
        dateSelected = outputFormat.parse(selectDate)!!
        val currDate = outputFormat.parse(outputFormat.format(Date()))
        selectedDate = curDateFormat.format(dateSelected)
        dateSelectCalender.time = date
        //Log.e(TAG, "onDateSelected: formatted date : $selectDate $dateSelected $currDate")

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

                    val timeOnly = timeFormat.format(Date())
                    val curTime = localDateFormat.parse(selectDate + " " + timeOnly)
                    Log.e(TAG, "onDateSelected: $barberStartDateTime $barberEndTime $curTime")
                    if (curTime!! > barberStartDateTime && curTime < barberEndTime) {
                        newStartTime = getCurrentStartTime()
                        newEndTime = openDay.endTime
                    } else if (curTime < barberStartDateTime) {
                        newStartTime = openDay.startTime
                        newEndTime = openDay.endTime
                    }
                } else {
                    newStartTime = openDay.startTime
                    newEndTime = openDay.endTime
                }
            }

            var isHoliday = false
            for (item in holidayList) {
                isHoliday = selectedDate == item.date
            }

            if (!isHoliday) {
                if ((newStartTime != "-" && newEndTime != "-") && (newStartTime != newEndTime)) {
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
                        "-1"
                    )
                } else {
                    slotViewModel.timeSlotList.value =
                        arrayListOf(TimeSlotResult("Closed", 0, false))
                }
            } else {
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

    override fun onItemClick(pos: Int) {
        Log.e(TAG, "--> onItemClick: $pos")
        binding.tvNoDataFound.visibility = View.GONE
        catServiceList.clear()
        if (!items[pos].services.isNullOrEmpty())
            catServiceList.addAll(items[pos].services)
        else
            binding.tvNoDataFound.visibility = View.VISIBLE

        serviceAdapter.notifyDataSetChanged()
    }

    override fun onServiceClicked(pos: Int) {

        var duration = 0
        val service = catServiceList[pos]
        val index = serviceSelectedList.indexOfFirst { it.id == service.id }
        if (index == -1) {
            serviceSelectedList.add(catServiceList[pos])
        } else
            serviceSelectedList.remove(service)

        for (item in serviceSelectedList) {
            if (item.isSelected.get())
                duration += item.duration
        }
        var canSelectSlotCount = 0
        if (duration > 0) {
            canSelectSlotCount = duration / 10
            val oddDuration = duration % 10
            if (oddDuration != 0)
                canSelectSlotCount++
        }

        slotAdapter.canSelectSlotCount(canSelectSlotCount)
    }

    override fun onItemClick(slot: TimeSlotResult) {
        slotTime = slot
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResumeee: " + SessionManager.getInstance(this).isBack)
        if (SessionManager.getInstance(this).isBack) {
            binding.datePicker.setDate(DateTime(DateTimeZone.getDefault()))
            SessionManager.getInstance(this).isBack = false
        }
    }
}