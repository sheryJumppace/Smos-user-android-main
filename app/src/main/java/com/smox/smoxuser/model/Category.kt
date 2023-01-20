package com.smox.smoxuser.model

import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

class Category:Serializable {

    val services: ArrayList<Service> = ArrayList()
    var cat_id:String?=null
    var cat_name:String?=null

    constructor() : super()
    constructor(json: JSONObject) : super() {
        try {
            if (json.has("id")) {
                this.cat_id = json.getString("id")
            }
            if (json.has("name")) {
                this.cat_name = json.getString("name")
            }

            if(json.has("service"))
            {
                val jsonArray = json.getJSONArray("service")

                for (i in 0 until jsonArray.length()) {
                    val jsonNew = jsonArray.getJSONObject(i)
                    val service = Service(jsonNew)
                    services.add(service)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}