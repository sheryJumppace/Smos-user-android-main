package com.smox.smoxuser.model

import org.json.JSONObject
import java.io.Serializable

open class OpenDay(var day: String) : Serializable {
    var id:Int = 0
    var startTime:String = "09:00 AM"
    var endTime:String = "05:00 PM"
    var isClosed = true

    fun updateTime(data: String) {

        isClosed = data.isEmpty()|| data == "---"

        if(!isClosed){
            val times = data.split("-")
            if(times.size == 2){
                startTime = times[0]
                endTime = times[1]
            }else{
                isClosed = true
            }
        }else {
            startTime = "-"
            endTime = "-"
        }
    }

    fun getHours():String{
        return if(isClosed){
            ""
        }else{
            "$startTime-$endTime"
        }
    }

    fun updateOpenDays(key: String, value: String, data: JSONObject) : JSONObject {
        data.put(key,value)
        return data
    }

    fun getFieldNameOfDay():String{
        return  when(day) {
            "Sunday"->"su"
            "Monday"->"mo"
            "Tuesday"->"tu"
            "Wednesday"->"we"
            "Thursday"->"th"
            "Friday"->"fr"
            "Saturday"->"sa"
            else->"mo"
        }
    }

    override fun toString(): String {
        return "OpenDay(day='$day', id=$id, startTime='$startTime', endTime='$endTime', isClosed=$isClosed)"
    }

}
