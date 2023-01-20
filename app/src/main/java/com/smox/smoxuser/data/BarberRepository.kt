package com.smox.smoxuser.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.databinding.BaseObservable
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.google.android.gms.maps.model.LatLng
import com.smox.smoxuser.App
import com.smox.smoxuser.HolidayRes
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Barber
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.google.gson.Gson
import com.smox.smoxuser.manager.SessionManager
import com.smox.smoxuser.utils.shortToast


/**
 * Repository module for handling data operations.
 */
class BarberRepository : BaseObservable() {

    var barbers: MutableLiveData<List<Barber>> = MutableLiveData()
    var mainBarbersList = arrayListOf<Barber>()
    var favBarbers: MutableLiveData<List<Barber>> = MutableLiveData()
    var barber: MutableLiveData<Barber> = MutableLiveData()
    lateinit var barber1:Barber
    var barberHolidayList: MutableLiveData<ArrayList<HolidayRes.Holidays>> = MutableLiveData()


    fun getBarber(id: Int): Barber? {
        return mainBarbersList.find { it.id == id }
    }

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: BarberRepository? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: BarberRepository().also {
                    instance = it
                }
            }
    }

    fun clearMainList() {
        mainBarbersList.clear()
        Log.e("TAG", "clearMainList: barber ")
    }

    fun fetchList(
        context: Context,
        location: LatLng? = null,
        query: String? = null,
        isFavorite: Boolean = false,
        page: String
    ) {
        val dateFormat = SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
        //val date = dateFormat.format(Date())
        val date = Constants.convertLocalToUTC(Date(), dateFormat)

        val latLng = location ?: App.instance.myLocation
        val params = HashMap<String, String>()
        query?.apply {
            params["search"] = this
        }

        if (latLng != null) {
            params["lat"] = latLng.latitude.toString()
            params["lng"] = latLng.longitude.toString()
        } else {
            params["lat"] = "0"
            params["lng"] = "0"
        }

        val sessionManager=SessionManager.getInstance(context)
        params["user_id"] =sessionManager.userId.toString()
        params["range"] = "50"
        params["favorite"] = if (isFavorite) "1" else "0"
        params["date"] = date
        params["page"] = page

        APIHandler(context,
            Request.Method.GET,
            Constants.API.barbers,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {

                    Log.e("TAG", " isFav $isFavorite")
                    Log.e("TAG", "barber list onResult: "+result.getJSONArray("result") )
                    val jsonArray = result.getJSONArray("result")
                    val items: ArrayList<Barber> = ArrayList()
                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray.getJSONObject(i)
                        val barber = Barber(json)
                        if (barber.id > 0) {
                            items.add(barber)
                        }
                    }
                    if (isFavorite) {
                        Log.e("TAG", "onResult: favorite ${items.size}")
                        favBarbers.value = items
                    } else {
                        Log.e("TAG", "onResult: simple ${items.size}")
                        barbers.value = items
                    }
                    mainBarbersList.addAll(items)
                }

                override fun onFail(error: String?) {
                    shortToast(error)
                }
            })
    }

    fun fetchBarberDetail(context: Context, id: Int) {

        val curDateFormat = SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
        val curDate = curDateFormat.format(Date())

        val params = HashMap<String, String>()
        params["id"] = id.toString()
        params["date"] = curDate
        params["lat"] = App.instance.myLocation?.latitude.toString()
        params["lng"] = App.instance.myLocation?.longitude.toString()
        val sessionManager=SessionManager.getInstance(context)
        params["user_id"] = sessionManager.userId.toString()

        APIHandler(context,
            Request.Method.GET,
            Constants.API.barberById,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {

                    val jsonArray = result.getJSONArray("result")
                    if (jsonArray.length() > 0) {
                        val json = jsonArray.getJSONObject(0)
                        barber.value = Barber(json)
                        barber1=Barber(json)
                    }
                }

                override fun onFail(error: String?) {
                    shortToast(error)

                }
            })
    }

    fun fetchBarberHoiliday(context: Context, barberId: Int) {
        val params = HashMap<String, String>()

        APIHandler(context,
            Request.Method.GET,
            Constants.API.barberHolidayList+"/"+barberId,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {

                    if (result.getString("holidays")=="null"){
                        Log.e("TAG", "onResult: " )
                        barberHolidayList= MutableLiveData()
                        barberHolidayList.value=null
                        return
                    }
                    val jsonArray = result.getJSONArray("holidays")
                    if (jsonArray.length() > 0) {
                        val gson = Gson()
                        val holiData: HolidayRes = gson.fromJson(result.toString(), HolidayRes::class.java)

                        barberHolidayList= MutableLiveData()
                        barberHolidayList.value=holiData.holidays as ArrayList<HolidayRes.Holidays>?

                        Log.e("TAG", "onResult: my holiday ${holiData.holidays[0].date}" )

                    }
                }

                override fun onFail(error: String?) {
                    shortToast(error)
                }
            })
    }


}
