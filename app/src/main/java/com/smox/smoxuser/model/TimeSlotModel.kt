package com.smox.smoxuser.model

import java.io.Serializable

open class TimeSlotModel(slott: String, isSelectedd: Boolean) : Serializable {
    var slot= slott
    var isSelected= isSelectedd
}