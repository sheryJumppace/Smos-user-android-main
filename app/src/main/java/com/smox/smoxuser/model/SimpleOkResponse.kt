package com.smox.smoxuser.model

import com.google.gson.annotations.SerializedName

data class SimpleOkResponse(

    @SerializedName("error") val error: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: List<SimpleResultResult>
)

class SimpleResultResult(

)
