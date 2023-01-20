package com.smox.smoxuser.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.applandeo.materialcalendarview.EventDay
import org.json.JSONException
import org.json.JSONObject

import java.io.Serializable
import java.util.*

open class NumberOfDay @RequiresApi(Build.VERSION_CODES.O) constructor(json: JSONObject, firstDay: Calendar) : Serializable {
    lateinit var evenDay: EventDay
    var count:Int = 0

    init {
        try {
            val cal = Calendar.getInstance()
            if (json.has("a_day")) {
                val day = json.getInt("a_day")
                cal.set(firstDay.get(Calendar.YEAR), firstDay.get(Calendar.MONTH), day)
            }
            if (json.has("num")) {
                this.count = json.getInt("num")
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}
