package com.smox.smoxuser.model

class OrderDetailResponse(
    val error : Boolean,
    val message : String,
    val result : List<OrderDetailItem>
)

class OrderDetailItem(
    val id : Int,
    val order_id : Int,
    val barber_id : Int,
    val user_id : Int,
    val category_id : Int,
    val product_id : Int,
    val product_name : String,
    val quantity : String,
    val main_image : String,
    val actual_price : Double,
    val discounted_price : Double,
    val shipping_charge : Double,
    val total_price : Double,
    val created_at : String,
    val updated_at : String,
    val deleted_at : String
)