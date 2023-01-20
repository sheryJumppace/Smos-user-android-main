package com.smox.smoxuser.model

import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

open class Orders: Serializable {
    var id:Int = -1
    var product_title: String = ""
    var customer_name: String = ""
    var barber_name: String = ""
    var product_price: Float = 0f
    var quantity: Int = 0
    var status: Int = 0
    var product_image: String = ""
    var order_id: String = ""
    var shipping_charges: Float = 0f
    var tax: Float = 0f
    var total_amount: Float = 0f

    constructor() : super(){
        id = -1
    }

    constructor(json: JSONObject){
        try{
            if(json.has("id")){
                id = json.getInt("id")
            }
            if(json.has("title")){
                product_title = json.getString("title")
            }
            if(json.has("customer_name")){
                customer_name = json.getString("customer_name")
            }
            if(json.has("barber_name")){
                barber_name = json.getString("barber_name")
            }
            if(json.has("tamount")){
                product_price = json.getString("tamount").toFloat()
            }
            if(json.has("quantity")){
                quantity = json.getInt("quantity")
            }
            if(json.has("status")){
                status = json.getInt("status")
            }
            if(json.has("main_img")){
                product_image = json.getString("main_img")
            }
            if(json.has("order_id")){
                order_id = json.getString("order_id")
            }
            if(json.has("shipping_charges")){
                shipping_charges = json.getString("shipping_charges").toFloat()
            }
            if(json.has("tax")){
                tax = json.getString("tax").toFloat()
            }
            if(json.has("amount")){
                total_amount = json.getString("amount").toFloat()
            }
        } catch (e: JSONException){
            e.printStackTrace()
        }
    }
}