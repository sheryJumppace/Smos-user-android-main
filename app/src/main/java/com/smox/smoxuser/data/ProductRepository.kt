package com.smox.smoxuser.data

import android.content.Context
import android.widget.Toast
import androidx.databinding.BaseObservable
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.kaopiz.kprogresshud.KProgressHUD
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Product
import com.smox.smoxuser.utils.shortToast
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

/**
 * Repository module for handling data operations.
 */
class ProductRepository(private var barberId: Int) : BaseObservable() {

    companion object {
        // For Singleton instantiation
        @Volatile
        private var instance: ProductRepository? = null

        private var barberIds: Int? = 0

        fun getInstance(barberId: Int): ProductRepository {
            val i = instance
            barberIds = barberId
            if (i != null) {
                return i
            }

            return synchronized(this) {
                instance ?: ProductRepository(barberId).also { instance = it }
            }
        }

       /* fun getInstance(barberId: Int): ProductRepository =
            instance ?: synchronized(this) {
                instance ?: ProductRepository(barberId).also { instance = it }
            }*/
    }

    var products: MutableLiveData<List<Product>> = MutableLiveData()

    fun getProduct(id: Int): Product? {
        return products.value?.find { it.id == id }
    }

    fun updateProduct(product: Product) {
        val items = products.value as ArrayList<Product>
        val s = items.find { it.id == product.id }
        if (s == null) {
            items.add(product)

        } else {
            s.image = product.image
            s.title = product.title
            s.price = product.price
            s.productDescription = product.productDescription
            s.shippingCharges = product.shippingCharges
            s.tax = product.tax
        }
        products.value = items
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
            Constants.API.product + "/" + barberIds,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    val jsonArray = result.getJSONArray("result")
                    val items: ArrayList<Product> = ArrayList()
                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray.getJSONObject(i)
                        val service = Product(json)
                        items.add(service)
                    }
                    products.value = items
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    fun deleteProduct(context: Context, productId: Int) {

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
            Constants.API.product + "/" + productId,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    val items = products.value as ArrayList<Product>
                    items.find { it.id == productId }?.apply {
                        items.remove(this)
                        products.value = items
                    }

                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }
}
