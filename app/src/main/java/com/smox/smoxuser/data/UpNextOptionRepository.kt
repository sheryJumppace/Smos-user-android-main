
package com.smox.smoxuser.data

import android.content.Context
import android.widget.Toast
import androidx.databinding.BaseObservable
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.kaopiz.kprogresshud.KProgressHUD
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.UpNextOption
import com.smox.smoxuser.model.type.UpNextStatus
import com.smox.smoxuser.utils.shortToast
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

/**
 * Repository module for handling data operations.
 */
class UpNextOptionRepository: BaseObservable() {

    companion object {
        // For Singleton instantiation
        @Volatile private var instance: UpNextOptionRepository? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: UpNextOptionRepository().also { instance = it }
            }

    }
    var options:MutableLiveData<List<UpNextOption>> = MutableLiveData()

    fun getOption(position:Int): UpNextOption? {
        return options.value?.get(position)
    }

    fun fetchList(context: Context) {

        val params = HashMap<String, String>()
        val progressHUD = KProgressHUD(context)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        progressHUD.show()

        APIHandler(
            context,
            Request.Method.GET,
            Constants.API.upnext_option,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    val jsonArray = result.getJSONArray("result")
                    val items:ArrayList<UpNextOption> = ArrayList()
                    val auto = UpNextOption(UpNextStatus.Automatically.name)
                    items.add(auto)
                    val pause = UpNextOption(UpNextStatus.Paused.name)
                    items.add(pause)

                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray.getJSONObject(i)
                        val service = UpNextOption(json)
                        items.add(service)
                    }
                    options.value = items
                }
                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    fun addOption(context: Context, title:String) {

        val params = HashMap<String, String>()
        params["title"] = title
        val progressHUD = KProgressHUD(context)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        progressHUD.show()

        APIHandler(
            context,
            Request.Method.POST,
            Constants.API.upnext_option,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    val id = result.getInt("result")
                    val option = UpNextOption(title)
                    option.id = id
                    option.isStatic = false

                    val items = options.value as ArrayList<UpNextOption>
                    items.add(option)

                    options.value = items

                }
                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }
    fun deleteOption(context: Context, position: Int) {
        val option = getOption(position) ?: return
        val id = option.id
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
            Constants.API.upnext_option + "/" + id,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    val items = options.value as ArrayList<UpNextOption>
                    items.find { it.id == id }?.apply {
                        items.remove(this)
                        options.value = items
                    }

                }
                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }
}
