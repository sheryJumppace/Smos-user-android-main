package com.smox.smoxuser.model

import android.util.Log
import com.smox.smoxuser.manager.Constants
import org.json.JSONException
import org.json.JSONObject

import java.io.Serializable
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

open class Event : Serializable {

    var id: Int = -1
    var barberId: Int = -1
    var image: String = ""
    var name: String = ""
    var event: String = ""
    var startAt: Date = Date()
    var endAt: Date = Date()
    /*var startAt: String  = ""
    var endAt: String = ""*/

    constructor() : super() {
        id = -1
    }

    constructor(json: JSONObject) {
        try {
            if (json.has("id")) {
                id = json.getInt("id")
            }
            if (json.has("barber_id")) {
                this.barberId = json.getInt("barber_id")
            }
            if (json.has("name")) {
                this.name = json.getString("name")
            }
            if (json.has("image")) {
                this.image = json.getString("image")
            }
            if (json.has("event")) {
                this.event = json.getString("event")
            }
            if (json.has("start_at")) {
                val t = json.getDouble("start_at").toLong() * 1000
                this.startAt = Date(t)
            }
            if (json.has("end_at")) {
                val t = json.getDouble("end_at").toLong() * 1000
                this.endAt = Date(t)
            }

            /*if (json.has("start_at")) {
                this.startAt = json.getString("start_at")
            }
            if (json.has("end_at")) {
                this.endAt = json.getString("end_at")
            }*/

        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: ParseException) {
            e.printStackTrace()
        }

    }

    fun getStart(): String {
        //Log.e("TIme start at", "$startAt")
        val dateFormat = SimpleDateFormat(Constants.KDateFormatter.displayFull, Locale.getDefault())
        return dateFormat.format(startAt)
    }

    fun getEnd(): String {
        //Log.e("TIme end at", "$endAt")
        val dateFormat = SimpleDateFormat(Constants.KDateFormatter.displayFull, Locale.getDefault())
         return dateFormat.format(endAt)
    }

    fun imageUrl(): String {
        return Constants.downloadUrlll(image)
    }

    fun getStartDate(): String {
        Log.e("TIme start at", "$startAt")
        val dateFormat = SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
        return dateFormat.format(startAt)
    }

    fun getEndDate(): String {
        Log.e("TIme start at", "$endAt")
        val dateFormat = SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
        return dateFormat.format(endAt)
    }
}
