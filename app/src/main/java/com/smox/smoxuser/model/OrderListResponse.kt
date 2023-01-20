package com.smox.smoxuser.model

import com.smox.smoxuser.manager.Constants
import java.text.SimpleDateFormat
import java.util.*

class OrderListResponse(
    val error : Boolean,
    val message : String,
    val result : List<OrderItem>
)

class OrderItem(
    val id : Int,
    val cart_id : String,
    val barber_id : Int,
    val user_id : Int,
    val sub_total : Double,
    val shipping_charges : Double,
    val total_price : Double,
    val name : String,
    val phone : String,
    val address : String,
    val email : String,
    val client_secret : String,
    val payment_intent_id : String,
    val payment_status : Boolean,
    val order_status : String,
    val delivery_date : String,
    val created_at : String,
    val barber_image : String,
    val barber_fname : String,
    val barber_lname : String,
    val product_name : String,
    val quantity : Int,
    val product_image : String,
    val product_total : String,
    val updated_at : String,
    val deleted_at : String,
    val product_count : Int
){
    fun getDateee():String{
        val inputFormat = SimpleDateFormat(Constants.KDateFormatter.server, Locale.getDefault())
        inputFormat.timeZone= TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(created_at)
        val outputFormat = SimpleDateFormat(Constants.KDateFormatter.displayFullTime, Locale.getDefault())
        outputFormat.timeZone= TimeZone.getDefault()
        return outputFormat.format(date)
    }

    fun getOrderOnlyDate():String{
        val inputFormat = SimpleDateFormat(Constants.KDateFormatter.server, Locale.getDefault())
        inputFormat.timeZone= TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(created_at)
        val outputFormat = SimpleDateFormat(Constants.KDateFormatter.displayFull, Locale.getDefault())
        outputFormat.timeZone= TimeZone.getDefault()
        return outputFormat.format(date)
    }

    fun getOrderShipDate():String{
        val inputFormat = SimpleDateFormat(Constants.KDateFormatter.server, Locale.getDefault())
        inputFormat.timeZone= TimeZone.getTimeZone("UTC")
        var date = inputFormat.parse(created_at)
        val cal=Calendar.getInstance()
        cal.time=date
        cal.add(Calendar.DATE,1)
        date=cal.time
        val outputFormat = SimpleDateFormat(Constants.KDateFormatter.displayFull, Locale.getDefault())
        outputFormat.timeZone= TimeZone.getDefault()
        return outputFormat.format(date)
    }

    fun getDeliveryDate():String{
        val inputFormat = SimpleDateFormat(Constants.KDateFormatter.server, Locale.getDefault())
        inputFormat.timeZone= TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(delivery_date)
        val outputFormat = SimpleDateFormat(Constants.KDateFormatter.displayFull, Locale.getDefault())
        outputFormat.timeZone= TimeZone.getDefault()
        return outputFormat.format(date)
    }

    fun isShowCancelBtn():Boolean{
        return order_status.equals("pending", true)||order_status.equals("placed", true)
    }

    fun isShowCancelStatus():Boolean{
        return order_status.equals("cancelled", true)
    }
}