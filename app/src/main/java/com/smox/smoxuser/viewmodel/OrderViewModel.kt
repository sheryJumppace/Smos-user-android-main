package com.smox.smoxuser.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.kaopiz.kprogresshud.KProgressHUD
import com.smox.smoxuser.R
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.model.*
import com.smox.smoxuser.retrofit.ApiRepository
import com.smox.smoxuser.utils.shortToast
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class OrderViewModel : ViewModel() {

    var orderItem = MutableLiveData<ArrayList<OrderItem>>()
    var orderDetailItem = MutableLiveData<ArrayList<OrderDetailItem>>()
    var orderCancelled = MutableLiveData<Boolean>(false)

    fun getUpcomingOrders(context: Context, progressBar: KProgressHUD, status: String, page:Int) {
        progressBar.show()
        ApiRepository(context).getOrderList(status, page)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<OrderListResponse> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(res: OrderListResponse) {
                    progressBar.dismiss()

                    if (!res.error) {
                        orderItem.postValue(res.result as ArrayList<OrderItem>?)
                    } else {
                        shortToast(res.message)
                    }
                }

                override fun onError(e: Throwable) {
                    progressBar.dismiss()
                    Log.e("TAG", "onError: ${e.message}")
                    if ((e as HttpException).code()==401){
                        shortToast(context.getString(R.string.authError))
                        APIHandler(context).logout()
                    }
                    else
                        shortToast(e.message())
                }

                override fun onComplete() {

                }
            })
    }

    fun getOrderProductList(context: Context, orderId: String) {
        ApiRepository(context).getOrderDetailList(orderId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<OrderDetailResponse> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(res: OrderDetailResponse) {
                    if (!res.error) {
                        orderDetailItem.postValue(res.result as ArrayList<OrderDetailItem>?)
                    } else {
                        shortToast(res.message)
                    }
                }

                override fun onError(e: Throwable) {
                    Log.e("TAG", "onError: ${e.message}")
                    if ((e as HttpException).code()==401){
                        shortToast(context.getString(R.string.authError))
                        APIHandler(context).logout()
                    }
                    else
                        shortToast(e.message())
                }

                override fun onComplete() {

                }
            })
    }

    fun cancelOrder(context: Context, progressBar: KProgressHUD, orderId: String) {
        progressBar.show()
        val jsonObject=JsonObject()
        jsonObject.addProperty("order_id",orderId)
        ApiRepository(context).cancelOrder(jsonObject)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<SimpleOkResponse2> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(res: SimpleOkResponse2) {
                    progressBar.dismiss()
                    if (!res.error) {
                       orderCancelled.postValue(true)
                    } else {
                        shortToast(res.message)
                    }
                }

                override fun onError(e: Throwable) {
                    progressBar.dismiss()
                    Log.e("TAG", "onError: ${e.message}")
                    if ((e as HttpException).code()==401) {
                        shortToast(context.getString(R.string.authError))
                        APIHandler(context).logout()
                    }
                    else
                        shortToast(e.message())
                }

                override fun onComplete() {

                }
            })
    }
}