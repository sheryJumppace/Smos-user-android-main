package com.smox.smoxuser.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Barber
import com.smox.smoxuser.model.TimeSlotModel
import com.smox.smoxuser.model.TimeSlotResult
import com.smox.smoxuser.utils.shortToast
import org.json.JSONArray
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.set

class TimeSlotListViewModel internal constructor(private val barber: Barber) : ViewModel() {

    var slots: MutableLiveData<List<String>> = MutableLiveData()
    var slotsTimes: MutableLiveData<List<TimeSlotModel>> = MutableLiveData()
    var timeSlotList: MutableLiveData<List<TimeSlotResult>> = MutableLiveData()

    fun fetchList(
        context: Context,
        day: Date,
        newStartTime: String,
        newEndTime: String,
        selectedDate: String,
        fullTime: String,
        appointmentId:String
    ) {
        val formatterr = SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
        val date = formatterr.format(day)
        val params = HashMap<String, String>()
        params["barber_id"] = barber.id.toString()
        try {
            var strStartTime = "$date $newStartTime"
            var strEndTime = "$date $newEndTime"
            val formatter = SimpleDateFormat(Constants.KDateFormatter.local, Locale.getDefault())
            val startDate = formatter.parse(strStartTime)
            val endDate = formatter.parse(strEndTime)

            formatter.applyPattern(Constants.KDateFormatter.server)

            strStartTime = formatter.format(startDate)
            strEndTime = formatter.format(endDate)

            params["start_time"] = strStartTime
            params["end_time"] = strEndTime
            params["current_date"] = selectedDate
            params["current_time"] = fullTime
            params["appointment_id"] = appointmentId
            APIHandler(
                context,
                Request.Method.GET,
                Constants.API.appointment_timeslots,
                params,
                object : APIHandler.NetworkListener {
                    override fun onResult(result: JSONObject) {
                        Log.e("", "onResult: timeslot response: $result")
                        timeSlotList.value = getTimeSlots(result.getJSONArray("result"))

                    }

                    override fun onFail(error: String?) {
                        shortToast(error)
                    }
                })
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    private fun getTimeSlots(jsonArray: JSONArray): ArrayList<TimeSlotResult> {

        val timeSlotList = arrayListOf<TimeSlotResult>()
        for (i in 0 until jsonArray.length()) {
            val json = jsonArray.getJSONObject(i)
            val timeSlot = TimeSlotResult(json.getString("timeslot"), json.getInt("status"), false)
            timeSlotList.add(timeSlot)
        }

        return timeSlotList
    }
}

class TimeSlotListViewModelFactory(
    private val barber: Barber
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = TimeSlotListViewModel(barber) as T
}