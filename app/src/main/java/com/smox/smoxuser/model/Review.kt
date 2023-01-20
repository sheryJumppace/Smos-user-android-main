package com.smox.smoxuser.model

import android.text.format.DateFormat
import androidx.databinding.ObservableBoolean
import com.smox.smoxuser.manager.Constants
import org.json.JSONException
import org.json.JSONObject

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

open class Review : Serializable {
    var id:Int = 0
    var barberId:Int = 0
    var userId:Int = 0
    var name:String = ""
    var comment:String = ""
    var rating:Int = 0
    var date: Date? = null

    var isMore: ObservableBoolean = ObservableBoolean(false)
    constructor() : super()
    constructor(json: JSONObject) {
        try {
            if (json.has("id")) {
                id = json.getInt("id")
            }
            if (json.has("barber_id")) {
                this.barberId = json.getInt("barber_id")
            }
            if (json.has("user_id")) {
                this.userId = json.getInt("user_id")
            }
            if (json.has("name")) {
                this.name = json.getString("name")
            }
            if (json.has("comment")) {
                this.comment = json.getString("comment")
                isMore.set(this.comment.length <= 200)
            }
            if (json.has("rating")) {
                this.rating = json.getInt("rating")
            }
            if (json.has("created_at")) {
                val strDate = json.getString("created_at")
                val dateFormat = SimpleDateFormat(Constants.KDateFormatter.server, Locale.getDefault())
//                dateFormat.timeZone = TimeZone.getTimeZone("Etc/UTC")
                this.date = dateFormat.parse(strDate)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }


    fun getReviewComment():String{
        if(isMore.get()){
            return comment
        }else{
            if(comment.length > 200){
                return comment.substring(0, 200)
            }else{
                return comment
            }

        }
    }

    fun getDisplayTime():String{

        if (this.date == null) {
            return ""
        }
        val d = this.date!!
        val smsTime = Calendar.getInstance()
        smsTime.time = d

        val now = Calendar.getInstance()

        return if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            DateFormat.format(Constants.KDateFormatter.hourAM, smsTime).toString() // Today
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            "Yesterday"
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            DateFormat.format(Constants.KDateFormatter.display, smsTime).toString() // This Year
        } else {
            DateFormat.format(Constants.KDateFormatter.displayFull, smsTime).toString()
        }
    }
}

