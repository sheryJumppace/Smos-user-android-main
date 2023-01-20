package com.smox.smoxuser.ui.activity.barber


import android.annotation.SuppressLint
import android.app.Activity
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TimePicker
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.applandeo.materialcalendarview.CalendarView
import com.smox.smoxuser.R
import com.smox.smoxuser.data.AppointmentRepository
import com.smox.smoxuser.data.ServiceRepository
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.*
import com.smox.smoxuser.model.type.AppointmentType
import com.smox.smoxuser.model.type.UserType
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.activity.customer.CheckOutActivity
import com.smox.smoxuser.ui.adapter.CategorySelectorAdapter
import com.smox.smoxuser.ui.adapter.ServiceBookingAdapter
import com.smox.smoxuser.ui.adapter.TimeSlotAdapter
import com.smox.smoxuser.utils.BoundTimePickerDialog
import com.smox.smoxuser.utils.shortToast
import com.smox.smoxuser.viewmodel.ServiceListViewModel
import com.smox.smoxuser.viewmodel.ServiceListViewModelFactory
import com.smox.smoxuser.viewmodel.TimeSlotListViewModel
import com.smox.smoxuser.viewmodel.TimeSlotListViewModelFactory
import kotlinx.android.synthetic.main.activity_booking_appointment.*
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class BookingAppointmentActivity : BaseActivity(), TimePickerDialog.OnTimeSetListener,
    CategorySelectorAdapter.CategorySelectActions {

    private lateinit var adapter: ServiceBookingAdapter
    private var items: ArrayList<Category> = ArrayList()
    private var itemsSelected: ArrayList<Category> = ArrayList()
    private lateinit var barber: Barber
    private lateinit var appointment: Appointment
    private var selectedService: ArrayList<Service>? = null
    private var category: Category? = null
    private var contact: Contact? = null
    private var isBarber = false
    private var duration = 0

    private lateinit var slotViewModel: TimeSlotListViewModel
    private lateinit var serviceViewModel: ServiceListViewModel

    private lateinit var calendarView: CalendarView

    companion object {
        const val ADD_SERVICE_REQUEST_CODE = 1
    }

    private var isServiceUpdate: Boolean = false
    private var startTime: String = ""
    private var endTime: String = ""
    private var formattedStartDate: String = ""
    private var formattedEndDate: String = ""
    private lateinit var timeformatter: SimpleDateFormat
    private lateinit var formatter: SimpleDateFormat
    private var startHour: Int = 0
    private var startMinute: Int = 0
    private var endHour: Int = 0
    private var endMinute: Int = 0
    private lateinit var cal_selected_date: Date
    var isRefresh: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_appointment)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@BookingAppointmentActivity, Constants.backButton))
        }

        isServiceUpdate = false

        if (isRefresh) {
            formatter = SimpleDateFormat(Constants.KDateFormatter.hourAM, Locale.getDefault())
            timeformatter = SimpleDateFormat(Constants.KDateFormatter.hourFull, Locale.getDefault())

            isBarber = app.currentUser.accountType == UserType.Barber

            if (intent.hasExtra("barber")) {
                barber = intent.getSerializableExtra("barber") as Barber
            } else {
                finish()
            }

            if (intent != null && intent.hasExtra("CatList")) {
                items = intent.getSerializableExtra("CatList") as ArrayList<Category>
                itemsSelected.addAll(items)
            }
            doRequestForCategoryList()


            if (intent.hasExtra("appointment")) {
                appointment = intent.getSerializableExtra("appointment") as Appointment

                val formatter =
                    SimpleDateFormat(Constants.KDateFormatter.hourAM, Locale.getDefault())
                val date =
                    if (appointment.status == AppointmentType.Approved) appointment.officialDate else appointment.preferredDate
                date?.apply {
                    txtTime.text = formatter.format(this)
                    startTime = formatter.format(this)
                    endTime = formatter.format(this)
                }
                txtComment.setText(appointment.comment)
            } else {
                finish()
            }

            if (intent.hasExtra("contact")) {
                contact = intent.getSerializableExtra("contact") as Contact
                contact?.apply {
                    if (profileImage != null) {
                        imgProfile.setImageBitmap(profileImage)
                    } else {
                        imgProfile.setImageDrawable(
                            ContextCompat.getDrawable(
                                this@BookingAppointmentActivity,
                                R.drawable.user
                            )
                        )
                    }
                    txtName.text = name
                    txtPhone.text = phoneNumber
                    getContactID()
                }

            } else {
                viewCustomer.visibility = View.GONE
            }

            btnBook.setOnClickListener {
                bookingAppointment()
            }

            btnAdd.setOnClickListener {
                isServiceUpdate = false;
                if (items != null) {
                    //if (category == null) {
                    /*val intent =
                        Intent(this@BookingAppointmentActivity, AddServicesActivity::class.java)
                    val user = if (isBarber) app.currentUser else appointment.user
                    intent.putExtra("barber_id", barber.id)
                    intent.putExtra("appointment_service", appointment.services)
                    startActivityForResult(intent, ADD_SERVICE_REQUEST_CODE)
                    overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)*/

                    var intent = Intent(this, CategoryActivity::class.java)
                    intent.putExtra("category_item", items)
                    intent.putExtra("appointment_service", appointment.services)
                    startActivityForResult(intent, 204)
                    this@BookingAppointmentActivity?.overridePendingTransition(
                        R.anim.activity_enter,
                        R.anim.activity_exit
                    )
                    /*} else {
                        var intent = Intent(this, ServiceSelectorActivity::class.java)
                        intent.putExtra("category_item", category)
                        intent.putExtra("fromBarber", false)
                        intent.putExtra("isShowMenu", true)
                        startActivityForResult(intent, 204)
                        overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
                    }*/
                } else {
                    doRequestForCategoryList()
                }
            }
            txtTime.setOnClickListener {
                openTimePicker()
            }

            initCalendar()
            initTimeSlotAdapter()
            updateServices()

        }
        isRefresh = true
        initServiceAdapter()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, 0)
        cal.set(Calendar.MONTH, 0)
        cal.set(Calendar.DAY_OF_MONTH, 0)
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        //formatter =  SimpleDateFormat(Constants.KDateFormatter.hourAM, Locale.getDefault())
        val time = formatter.format(cal.time)

        formattedStartDate = timeformatter.format(formatter.parse(startTime))
        formattedEndDate = timeformatter.format(formatter.parse(endTime))


        val selected_time: String = timeformatter.format(formatter.parse(time))

        var selectedHour: Int = selected_time.split(":")[0].toInt()
        var selectedMinute: Int = selected_time.split(":")[1].toInt()

        /*var validTime: Boolean
        if(hourOfDay < startHour) {
            validTime = false;
        }
        else if(hourOfDay > endHour) {
            validTime = false;
        }
        else if(hourOfDay == startHour) {
            validTime = minute >= startMinute;
        }
        else if(hourOfDay == endHour) {
            validTime = minute <= endMinute;
        }
        else {
            validTime = true;
        }

        if(validTime){
            txtTime.text = time
        } else {
            Toast.makeText(applicationContext, "Select Valid Time", Toast.LENGTH_LONG).show()
        }*/

        txtTime.text = time
        val slotAdapter = TimeSlotAdapter()
        time_slot_list.adapter = slotAdapter



        if (isInBetweenTime(time)) {
            var changedSlotTime: String = "$time-$endTime"


            Log.i("-changedSlotTime-", changedSlotTime)
            var changedSlot: List<String> = ArrayList()
            changedSlot = arrayListOf(changedSlotTime)

            txtTime.text = time

            btnBook.isEnabled = true
            btnBook.alpha = 1f
            btnBook.background = ContextCompat.getDrawable(
                applicationContext,
                R.drawable.bg_smox_button
            )
            txtSlotError.visibility = View.GONE
            time_slot_list.visibility = View.VISIBLE
            slotAdapter.submitList(changedSlot)
            slotAdapter.notifyDataSetChanged()
        } else {
            closedBooking()
        }
        /*val book_current_time: String = timeformatter.format(formatter.parse(txtTime.text.toString()))
        if(book_current_time.compareTo(formattedEndDate) > 0){
            Log.i("-Comapre-", "true")
        } else {
            Log.i("-Comapre-", "false")
        }


        if(formattedStartDate.compareTo(book_current_time) > 0){
            Log.i("-Comapre-", "true")
        } else {
            Log.i("-Comapre-", "false")
        }*/
    }

    private fun doRequestForCategoryList() {
        progressHUD.show()
        val params = java.util.HashMap<String, String>()
        APIHandler(
            this,
            Request.Method.GET,
            Constants.API.get_category + "/" + barber.id,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()

                    items.clear()
                    if (result.has("result")) {
                        val jsonArray = result.getJSONArray("result")

                        for (i in 0 until jsonArray.length()) {
                            val json = jsonArray.getJSONObject(i)
                            val category = Category(json)
                            items.add(category)
                        }

                        itemsSelected.clear()
                        for (category_item in items) {
                            /*if (appointment.services.size > 0 && category_item.cat_id == appointment.services[0].category_id) {
                                category = category_item
                                if (!itemsSelected.contains(category_item))
                                    itemsSelected.add(category_item)
                            }*/
                            for (service in appointment.services) {
                                if (service.category_id == category_item.cat_id) {
                                    itemsSelected.add(category_item)
                                    break
                                }
                            }
                        }
                        updateServiceUI(0)
                    }
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })

    }

    public fun isInBetweenTime(time: String): Boolean {

        try {



        var caltime: Calendar = Calendar.getInstance();
        val sdf1: SimpleDateFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        val sdf: SimpleDateFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)

       // caltime.setTime(sdf1.parse(sdf1.format(sdf.parse(time))))

//         caltime.setTime( sdf.format( Date()))

            val date = Date()
            val format: DateFormat = SimpleDateFormat("hh:mm a")

            caltime.setTime(sdf1.parse(format.format(date)))

        val cal1: Calendar = Calendar.getInstance();
        cal1.setTime(sdf1.parse(sdf1.format(sdf.parse(endTime))))
        val cal2: Calendar = Calendar.getInstance()
        cal2.setTime(sdf1.parse(sdf1.format(sdf.parse(startTime))))


        Log.e("ello",cal2.toString() +" " + cal1.toString())
        return caltime >= cal2 && caltime <= cal1;

        }catch (ex:Exception){
            ex.printStackTrace()
        }

        return false

    }


    private fun openTimePicker() {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        val timePickerDialog = BoundTimePickerDialog(
            this@BookingAppointmentActivity,
            this@BookingAppointmentActivity,
            hour,
            minute,
            false
        )
        if (!TextUtils.isEmpty(formattedStartDate)) {
            startHour = formattedStartDate.split(":")[0].toInt()
            startMinute = formattedStartDate.split(":")[1].toInt()

            timePickerDialog.setMin(startHour, startMinute)
        }
        if (!TextUtils.isEmpty(formattedEndDate)) {
            endHour = formattedEndDate.split(":")[0].toInt()
            endMinute = formattedEndDate.split(":")[1].toInt() + 1

            timePickerDialog.setMax(endHour, endMinute)
        }
        timePickerDialog.show()
    }

    private fun initCalendar() {
        calendarView = findViewById(R.id.calendarView)
        val calendar = Calendar.getInstance()
        if (intent.hasExtra("selectedDate") && intent.getLongExtra("selectedDate", 0) > 0)
            calendar.timeInMillis = intent.getLongExtra("selectedDate", 0)
        else if (intent.hasExtra("appointment")) {
            var timeIS =
                if (appointment.status == AppointmentType.Approved) appointment.officialDate else appointment.preferredDate
            if (timeIS != null) {
                if (timeIS.before(Date())) {
                    calendar.time = Date()
                } else {
                    calendar.time = timeIS
                }
            } else
                calendar.time = Date()
        } else
            calendar.time = Date()
        calendarView.setDate(calendar)

        val calendarMin = Calendar.getInstance()
        calendarMin.add(Calendar.DAY_OF_MONTH, -1)
        calendarView.setMinimumDate(calendarMin)

        var timeAformatter = SimpleDateFormat(
            Constants.KDateFormatter.hourAM,
            Locale.getDefault()
        )
        var cDate = Date()

        calendarView.setOnDayClickListener { eventDay ->
            val clickedDayCalendar = eventDay.calendar
            val slotAdapter = TimeSlotAdapter()
            time_slot_list.adapter = slotAdapter
            if (calendar.time < clickedDayCalendar.time) {

                var changedSlotTime: String = "$startTime-$endTime"

                var changedSlot: List<String> = ArrayList()
                changedSlot = arrayListOf(changedSlotTime)

                txtTime.text = startTime

                btnBook.isEnabled = true
                btnBook.alpha = 1f
                btnBook.background = ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.bg_smox_button
                )
                txtSlotError.visibility = View.GONE
                time_slot_list.visibility = View.VISIBLE
                slotAdapter.submitList(changedSlot)
                slotAdapter.notifyDataSetChanged()

            } else if (calendar.time == clickedDayCalendar.time) {
                var convertCurTime: String = timeAformatter.format(cDate)
                var changedSlotTime: String = "$convertCurTime-$endTime"
                Log.i("-changedSlotTime-", changedSlotTime)
                var changedSlot: List<String> = ArrayList()
                changedSlot = arrayListOf(changedSlotTime)

                txtTime.text = convertCurTime

                btnBook.isEnabled = true
                btnBook.alpha = 1f
                btnBook.background = ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.bg_smox_button
                )
                txtSlotError.visibility = View.GONE
                time_slot_list.visibility = View.VISIBLE
                slotAdapter.submitList(changedSlot)
                slotAdapter.notifyDataSetChanged()
            }
            cal_selected_date = clickedDayCalendar.time
            fetchSlot(clickedDayCalendar)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun fetchSlot(calendar: Calendar) {
        val day = calendar.get(Calendar.DAY_OF_WEEK)
        if (barber.openDays.size != 7) {
            getHours()
            return
        }
        val timeAformatter = SimpleDateFormat(
            Constants.KDateFormatter.hourAM,
            Locale.getDefault()
        )
        val cDate = Date()

        val openDay = barber.openDays[day - 1]
        val times = Calendar.getInstance().time
        if (openDay.isClosed || (cal_selected_date.date < times.date && cal_selected_date.month <= times.month && cal_selected_date.year <= times.year)) {
            closedBooking()
            return
        } else {
            btnBook.isEnabled = false
            btnBook.alpha = 0.5f
            txtSlotError.text = "Loading barber's available times"
            txtSlotError.visibility = View.VISIBLE
            time_slot_list.visibility = View.GONE

            val slotAdapter = TimeSlotAdapter()
            time_slot_list.adapter = slotAdapter


            startTime = openDay.startTime
            endTime = openDay.endTime
            txtTime.text = startTime
            var changedSlotTime: String = "$startTime-$endTime"
            if (iscurrentDate()) {
                val convertCurTime: String = timeAformatter.format(cDate)
                txtTime.text = convertCurTime
                if (isInBetweenTime(convertCurTime)) {
                    changedSlotTime = "$convertCurTime-$endTime"
                } else {
                    closedBooking()
                    return
                }
            }

            Log.i("-changedSlotTime-", changedSlotTime)
            var changedSlot: List<String> = ArrayList()
            changedSlot = arrayListOf(changedSlotTime)

            btnBook.isEnabled = true
            btnBook.alpha = 1f
            btnBook.background = ContextCompat.getDrawable(
                applicationContext,
                R.drawable.bg_smox_button
            )
            txtSlotError.visibility = View.GONE
            time_slot_list.visibility = View.VISIBLE
            slotAdapter.submitList(changedSlot)
            slotAdapter.notifyDataSetChanged()

        }
//        slotViewModel.fetchList(applicationContext, calendar.time, duration, openDay)
    }

    public fun iscurrentDate(): Boolean {
        val time = Calendar.getInstance().time
        return cal_selected_date.date == time.date && cal_selected_date.month == time.month && cal_selected_date.year == time.year;
    }

    private fun initTimeSlotAdapter() {
        val factory = TimeSlotListViewModelFactory(barber)
        slotViewModel = ViewModelProviders.of(this, factory).get(TimeSlotListViewModel::class.java)
        val slotAdapter = TimeSlotAdapter()
        slotAdapter.setItemClickListener(object : TimeSlotAdapter.ItemClickListener {
            override fun onItemClick(view: View, slot: String) {
                val t = slot.split("-")
                if (t.isNotEmpty()) {
                    startTime = t[0]
                    endTime = t[1]
                    txtTime.text = t[0]

                    formattedStartDate = timeformatter.format(formatter.parse(startTime))
                    formattedEndDate = timeformatter.format(formatter.parse(endTime))
                }
            }

        })
        time_slot_list.adapter = slotAdapter

        slotViewModel.slots.observe(this, Observer {
            if (it != null) {
                /*btnBook.isEnabled = true
                btnBook.alpha = 1f
                btnBook.background = ContextCompat.getDrawable(applicationContext, R.drawable.bg_smox_button)
                txtSlotError.visibility = View.GONE
                slotAdapter.submitList(it)
                slotAdapter.notifyDataSetChanged()*/

                val curDateFormat =
                    SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
                val curTimeFormat =
                    SimpleDateFormat(Constants.KDateFormatter.hourFull, Locale.getDefault())
                val cDate = Date()
                val curDate = curDateFormat.format(cDate)
                val curTime = curTimeFormat.format(cDate)
                val calSelDate = curDateFormat.format(cal_selected_date)

                if (curDate == calSelDate) {

                    if (it.isNotEmpty()) {
                        val timeSlot = it.get(0).split("-")
                        startTime = timeSlot[0]
                        endTime = timeSlot[1]

                        val convTimeAMPM =
                            SimpleDateFormat(Constants.KDateFormatter.hourAM, Locale.getDefault())
                        val convTime24 =
                            SimpleDateFormat(Constants.KDateFormatter.hourFull, Locale.getDefault())
                        val conStartTimeAMPM = convTimeAMPM.parse(startTime)
                        val startTimeFull = convTime24.format(conStartTimeAMPM)
                        val conEndTimeAMPM = convTimeAMPM.parse(endTime)
                        var endTimeFull = convTime24.format(conEndTimeAMPM)

                        formattedStartDate = timeformatter.format(formatter.parse(startTime))
                        formattedEndDate = timeformatter.format(formatter.parse(endTime))

                        val startSHour: Int = startTimeFull.split(":")[0].toInt()
                        val endSHour: Int = endTimeFull.split(":")[0].toInt()
//                        val curSHour: Int = curTime.split(":")[0].toInt()
                        val curSHour: Int = curTime.split(":")[0].toInt()

                        if (curSHour >= endSHour) {
                            //if(curTime.compareTo(endTime) > 0){
                            closedBooking()
                        } else {

                            if (curSHour >= startSHour) {
                                //if(curTime.compareTo(startTime) > 0){

                                var timeAformatter = SimpleDateFormat(
                                    Constants.KDateFormatter.hourAM,
                                    Locale.getDefault()
                                )
                                var convertCurTime: String = timeAformatter.format(cDate)
                                var changedSlotTime: String = "$convertCurTime-$endTime"
                                Log.i("-changedSlotTime-", changedSlotTime)
                                var changedSlot: List<String> = ArrayList()
                                changedSlot = arrayListOf(changedSlotTime)

                                txtTime.text = convertCurTime

                                btnBook.isEnabled = true
                                btnBook.alpha = 1f
                                btnBook.background = ContextCompat.getDrawable(
                                    applicationContext,
                                    R.drawable.bg_smox_button
                                )
                                txtSlotError.visibility = View.GONE
                                time_slot_list.visibility = View.VISIBLE
                                slotAdapter.submitList(changedSlot)
                                slotAdapter.notifyDataSetChanged()

                            } else if (curSHour < startSHour) {

                                var timeAformatter = SimpleDateFormat(
                                    Constants.KDateFormatter.hourAM,
                                    Locale.getDefault()
                                )
                                var convertCurTime: String = timeAformatter.format(cDate)
                                var changedSlotTime: String = "$startTime-$endTime"
                                Log.i("-changedSlotTime-", changedSlotTime)
                                var changedSlot: List<String> = ArrayList()
                                changedSlot = arrayListOf(changedSlotTime)

                                txtTime.text = convertCurTime

                                btnBook.isEnabled = true
                                btnBook.alpha = 1f
                                btnBook.background = ContextCompat.getDrawable(
                                    applicationContext,
                                    R.drawable.bg_smox_button
                                )
                                txtSlotError.visibility = View.GONE
                                time_slot_list.visibility = View.VISIBLE
                                slotAdapter.submitList(changedSlot)
                                slotAdapter.notifyDataSetChanged()
                            } else {
                                btnBook.isEnabled = true
                                btnBook.alpha = 1f
                                btnBook.background = ContextCompat.getDrawable(
                                    applicationContext,
                                    R.drawable.bg_smox_button
                                )
                                txtSlotError.visibility = View.GONE
                                time_slot_list.visibility = View.VISIBLE
                                slotAdapter.submitList(it)
                                slotAdapter.notifyDataSetChanged()
                            }
                        }

                    }
                } else {
                    btnBook.isEnabled = true
                    btnBook.alpha = 1f
                    btnBook.background =
                        ContextCompat.getDrawable(applicationContext, R.drawable.bg_smox_button)
                    txtSlotError.visibility = View.GONE
                    time_slot_list.visibility = View.VISIBLE
                    slotAdapter.submitList(it)
                    slotAdapter.notifyDataSetChanged()
                }

            }
        })

    }

    public fun closedBooking() {
        btnBook.isEnabled = false
        btnBook.alpha = 0.5f
        txtSlotError.text = "CLOSED"
        txtSlotError.visibility = View.VISIBLE
        time_slot_list.visibility = View.GONE
    }

    private fun initServiceAdapter() {
        val factory =
            ServiceListViewModelFactory(ServiceRepository.getInstance(barberId = barber.id))
        serviceViewModel =
            ViewModelProviders.of(this, factory).get(ServiceListViewModel::class.java)
        adapter = ServiceBookingAdapter()
        adapter.setItemClickListener(object : ServiceBookingAdapter.ItemClickListener {
            override fun onItemClick(view: View, position: Int, service: Service) {
                serviceViewModel.deleteService(service)
                isServiceUpdate = true
                updateServiceUI(0)
            }
        })
        service_list.adapter = adapter
        serviceViewModel.updateServices(appointment.services)
        serviceViewModel.services.observe(this, Observer { services ->
            if (services != null) {
                appointment.services = services as ArrayList<Service>

                duration = 0
                appointment.services.forEach {
                    duration += it.duration
                }
                //if(isServiceUpdate)
                itemsSelected.clear()
                if (appointment.services.size > 0) {
                    for (category_item in items) {
                        for (service in appointment.services) {
                            if (service.category_id == category_item.cat_id) {
                                itemsSelected.add(category_item)
                                break
                            }
                        }
                        /*if (appointment.services.size > 0 && category_item.cat_id == appointment.services[0].category_id) {
                            category = category_item
                            if (!itemsSelected.contains(category_item))
                                itemsSelected.add(category_item)
                            //title_service.text = category_item.cat_name
                        }*/
                    }

                    updateServiceUI(0)
                } else {
                    category = null
                    //title_service.text = ""
                    updateServiceUI(0)
                }
            } else {
                category = null
                //title_service.text = ""
                updateServiceUI(0)
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ADD_SERVICE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                //val message = data!!.getStringExtra("message")
                //Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == 204) {

            if (data != null) {
                val message = data!!.getBooleanExtra("isServiceUpdate", false);
                selectedService =
                    data.getSerializableExtra("selectedService") as ArrayList<Service>
                if (selectedService!!.size > 0) {
                    appointment.services.clear()
                    appointment.services.addAll(selectedService!!)
                    itemsSelected.clear()
                    for (category_item in items) {
                        for (services in appointment.services) {
                            if (services.category_id == category_item.cat_id) {
                                itemsSelected.add(category_item)
                                break
                            }
                        }
                    }
                    updateServiceUI(0)

                    isServiceUpdate = message
                    isRefresh = false
                }
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun setListData(position: Int) {
        var listService: ArrayList<Service> = ArrayList()
        for (service in appointment.services) {
            if (service.category_id == itemsSelected.get(position).cat_id) {
                listService.add(service)
            }
        }
        adapter.submitList(listService)
        adapter.notifyDataSetChanged()
    }

    private fun updateServices() {
        duration = 0
        appointment.services.forEach {
            duration += it.duration
        }
        calendarView.firstSelectedDate.apply {
            cal_selected_date = this.time
            fetchSlot(this)
        }

    }

    private fun getContactID() {

        val params = HashMap<String, String>()
        params["phone"] = contact!!.phoneNumber
        progressHUD.show()
        APIHandler(
            applicationContext,
            Request.Method.GET,
            Constants.API.user_by_phone,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()

                    if (result.has("result")) {
                        val data = result.getJSONObject("result")
                        if (data.has("user_id")) {
                            contact?.userID = data.getInt("user_id")
                        }
                        if (data.has("customer_id")) {
                            contact?.customerID = data.getInt("customer_id")
                        }
                    }
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    override fun onResume() {
        super.onResume()
        isRefresh = false
    }

    private fun getHours() {
        val params = java.util.HashMap<String, String>()
        progressHUD.show()
        APIHandler(
            applicationContext,
            Request.Method.GET,
            Constants.API.hours + "/" + barber.id.toString(),
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()

                    if (result.has("result")) {
                        val data = result.getJSONObject("result")
                        barber.openDays = barber.getOpenDays(data)
                        calendarView.firstSelectedDate.apply {
                            cal_selected_date = this.time
                            fetchSlot(this)
                        }
                    }
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    private fun bookingAppointment() {

        /*if (txtTime.text.isEmpty() || txtTime.text == "CLOSED") {
            showAlertDialog(
                "", "Please select starting time",
                DialogInterface.OnClickListener { _, _ ->
                    openTimePicker()
                }, getString(R.string.ok), null, null
            )
            return
        }*/

        if(txtTime.text == "CLOSED") {
            showAlertDialog(
                "", "Booking is closed",
                DialogInterface.OnClickListener { _, _ ->

                }, getString(R.string.ok), null, null
            )
            return
        }

        if (appointment.services.isEmpty()) {
            showAlertDialog(
                "", "Please select a service to proceed for booking",
                null, getString(R.string.ok), null, null
            )
            return
        }

        val formatter = SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
        val day = formatter.format(calendarView.firstSelectedDate.time)

        formatter.applyPattern(Constants.KDateFormatter.local)
        val date = formatter.parse("$day ${txtTime.text}")

        formatter.applyPattern(Constants.KDateFormatter.server)
        val serverDate = formatter.format(date)

        formatter.applyPattern(Constants.KDateFormatter.hourDetail)
        val localDate = formatter.format(date)

        formatter.applyPattern(Constants.KDateFormatter.server)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        val utcDate = formatter.format(date)

        val ids = appointment.services.map { it.id.toString() }.joinToString(",") { it }

        when {
            contact != null -> createAppointmentWithContact(ids, serverDate, localDate, utcDate)
            appointment.id > 0 -> updateAppointment(ids, serverDate, utcDate)
            else -> {
                //createAppointment(ids, serverDate, localDate, utcDate)
                appointment.barberId = barber.id
                appointment.duration = duration
                appointment.officialDate = date
                appointment.comment = txtComment.text.toString()

                val intent = Intent(this@BookingAppointmentActivity, CheckOutActivity::class.java)
                intent.putExtra("appointment", appointment)
                startActivity(intent)
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
            }
        }
    }

    private fun createAppointmentWithContact(
        services: String,
        date: String,
        time: String,
        utc: String
    ) {
        val formatter = SimpleDateFormat(Constants.KDateFormatter.server, Locale.getDefault())
        val message = String.format(
            "%s on %s with %s",
            appointment.services[0].title,
            time,
            app.currentUser.firstName
        )
        val files = java.util.ArrayList<File>()
        val names = java.util.ArrayList<String>()

        var imageName = ""
        contact?.apply {
            if (profileImage != null && !(userID > 0 || customerID > 0)) {

                imageName = String.format(
                    "%d_contact_%d.jpg",
                    app.currentUser.id,
                    System.currentTimeMillis()
                )
                persistImage(profileImage, imageName)?.apply {
                    files.add(this)
                    names.add(imageName)
                }
            }
        }
        val params = java.util.HashMap<String, String>()
        params["image"] = imageName
        params["type"] = "contact"
        contact?.apply {
            params["phone"] = phoneNumber
            params["name"] = name
            params["user_id"] = userID.toString()
            params["customer_id"] = customerID.toString()
        }

        params["services"] = services
        params["duration"] = duration.toString()
        params["date"] = Constants.convertLocalToUTC(date, formatter)
        //params["date"] = utc
        params["utc_date"] = utc
        params["comment"] = txtComment.text.toString()
        params["message"] = message

        for ((key, value) in params) {
            println("$key = $value")
        }

        progressHUD.show()

        /*APIHandler(
            applicationContext,
            Constants.API.upload_image,
            params,
            names,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()

                    showAlertDialog(
                        "Thank You!", "Your appointment has been scheduled",
                        DialogInterface.OnClickListener { _, _ ->
                            finish()
                        }, getString(R.string.ok), null, null
                    )
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    Toast.makeText(
                        applicationContext,
                        error, Toast.LENGTH_LONG
                    ).show()
                }
            })*/
    }

    private fun updateAppointment(services: String, date: String, utc: String) {
        val comment = txtComment.text.toString()
        val message: String

        val formatter = SimpleDateFormat(Constants.KDateFormatter.server, Locale.getDefault())
        val strDate = formatter.parse(date)

        formatter.applyPattern(Constants.KDateFormatter.hourDetail)

        val time = formatter.format(strDate)

        var preferredTime: String = appointment.strPreferredDate
        var officialTime: String = appointment.strOfficialDate
        //formatter.applyPattern(Constants.KDateFormatter.server)
        //var officialTime:String = Constants.convertLocalToUTC(appointment.strOfficialDate,formatter)

        formatter.applyPattern(Constants.KDateFormatter.server)
        //var conOfficialDate : Date = formatter.parse(appointment.strOfficialDate)
        //officialTime = Constants.convertLocalToUTC(appointment.strOfficialDate)
        var status = appointment.status.name.toLowerCase()

        if (isBarber) {
            //officialTime = date
            officialTime = Constants.convertLocalToUTC(date, formatter)
            status = AppointmentType.Approved.name.toLowerCase()
            message = String.format(
                "Your %s appointment with %s is scheduled for %s",
                appointment.services[0].title,
                app.currentUser.firstName,
                time
            )
        } else {
            if (appointment.status == AppointmentType.Approved) {
                //officialTime = date
                officialTime = Constants.convertLocalToUTC(date, formatter)
            } else {
                //preferredTime = date
                preferredTime = Constants.convertLocalToUTC(date, formatter)
            }
            message = String.format(
                "%s appointment with %s is scheduled for %s",
                appointment.services[0].title,
                app.currentUser.firstName,
                time
            )
        }

        val params = java.util.HashMap<String, String>()

        params["id"] = appointment.id.toString()
        params["services"] = services
        params["duration"] = duration.toString()
        params["preferred_at"] = preferredTime
        params["official_at"] = officialTime
        params["utc_date"] = utc
        params["status"] = status
        params["comment"] = comment
        params["message"] = message
        params["receiver"] =
            if (isBarber) appointment.customerId.toString() else appointment.barberId.toString()

        for ((key, value) in params) {
            println("$key = $value")
        }

        APIHandler(
            applicationContext,
            Request.Method.PUT,
            Constants.API.appointment,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    appointment.comment = comment
                    if (isBarber) {
                        appointment.status = AppointmentType.Approved
                    }
                    if (isBarber || appointment.status == AppointmentType.Approved) {
                        appointment.officialDate = strDate
                    } else {
                        appointment.preferredDate = strDate
                    }

                    AppointmentRepository.getInstance().updateAppointment(appointment)

                    showAlertDialog(
                        "Thank You!", "Your appointment has been scheduled",
                        DialogInterface.OnClickListener { _, _ ->
                            finish()
                        }, getString(R.string.ok), null, null
                    )
                }

                override fun onFail(error: String?) {
                    shortToast(error)
                }
            })
    }


    private fun persistImage(bitmap: Bitmap, name: String): File? {
        val filesDir = applicationContext.filesDir
        val imageFile = File(filesDir, name)
        return try {
            val os = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
            os.flush()
            os.close()
            imageFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun updateServiceUI(position: Int) {
        if (itemsSelected.size > 0) {
            setAdapter(position)
            tvNotFound.visibility = View.GONE
        } else tvNotFound.visibility = View.VISIBLE
    }

    override fun onItemClick(pos: Int) {
        setListData(pos)
    }

    private fun setAdapter(position: Int) {
        var categoryAdapter = CategorySelectorAdapter(this, this@BookingAppointmentActivity, false)
        service_category_list.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        service_category_list.setHasFixedSize(true)
        service_category_list.adapter = categoryAdapter

        categoryAdapter.doRefresh(itemsSelected)
        setListData(position)
    }
}
