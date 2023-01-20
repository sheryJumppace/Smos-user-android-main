package com.smox.smoxuser.model

import com.google.gson.annotations.SerializedName

data class UpdateAppointmentResponse(
    @SerializedName("error") val error : Boolean,
    @SerializedName("result") val result : Result
) {

    class Result(
        @SerializedName("appointment_id") val appointment_id : Int,
        @SerializedName("status") val status : Boolean,
        @SerializedName("message") val message : String
    )

}