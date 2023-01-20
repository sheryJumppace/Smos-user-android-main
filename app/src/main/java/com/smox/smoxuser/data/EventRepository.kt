
package com.smox.smoxuser.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.databinding.BaseObservable
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Event
import com.smox.smoxuser.utils.shortToast
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

/**
 * Repository module for handling data operations.
 */
class EventRepository: BaseObservable() {
    private var isFetchInProgress = false

    var totalCount:Int? = null
    var page:Int = 0

    var events: MutableLiveData<List<Event>> = MutableLiveData()
    var eventsList:ArrayList<Event> = ArrayList()

    fun getEvent(id:Int): Event? {
        return eventsList.find { it.id == id }
    }

    companion object {

        // For Singleton instantiation
        @Volatile private var instance: EventRepository? = null

        fun getInstance() =
                instance ?: synchronized(this) {
                    instance ?: EventRepository().also { instance = it }
                }
    }

    fun fetchList(context: Context,page:Int) {
        if(isFetchInProgress) return
        isFetchInProgress = true

        val params = HashMap<String, String>()
        params["page"] = page.toString()
        params["timezone"] = TimeZone.getDefault().id


        APIHandler(
            context,
            Request.Method.GET,
            Constants.API.event,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {

                    isFetchInProgress = false

                    val data = result.getJSONObject("result")
                    Log.e("data is :-", data.toString())
                    if(data.has("total")){
                        totalCount = data.getInt("total")
                    }
                    val jsonArray = data.getJSONArray("events")
                    val items:ArrayList<Event> = ArrayList()
                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray.getJSONObject(i)
                        val review = Event(json)
                        items.add(review)
                    }
                    events.value=items
                    eventsList.addAll(items)

                }
                override fun onFail(error: String?) {

                    isFetchInProgress = false
                    Log.e("data error is :-", "$error")
                    shortToast(error)
                }
            })
    }

    fun clearData() {
        eventsList.clear()
    }

}
