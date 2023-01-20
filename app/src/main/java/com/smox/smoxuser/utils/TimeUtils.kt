package com.smox.smoxuser.utils

import com.smox.smoxuser.manager.Constants
import java.text.SimpleDateFormat
import java.util.*

private const val MMMM_dd_PATTERN = "MMMM dd"
private const val ddMMyyyy_PATTERN = "ddMMyyyy"

fun getDate(milliseconds: Long): String {
    val dateFormat = SimpleDateFormat(MMMM_dd_PATTERN, Locale.getDefault())
    return dateFormat.format(Date(milliseconds))
}

fun getDateAsHeaderId(milliseconds: Long): Long {
    val dateFormat = SimpleDateFormat(ddMMyyyy_PATTERN, Locale.getDefault())
    return java.lang.Long.parseLong(dateFormat.format(Date(milliseconds)))
}

fun getCurrentStartTime(): String {

    val calendar = Calendar.getInstance()
    val min = calendar.get(Calendar.MINUTE)

    when (min) {
        in 0..9 -> calendar.set(Calendar.MINUTE, 10)
        in 10..19 -> calendar.set(Calendar.MINUTE, 20)
        in 20..29 -> calendar.set(Calendar.MINUTE, 30)
        in 30..39 -> calendar.set(Calendar.MINUTE, 40)
        in 40..49 -> calendar.set(Calendar.MINUTE, 50)
        in 50..59 -> {
            calendar.set(Calendar.MINUTE, 0)
            calendar.add(Calendar.HOUR_OF_DAY, 1)
        }
    }

    //Log.e(TAG, "getCurrentStartTime: ${calendar.time}")

    val timeFormat = SimpleDateFormat(Constants.KDateFormatter.hourAM, Locale.getDefault())
    val tt = timeFormat.format(calendar.time)

    //Log.e(TAG, "getCurrentStartTime: new time AM PM $tt")

    return tt
}