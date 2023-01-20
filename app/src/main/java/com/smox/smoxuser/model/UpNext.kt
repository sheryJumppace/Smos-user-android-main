package com.smox.smoxuser.model

import android.content.Context
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.type.AppointmentType
import org.json.JSONException
import org.json.JSONObject

import java.io.Serializable
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

open class UpNext: Serializable {
    var id:Int = 0
    var image:String = ""
    var name:String = ""
    var phone:String = ""
    var services:String = ""
    var officialTime:String = ""
    var waitingList:String = ""
    var upNext:String = ""
    var lastClient:String = ""

    constructor() : super(){

    }
    constructor(json: JSONObject, context: Context) {
        try {
            val appointment = Appointment(json)
            id = appointment.id
            val user = appointment.user
            image = user.image
            name = user.name
            phone = if(user.phone.isEmpty()) user.email else user.phone
            services = appointment.getService(context, false)
            officialTime = appointment.getDayTime()

            if (json.has("upnext")){
                val jsonArray = json.getJSONArray("upnext")
                val items:ArrayList<Appointment> = ArrayList()
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    val apt = Appointment(item)
                    items.add(apt)
                }

                val approved = items.filter { it.status == AppointmentType.Approved}
                val completed = items.filter { it.status == AppointmentType.Completed}

                val dateFormat = SimpleDateFormat(Constants.KDateFormatter.defaultDate, Locale.getDefault())
                waitingList = if(approved.isEmpty()) "N/A" else approved.size.toString().padStart(2, '0')
                upNext = if(approved.isEmpty()) "N/A" else approved[0].user.name.capitalize()
                dateFormat.apply { Constants.KDateFormatter.hourAM }
                lastClient = if(completed.isEmpty()) "N/A" else dateFormat.format(completed[completed.size - 1].completedDate)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e:ParseException){
            e.printStackTrace()
        }

    }
}
