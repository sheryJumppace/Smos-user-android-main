package com.smox.smoxuser.model

import androidx.databinding.ObservableInt
import org.json.JSONException
import org.json.JSONObject

import java.io.Serializable

open class Product : Serializable {
    var id:Int = 0
    var title:String = ""
    var price:Float = 0.0f
    var shippingCharges:Float = 0.0f
    var tax:Float = 0.0f
    var productDescription: String = ""
    var image:String = ""
    var quantity:ObservableInt = ObservableInt(1)
    var stripe_public_key:String = ""

    constructor() : super()
    constructor(json: JSONObject) {
        try {
            if (json.has("id")) {
                id = json.getInt("id")
            }

            if (json.has("title")){
                title = json.getString("title")
            }
            if (json.has("tamount")) {
                price = json.getDouble("tamount").toFloat()
            }
            if (json.has("shipping_charges")) {
                shippingCharges = json.getDouble("shipping_charges").toFloat()
            }
            if (json.has("tax")) {
                tax = json.getDouble("tax").toFloat()
            }
            if (json.has("description")) {
                productDescription = json.getString("description").replace("\\n","\n")
            }
            if (json.has("stripe_public_key")) {
                stripe_public_key = json.getString("stripe_public_key")
            }
            if (json.has("main_img")) {
                image = json.getString("main_img")
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }



    /*override fun toString(): String {
        return "Product(id=$id, title='$title', price=$price, shippingCharges=$shippingCharges, tax=$tax, productDescription='$productDescription', image='$image', quantity=$quantity)"
    }*/

}
