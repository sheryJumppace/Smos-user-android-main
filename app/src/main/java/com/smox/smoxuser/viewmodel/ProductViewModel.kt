package com.smox.smoxuser.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.JsonObject
import com.kaopiz.kprogresshud.KProgressHUD
import com.smox.smoxuser.R
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.*
import com.smox.smoxuser.retrofit.ApiRepository
import com.smox.smoxuser.utils.shortToast
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class ProductViewModel : ViewModel() {

    var productRes = MutableLiveData<Products>()
    var cartData = MutableLiveData<CartData>()
    var cartCount = ObservableField(0)
    var isCartAdded = MutableLiveData(false)
    var barberId = MutableLiveData("0")
    var userId = MutableLiveData("-1")
    var isCheckoutSuccess=MutableLiveData(false)
    var isOrderPlaced=MutableLiveData(false)
    var isQtyUpdate=MutableLiveData(false)
    var paymentIntentData=ObservableField<PaymentResultt>()

    fun getAllProducts(
        context: Context,
        progressBar: KProgressHUD,
    ) {
        progressBar.show()
        ApiRepository(context).getAllProducts(barberId.value!!, userId.value.toString())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Products> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(res: Products) {
                    progressBar.dismiss()
                    if (res.error) {
                        shortToast(res.message)
                    } else {
                        productRes.postValue(res)
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

    fun addToCart(context: Context, progressBar: KProgressHUD, jsonObject: JsonObject) {
        progressBar.show()
        ApiRepository(context).addToCart(jsonObject)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<AddCartResponse> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(res: AddCartResponse) {
                    progressBar.dismiss()
                    shortToast(res.message)
                    if (!res.error) {
                        isCartAdded.postValue(true)
                    } else
                        isCartAdded.postValue(false)
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

    fun getCartListByBarber(context: Context, progressBar: KProgressHUD) {
        if (!progressBar.isShowing)
            progressBar.show()
        ApiRepository(context).getCartListByBarber(barberId.value!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<CartResponse> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(res: CartResponse) {
                    progressBar.dismiss()

                    if (!res.error) {
                        cartData.postValue(res.result)
                        cartCount.set(res.result.cart_count)
                    } else {
                        shortToast(res.message)
                        cartCount.set(0)
                    }
                }

                override fun onError(e: Throwable) {
                    progressBar.dismiss()
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

    fun updateCartQuantity(
        context: Context,
        progressBar: KProgressHUD,
        jsonObject: JsonObject,
        isAddedFromDetail: Boolean
    ) {
        progressBar.show()
        ApiRepository(context).updateCart(jsonObject)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<SimpleOkResponse2> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(res: SimpleOkResponse2) {

                    if (!res.error) {
                        if (!isAddedFromDetail)
                            getCartListByBarber(context, progressBar)
                        else
                            progressBar.dismiss()
                    } else {
                        progressBar.dismiss()
                        isQtyUpdate.postValue(true)
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

    fun callCheckoutApi(context: Context, progressBar: KProgressHUD, jsonObject: JsonObject) {
        progressBar.show()
        ApiRepository(context).checkout(jsonObject)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<CartPaymentResponse> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(res: CartPaymentResponse) {
                    progressBar.dismiss()
                    if (!res.error) {
                        paymentIntentData.set(res.result)
                        isCheckoutSuccess.postValue(true)
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

    fun confirmPayment(context: Context, progressBar: KProgressHUD, jsonObject: JsonObject) {
        progressBar.show()
        ApiRepository(context).checkoutAgain(jsonObject)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<SimpleOkResponse2> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(res: SimpleOkResponse2) {
                    progressBar.dismiss()
                    if (!res.error) {
                        isOrderPlaced.postValue(true)
                    }else
                        shortToast(res.message)
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
}