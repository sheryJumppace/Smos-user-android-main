package com.smox.smoxuser.model

import androidx.databinding.ObservableBoolean
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

open class Address: Serializable {
    var id: Int = -1
    var customer_name: String = ""
    var customer_address: String = ""
    var postal_code: String = ""
    var address_latitude: Double = 0.0
    var address_longitude: Double = 0.0

    var isSelected: ObservableBoolean = ObservableBoolean(false)

    constructor() : super(){
        id = -1
    }

    constructor(json: JSONObject){
        try{
            if(json.has("id")){
                id = json.getInt("id")
            }
            if(json.has("name")){
                customer_name = json.getString("name")
            }
            if(json.has("address")){
                customer_address = json.getString("address")
            }
            if(json.has("postal_code")){
                postal_code = json.getString("postal_code")
            }
            if(json.has("latitude")){
                address_latitude = json.getString("latitude").toDouble()
            }
            if(json.has("longitude")){
                address_longitude = json.getString("longitude").toDouble()
            }
        } catch (e: JSONException){
            e.printStackTrace()
        }
    }

}