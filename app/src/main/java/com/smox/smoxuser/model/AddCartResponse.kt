package com.smox.smoxuser.model

import com.google.gson.annotations.SerializedName

data class AddCartResponse(

    @SerializedName("error") val error: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: CartCount
)

class CartCount(
    @SerializedName("cart_count") val cartCount : Int
)
