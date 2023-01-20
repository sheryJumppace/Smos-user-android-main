package com.smox.smoxuser.data

import android.content.Context
import android.widget.Toast
import androidx.databinding.BaseObservable
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.kaopiz.kprogresshud.KProgressHUD
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Address
import com.smox.smoxuser.utils.shortToast
import org.json.JSONObject

class AddressRepository (private val addressId: Int): BaseObservable(){
    companion object{
        @Volatile private var instance : AddressRepository? = null

        fun getInstance(addressId: Int) =
            instance?: synchronized(this){
                instance?: AddressRepository(addressId).also { instance = it }
            }
    }

    var addresses: MutableLiveData<List<Address>> = MutableLiveData()
    var deleteAddress: MutableLiveData<JSONObject> = MutableLiveData()

    fun fetchList(context: Context){
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
            Constants.API.myaddress,
            params,
            object: APIHandler.NetworkListener{
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    val jsonArray = result.getJSONArray("result")
                    val items:ArrayList<Address> = ArrayList()
                    for (i in 0 until jsonArray.length()){
                        val json = jsonArray.getJSONObject(i)
                        val address = Address(json)
                        items.add(address)
                    }
                    addresses.value = items
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }

            }
        )
    }

    fun deleteAddress(context: Context, addressId: String){
        val params = HashMap<String, String>()
        params["id"] = addressId

        val progressHUD = KProgressHUD(context)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        progressHUD.show()

        APIHandler(
            context,
            Request.Method.POST,
            Constants.API.delete_address,
            params,
            object: APIHandler.NetworkListener{
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    deleteAddress.value = result
                    if(result.has("message"))
                    {

                        shortToast(result.getString("message"))
                    }
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)

                }

            }
        )
    }
}