package com.smox.smoxuser.data

import android.content.Context
import android.widget.Toast
import androidx.databinding.BaseObservable
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.kaopiz.kprogresshud.KProgressHUD
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Orders
import com.smox.smoxuser.utils.shortToast
import org.json.JSONObject

class OrdersRepository (): BaseObservable() {
    companion object{
        @Volatile private var instance: OrdersRepository? =null

        fun getInsance() =
            instance?: synchronized(this){
                instance?: OrdersRepository().also { instance = it }
            }
    }

    var productOrders: MutableLiveData<ArrayList<Orders>> = MutableLiveData()
    var productStatus: MutableLiveData<String> = MutableLiveData()

    fun fetchList(context: Context, orderUrl: String){
        val params= HashMap<String, String>()
//        val progressHUD = KProgressHUD(context)
//        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
//            .setCancellable(true)
//            .setAnimationSpeed(2)
//            .setDimAmount(0.5f)
//        progressHUD.show()

        APIHandler(
            context,
            Request.Method.GET,
            orderUrl,
            params,
            object: APIHandler.NetworkListener{
                override fun onResult(result: JSONObject) {
                   // progressHUD.dismiss()
                    val jsonArray = result.getJSONArray("result")
                    val items: ArrayList<Orders> = ArrayList()
                    for (i in 0 until  jsonArray.length()){
                        val json = jsonArray.getJSONObject(i)
                        val orders = Orders(json)
                        items.add(orders)
                    }
                    productOrders.value = items
                }

                override fun onFail(error: String?) {
            //        progressHUD.dismiss()
                    shortToast(error)

                }

            }

        )

    }

    fun changeProductStatus(context: Context, mParams: HashMap<String, String>){
        val params= mParams
        val progressHUD = KProgressHUD(context)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        progressHUD.show()

        APIHandler(
            context,
            Request.Method.POST,
            Constants.API.mark_delivered,
            params,
            object: APIHandler.NetworkListener{
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    /*val jsonArray = result.getJSONArray("result")
                    val items: ArrayList<Orders> = ArrayList()
                    for (i in 0 until  jsonArray.length()){
                        val json = jsonArray.getJSONObject(i)
                        val orders = Orders(json)
                        items.add(orders)
                    }*/
                    productStatus.value = result.toString()
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }

            }

        )

    }
}