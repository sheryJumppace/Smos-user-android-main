package com.smox.smoxuser.model

import com.google.gson.annotations.SerializedName

class TimeSlotResponse(

    @SerializedName("error") val error: Boolean,
    @SerializedName("result") val result: List<TimeSlotResult>
)

class TimeSlotResult(

    @SerializedName("timeslot")
    val timeslot: String = "",
    @SerializedName("status")
    val status: Int = 0,
    @SerializedName("isSelected")
    var isSelected: Boolean = false
)
