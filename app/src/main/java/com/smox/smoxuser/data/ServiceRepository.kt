package com.smox.smoxuser.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.databinding.BaseObservable
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.kaopiz.kprogresshud.KProgressHUD
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Service
import com.smox.smoxuser.utils.shortToast
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

/**
 * Repository module for handling data operations.
 */
class ServiceRepository(private val barberId: Int) : BaseObservable() {

    companion object {
        // For Singleton instantiation
        @Volatile
        private var instance: ServiceRepository? = null

        private var barberIds: Int? = 0

        fun getInstance(barberId: Int): ServiceRepository {
            val i = instance
            barberIds = barberId
            if (i != null) {
                return i
            }

            return synchronized(this) {
                instance
                    ?: ServiceRepository(barberId).also { instance = it }
            }
        }

        /* fun getInstance(barberId:Int) =
             instance ?: synchronized(this) {
                 instance ?: ServiceRepository(barberId).also { instance = it }
             }*/
    }

    var isUpdated: MutableLiveData<Boolean> = MutableLiveData()
    var services: ArrayList<Service> = ArrayList<Service>()

    fun getService(id: Int): Service? {
        return services.find { it.id == id }
    }

    fun getServices(): MutableLiveData<List<Service>> {
        return MutableLiveData(services)
    }

    fun didDeselectAll() {
        for (contact in services) {
            contact.isSelected.set(false)
        }
        isUpdated.postValue(true)
    }

    fun getSelectedServices(): List<Service> {
        return services.filter { contact -> contact.isSelected.get() }
    }

    fun deleteService(service: Service) {
        try {
            if (services.indexOf(service) != -1)
                services.removeAt(services.indexOf(service))
        } catch (e: Exception) {

        }
        isUpdated.postValue(true)
    }

    fun updateService(service: Service) {
        val s = getService(service.id)
        if (s == null) {
            services.add(service)
        } else {
            s.image = service.image
            s.title = service.title
            s.duration = service.duration
            s.price = service.price
            s.serviceDescription = service.serviceDescription
        }
        isUpdated.postValue(true)
    }

    fun updateServices(services: ArrayList<Service>) {
        this.services = services
        isUpdated.postValue(true)
    }


    fun fetchList(context: Context) {

        val params = HashMap<String, String>()
        params["barber_id"] = barberIds.toString()

        val progressHUD = KProgressHUD(context)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        progressHUD.show()

        APIHandler(
            context,
            Request.Method.GET,
            Constants.API.service,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    val jsonArray = result.getJSONArray("result")
                    val items: ArrayList<Service> = ArrayList()
                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray.getJSONObject(i)
                        val service = Service(json)
                        items.add(service)
                    }
                    services = items
                    isUpdated.postValue(true)
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    fun sendReorderService(context: Context, data: JSONObject) {
        /*val progressHUD = KProgressHUD(context)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        progressHUD.show()*/

        APIHandler(
            context,
            Request.Method.POST,
            Constants.API.rearrange_service,
            data,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    Log.e("Data Json Object Result", result.toString())
                    //progressHUD.dismiss()
                    //parseResult(result, context)
                }

                override fun onFail(error: String?) {
                    Log.e("Data Json Object Result", error.toString())
                }
            },
            "json"
        )
    }

    fun deleteServiceFromServer(context: Context, serviceId: Int) {

        val params = HashMap<String, String>()

        val progressHUD = KProgressHUD(context)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        progressHUD.show()

        APIHandler(
            context,
            Request.Method.DELETE,
            Constants.API.service + "/" + serviceId,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    services.find { it.id == serviceId }?.apply {
                        services.remove(this)
                        isUpdated.postValue(true)
                    }

                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }
}
