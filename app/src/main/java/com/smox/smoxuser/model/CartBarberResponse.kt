package com.smox.smoxuser.model

import com.google.gson.annotations.SerializedName

class CartBarberResponse(
    @SerializedName("error") val error : Boolean,
    @SerializedName("message") val message : String,
    @SerializedName("result") val result : List<CartBarberItem>
)

class CartBarberItem(
    @SerializedName("id") val id : Int,
    @SerializedName("barber_id") val barberId : Int,
    @SerializedName("cart_count") val cartCount : Int,
    @SerializedName("first_name") val firstName : String,
    @SerializedName("last_name") val lastName : String,
    @SerializedName("image") val image : String
)