package com.smox.smoxuser.viewmodel

import android.content.Context
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.smox.smoxuser.R
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.model.PaymentStartResponse
import com.smox.smoxuser.model.SimpleOkResponse1
import com.smox.smoxuser.retrofit.ApiRepository
import com.smox.smoxuser.utils.shortToast
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class PaymentViewModel : ViewModel() {

    var isSuccess = MutableLiveData<Boolean>()
    var errorMessage = ObservableField<String>()
    var payStartRes = ObservableField<PaymentStartResponse>()
    var isBookingConfirmed = MutableLiveData<Boolean>()
    var isConfirmDone = MutableLiveData<Boolean>()

    fun bookAppointment(
        context: Context, isFreshBooking: Boolean, jsonObject: JsonObject
    ) {
        ApiRepository(context).bookAppointment(jsonObject).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<PaymentStartResponse> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(res: PaymentStartResponse) {
                    Log.e("++--++", "41 PaymentViewModel onNext: ${Gson().toJson(res)}")
                    if (res.error) {
                        errorMessage.set(res.message)
                        isSuccess.postValue(false)
                    } else {
                        payStartRes.set(res)
                        isSuccess.postValue(true)
                    }
                }

                override fun onError(e: Throwable) {
                    Log.e("++--++", "52 PaymentViewModel onError: ${e.message}")
                    try {
                        errorMessage.set(e.message)
                        isSuccess.postValue(false)
                        if ((e as HttpException).code() == 401) {
                            shortToast(context.getString(R.string.authError))
                            APIHandler(context).logout()
                        } else shortToast(e.message())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onComplete() {

                }

            })
    }

    fun confirmAppointment(context: Context, jsonObject: JsonObject) {
        ApiRepository(context).confirmAppointment(jsonObject).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<SimpleOkResponse1> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(res: SimpleOkResponse1) {

                    if (res.error) {
                        errorMessage.set(res.message)
                        isBookingConfirmed.postValue(false)

                    } else {
                        isBookingConfirmed.postValue(true)
                    }
                }

                override fun onError(e: Throwable) {
                    try {
                        Log.e("TAG", "onError: ${e.message}")
                        errorMessage.set(e.message)
                        isBookingConfirmed.postValue(false)
                        if ((e as HttpException).code() == 401) {
                            shortToast(context.getString(R.string.authError))
                            APIHandler(context).logout()
                        } else shortToast(e.message())
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }

                override fun onComplete() {

                }

            })
    }

    fun confirmAppointmentPayment(context: Context, jsonObject: JsonObject) {
        ApiRepository(context).confirmAppointment(jsonObject).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<SimpleOkResponse1> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(res: SimpleOkResponse1) {
                    Log.e("++--++", "onNext: ${Gson().toJson(res)}")

                    if (res.error) {
                        errorMessage.set(res.message)
                        isConfirmDone.postValue(false)
                    } else {
                        isConfirmDone.postValue(true)
                    }
                }

                override fun onError(e: Throwable) {


                    try {
                        Log.e("++--++", "onError: ${e.message}")
                        errorMessage.set(e.message)
                        isConfirmDone.postValue(false)
                        if ((e as HttpException).code() == 401) {
                            shortToast(context.getString(R.string.authError))
                            APIHandler(context).logout()
                        } else shortToast(e.message())

                    } catch (EX: Exception) {
                        e.printStackTrace()
                        shortToast("Something went wrong")
                    }


                }

                override fun onComplete() {

                }

            })
    }

}