package com.smox.smoxuser.data

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.databinding.BaseObservable
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.kaopiz.kprogresshud.KProgressHUD
import com.smox.smoxuser.App
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.manager.SessionManager
import com.smox.smoxuser.model.Appointment
import com.smox.smoxuser.model.Event
import com.smox.smoxuser.model.type.AppointmentType
import com.smox.smoxuser.utils.shortToast
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Repository module for handling data operations.
 */
class AppointmentRepository : BaseObservable() {

    var appointments: MutableLiveData<List<Appointment>> = MutableLiveData()
    var appointmentsMainList = arrayListOf<Appointment>()
    var events: MutableLiveData<List<Event>> = MutableLiveData()
    var selectedAppointment: MutableLiveData<Appointment> = MutableLiveData()
    var staus: MutableLiveData<String> = MutableLiveData()
    var isSuccessToSentPaymentRequest: MutableLiveData<Boolean> = MutableLiveData()
    var isCheckCompleted = false
    protected lateinit var sessionManager: SessionManager

    fun getAppointment(context: Context, id: Int) {
        //val appointment = appointments.value?.find { it.id == id }
        /*if (appointment != null) { // comment because at notification time not get updated data from liv, used already saved data in list
            selectedAppointment.value = appointment
            fetchServices(context, appointment)
        } else {*/
        fetchAppointment(context, id)
        //}
    }

    fun getAppointmentById(id: Int): Appointment? {
        return appointments.value?.find { it.id == id }
    }

    fun updateAppointment(appointment: Appointment) {
        if (appointments.value != null) {
            val all = appointments.value!! as ArrayList
            val item = all.find { it.id == appointment.id }
            if (item == null) {
                all.add(appointment)
                appointments.value = all
            } else {
                all.indexOfFirst { it.id == appointment.id }.apply {
                    all[this] = appointment
                    appointments.value = all
                    if (appointment.id == selectedAppointment.value?.id) {
                        selectedAppointment.value = appointment
                    }
                }
            }
        }

    }

    fun getAppointments(type: AppointmentType): List<Appointment> {
        //val items = appointments.value
        val items = appointmentsMainList
        return items?.filter { it.status == type } ?: ArrayList<Appointment>()
    }

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: AppointmentRepository? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: AppointmentRepository().also {
                    it.appointments.value= arrayListOf<Appointment>()
                    instance = it
                }
            }
    }

   /* fun fetchList(context: Context, date: String, barberID: Int) {
        sessionManager = SessionManager.getInstance(context)
        val params = HashMap<String, String>()
        params["date"] = date
        params["timezone"] = TimeZone.getDefault().id

        for ((key, value) in params) {
            println("$key = $value")
        }

        val progressHUD = KProgressHUD(context)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        progressHUD.show()

        APIHandler(
            context,
            Request.Method.GET,
            Constants.API.appointment_by_date + "/" + barberID,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    parseResult(result, context)
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    Toast.makeText(
                        context.applicationContext,
                        error, Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun parseResult(result: JSONObject, context: Context) {
        val jsonArray = result.getJSONArray("result")
        Log.d("Result data :- ", result.toString())
        val items: ArrayList<Appointment> = ArrayList()
        for (i in 0 until jsonArray.length()) {
            val json = jsonArray.getJSONObject(i)
            json.put("pos", i)

            val appointment = Appointment(json)
            items.add(appointment)
        }
        try {
            if (result.has("up_next") && !result.isNull("up_next")) {
                val upNext = result.getJSONObject("up_next")
                if (upNext.has("status")) {
                    staus.value = upNext.getString("status")
                }
            }
        } catch (e: JSONException) {

        }

        if (result.has("sub_data")) {
            val subData = result.getJSONObject("sub_data")
            sessionManager.isSubscribed = subData.getBoolean("is_subscribed")
            sessionManager.subscription_enddate = subData.getString("subscription_enddate")
        }

        tidyData(items)

        if (result.has("events")) {
            val evs = result.getJSONArray("events")
            Log.e("Events data list:-", evs.toString())
            val its: ArrayList<Event> = ArrayList()
            for (i in 0 until evs.length()) {
                val json = evs.getJSONObject(i)
                val item = Event(json)
                its.add(item)
            }
            events.value = its
        }
    }*/

    fun completeAppointment(context: Context, appointment: Appointment) {
        val message = when (appointment.status) {
            AppointmentType.Completed -> String.format(
                "%s appointment with %s has been completed",
                appointment.services[0].title,
                App.instance.currentUser.firstName
            )
            else -> String.format(
                "%s appointment is cancelled by %s",
                appointment.services[0].title,
                App.instance.currentUser.firstName
            )
        }
        val completedDate = Date()
        val dateFormat = SimpleDateFormat(Constants.KDateFormatter.server, Locale.getDefault())
        //val date = dateFormat.format(completedDate)
        val date = Constants.convertLocalToUTC(completedDate, dateFormat)

        val tag = Constants.API.appointment_status + "/" + appointment.id.toString()
        val receiver = appointment.customerId
        val params = HashMap<String, String>()
        params["status"] = appointment.status.name.toLowerCase()
        params["receiver"] = receiver.toString()
        params["message"] = message
        params["date"] = date
        params["fee"] = appointment.cancellationFee.toString()
        params["barber_id"] = App.instance.currentUser.id.toString()

        val progressHUD = KProgressHUD(context)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        progressHUD.show()

        APIHandler(
            context,
            Request.Method.POST,
            tag,
            params,
            object : APIHandler.NetworkListener {
                @SuppressLint("SetTextI18n")
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    //parseResult(result, context)
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    fun createAppointment(context: Context, services: String, duration: Int) {
        val formatter = SimpleDateFormat(Constants.KDateFormatter.server, Locale.getDefault())

        formatter.applyPattern(Constants.KDateFormatter.server)
        val date = formatter.format(Date())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        val utcDate = formatter.format(Date())

        val params = java.util.HashMap<String, String>()
        params["services"] = services
        params["duration"] = duration.toString()
        //params["date"] = date
        params["date"] = utcDate
        params["utc_date"] = utcDate
        val progressHUD = KProgressHUD(context)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        progressHUD.show()
        APIHandler(
            context,
            Request.Method.POST,
            Constants.API.appointment_walkin,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    //parseResult(result, context)
                }

                override fun onFail(error: String?) {
                    shortToast(error)
                }
            })
    }

    fun sendReorderAppointment(context: Context, data: JSONObject) {
        /*val progressHUD = KProgressHUD(context)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        progressHUD.show()*/

        APIHandler(
            context,
            Request.Method.POST,
            Constants.API.rearrange_appointment,
            data,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    //progressHUD.dismiss()
                    //parseResult(result, context)
                }

                override fun onFail(error: String?) {
                    /*Toast.makeText(
                        context,
                        error, Toast.LENGTH_LONG
                    ).show()*/
                }
            },
            "json"
        )
    }

/*
    fun sendPaymentRequest(context: Context, appointment: Appointment) {
        if (isCheckCompleted) return
        isCheckCompleted = true


        val services = appointment.services.map { it.title }.joinToString("#||#") { it }
        val subPrices = appointment.services.map { it.price.toString() }.joinToString(",") { it }
        var amount = 0.0f
        for (s in appointment.services) {
            amount += s.price
        }

        val progressHUD = KProgressHUD(context)
        val params = HashMap<String, String>()

        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        progressHUD.show()

        params["amount"] = amount.toString()
        params["sub_prices"] = subPrices
        params["services"] = services

        val dateFormat = SimpleDateFormat(Constants.KDateFormatter.displayFull, Locale.getDefault())
        //val date = dateFormat.format(Date())
        val date = Constants.convertLocalToUTC(Date(), dateFormat)
        params["date"] = date

        APIHandler(
            context,
            Request.Method.PUT,
            Constants.API.send_payment_request + "/" + appointment.id,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    Toast.makeText(
                        context.applicationContext,
                        "Success to send the Payment Request", Toast.LENGTH_LONG
                    ).show()
                    isSuccessToSentPaymentRequest.value = true

                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    isCheckCompleted = false
                    Toast.makeText(
                        context.applicationContext,
                        error, Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
*/

    fun fetchAppointmentsOfCustomer(context: Context, page: String) {
        val params = HashMap<String, String>()
        params["page"] = page
        params["order_by"] = "desc"

        val progressHUD = KProgressHUD(context)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        progressHUD.show()

        APIHandler(
            context,
            Request.Method.GET,
            Constants.API.appointment,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    val jsonArray = result.getJSONArray("result")
                    Log.e("TAG", "onResult: appointments $result")
                    val items: ArrayList<Appointment> = ArrayList()
                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray.getJSONObject(i)
                        json.put("pos", i)
                        val appointment = Appointment(json)
                        items.add(appointment)
                    }
                    appointments.value = items
                    appointmentsMainList.addAll(items)
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    private fun fetchAppointment(context: Context, id: Int) {
        val params = HashMap<String, String>()
        val tab = Constants.API.appointment + "/" + id

        val progressHUD = KProgressHUD(context)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        progressHUD.show()

        APIHandler(
            context,
            Request.Method.GET,
            tab,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    val json = result.getJSONObject("result")
                    json.put("pos", -1)
                    selectedAppointment.value = Appointment(json)

                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    selectedAppointment.value=null
                    shortToast(error)

                }
            })
    }

    /*private fun fetchServices(context: Context, appointment: Appointment) {
        val params = HashMap<String, String>()
        params["ids"] = appointment.services.map { it.id }.joinToString(",") { it.toString() }


        val progressHUD = KProgressHUD(context)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        progressHUD.show()

        APIHandler(
            context,
            Request.Method.GET,
            Constants.API.service,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    val jsonArray = result.getJSONArray("result")
                    val items: ArrayList<Service> = ArrayList()
                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray.getJSONObject(i)
                        val service = Service(json)
                        items.add(service)
                    }
                    appointment.services = items
                    selectedAppointment.value = appointment
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    Toast.makeText(
                        context.applicationContext,
                        error, Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    fun tidyData(items: List<Appointment>) {
        val approved = items.filter { it.status == AppointmentType.Approved }
        val pending = items.filter { it.status == AppointmentType.Pending }
        val completed = items.filter { it.status == AppointmentType.Completed }
        val temp = ArrayList<Appointment>()

        temp.addAll(approved)
        if (pending.isNotEmpty()) {
            val section = Appointment()
            section.isSection = true
            section.comment = "PENDING"
            temp.add(section)
            temp.addAll(pending)
        }

        if (completed.isNotEmpty()) {
            val section = Appointment()
            section.isSection = true
            section.comment = "COMPLETED"
            temp.add(section)
            temp.addAll(completed)
        }

        appointments.value = temp
    }*/

    fun postEvent(
        context: Context,
        event: String,
        start: Long,
        end: Long,
        startDate: String,
        endDate: String,
        id: Int,
        pos: Int
    ) {
        val user = App.instance.currentUser
        val message = String.format("%s\nStart:%s\nEnd :%s", event, startDate, endDate)
        //Log.e("Message data", message)

        val dateFormat = SimpleDateFormat(Constants.KDateFormatter.server, Locale.getDefault())
        val createdAt = dateFormat.format(Date())
/*
//        dateFormat.applyPattern(Constants.KDateFormatter.serverDay)

        var sDate = dateFormat.parse(start)!!
        var eDate = dateFormat.parse(end)!!*/

        val c = Calendar.getInstance()
        c.time = Date(start)
        //c.add(Calendar.DATE, 1)
        var sDate = c.time

        val c1 = Calendar.getInstance()
        c1.time = Date(end)
        //c1.add(Calendar.DATE, 1)
        var eDate = c1.time

        val sd = sDate.time / 1000
        val ed = eDate.time / 1000

        val params = HashMap<String, String>()
        params["name"] = user.name
        params["event"] = event
        params["start"] = startDate
        params["end"] = endDate
        /*params["start"] = sd.toString()
        params["end"] = ed.toString()*/
        params["message"] = message
        params["created"] = createdAt
        params["id"] = id.toString()
        //params["timezone"] = TimeZone.getDefault().id

        for ((key, value) in params) {
            println("$key = $value")
        }

        val progressHUD = KProgressHUD(context)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        progressHUD.show()
        progressHUD.show()
//
        APIHandler(
            context,
            Request.Method.POST,
            Constants.API.event,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()

                    val review = Event()
                    review.id = result.getInt("result")
                    review.event = event

                    /*val c = Calendar.getInstance()
                    c.time = sDate
                    c.add(Calendar.DATE, -1)
                    sDate = c.time

                    val c1 = Calendar.getInstance()
                    c1.time = eDate
                    c1.add(Calendar.DATE, -1)
                    eDate = c1.time*/

                    review.startAt = sDate
                    review.endAt = eDate
                    /*review.startAt = startDate
                    review.endAt = endDate*/

                    val temp = events.value as ArrayList<Event>

                    if (id == 0)
                        temp.add(0, review)
                    else {
                        temp[pos] = review
                    }

                    events.value = temp
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }


}
