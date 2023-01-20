
package com.smox.smoxuser.data

import android.content.Context
import android.widget.Toast
import androidx.databinding.BaseObservable
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.kaopiz.kprogresshud.KProgressHUD
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.App
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Review
import com.smox.smoxuser.utils.shortToast
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

/**
 * Repository module for handling data operations.
 */
class ReviewRepository: BaseObservable() {
    private var isFetchInProgress = false

    var totalCount:Int? = null
    var page:Int = 0

    var isUpdated:MutableLiveData<Boolean> = MutableLiveData()
    var reviews:ArrayList<Review> = ArrayList<Review>()

    fun getReview(id:Int): Review? {
        return reviews.find { it.id == id }
    }

    fun getReviews(): MutableLiveData<List<Review>> {
        return MutableLiveData(reviews)
    }

    companion object {

        // For Singleton instantiation
        @Volatile private var instance: ReviewRepository? = null

        fun getInstance() =
                instance ?: synchronized(this) {
                    instance ?: ReviewRepository().also { instance = it }
                }
    }

    fun fetchList(context: Context, barberId:Int) {
        if(isFetchInProgress) return
        if(totalCount != null && totalCount!! <= reviews.size){
            return
        }
        isFetchInProgress = true

        val params = HashMap<String, String>()
        params["barber_id"] = barberId.toString()
        params["page"] = page.toString()

        val progressHUD = KProgressHUD(context)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        progressHUD.show()

        APIHandler(
            context,
            Request.Method.GET,
            Constants.API.review,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    page += 1
                    isFetchInProgress = false

                    val data = result.getJSONObject("result")
                    if(data.has("total")){
                        totalCount = data.getInt("total")
                    }
                    val jsonArray = data.getJSONArray("reviews")
                    val items:ArrayList<Review> = ArrayList()
                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray.getJSONObject(i)
                        val review = Review(json)
                        items.add(review)
                    }
                    reviews.addAll(items)
                    isUpdated.postValue(true)

                }
                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    isFetchInProgress = false
                    shortToast(error)
                }
            })
    }

    fun addReview(context: Context, comment: String, clean: Int,work: Int,behave: Int, barberId: Int, appointId:Int){
        val user = App.instance.currentUser

        val params = HashMap<String, String>()
        params["comment"] = comment
        params["clean_rating"] = clean.toString()
        params["work_rating"] = work.toString()
        params["behave_rating"] = behave.toString()
        params["barber_id"] = barberId.toString()
        params["appointment_id"] = appointId.toString()

        for ((key, value) in params) {
            println("$key = $value")
        }

        val progressHUD = KProgressHUD(context)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        progressHUD.show()
        progressHUD.show()

        APIHandler(
            context,
            Request.Method.POST,
            Constants.API.review,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    isUpdated.postValue(true)

                }
                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }
}
