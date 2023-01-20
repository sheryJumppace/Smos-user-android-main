package com.smox.smoxuser.model

import androidx.databinding.ObservableBoolean
import androidx.room.Ignore
import org.json.JSONException
import org.json.JSONObject

import java.io.Serializable

@Suppress("UNUSED_PARAMETER")
open class Service : Serializable {
    var id:Int = 0
    var title:String = ""
    var price:Float = 0.0f
    var duration:Int = 0
    var serviceDescription: String = ""
    var image:String = ""
    var category_id:String = ""

    var isSelected: ObservableBoolean = ObservableBoolean(false)
    var isSelectedService: Boolean = false

    @Ignore
    private var isDraggable = true

    @Ignore
    fun isDraggable(): Boolean {
        return isDraggable
    }

    @Ignore
    fun setDraggable() {
        isDraggable = true
    }

    constructor() : super()
    constructor(json: JSONObject){
        try {
            if (json.has("id")) {
                id = json.getInt("id")
            }
            
            if (json.has("title")){
                title = json.getString("title")
            }
            if (json.has("price")) {
                price = json.getDouble("price").toFloat()
            }
            if (json.has("duration")) {
                duration = json.getInt("duration")
            }
            if (json.has("description")) {
                serviceDescription = json.getString("description")
            }
            if (json.has("image")) {
                image = json.getString("image")
            }
            if (json.has("category_id")) {
                category_id = json.getString("category_id")
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    override fun toString(): String {
        return "id=$id, title='${title.toUpperCase()}', isDraggable=$isDraggable"
    }
}
