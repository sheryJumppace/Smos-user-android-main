package com.smox.smoxuser

import com.google.gson.annotations.SerializedName

class HolidayRes(
    @SerializedName("error") val error : Boolean,
    @SerializedName("holidays") val holidays : List<Holidays>
) {
    data class Holidays (
        @SerializedName("id") val id : String,
        @SerializedName("barber_id") val barber_id : String,
        @SerializedName("date") val date : String,
        @SerializedName("title") val title : String,
        @SerializedName("created_at") val created_at : String,
        @SerializedName("day") val day : String
            )

}
