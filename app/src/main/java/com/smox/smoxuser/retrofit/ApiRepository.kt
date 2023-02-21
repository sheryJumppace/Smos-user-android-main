package com.smox.smoxuser.retrofit

import android.content.Context
import android.util.Log
import com.google.gson.JsonObject
import com.smox.smoxuser.model.*
import io.reactivex.Observable

class ApiRepository(private val context: Context) {

    private val apiService: RetrofitApiService = RetrofitApiService()


    fun addNewCardOnStripe(
        custId: String,
        token: MutableMap<String, Any>,
    ): Observable<AddCardResponse> {
        return apiService.getRetrofitServiceForAuth(context).addNewCardOnStripe(custId, token)
    }

    fun createCardToken(params: MutableMap<String, Any>): Observable<AddCardResponse> {
        return apiService.getRetrofitServiceForAuth(context).createCardToken(params)
    }

    fun getAllCardsOnStripe(custId: String): Observable<SavedCardListResponse> {
        return apiService.getRetrofitServiceForAuth(context).getAllCardsOnStripe(custId)
    }

    fun bookAppointment(
        jsonObject: JsonObject
    ): Observable<PaymentStartResponse> {


        Log.d("++--++","bookAppointment jsonobject\n$jsonObject")

        return apiService.getRetrofitServiceForAuthForApp(context)
            .bookAppointment(jsonObject)
    }

    fun confirmAppointment(
        jsonObject: JsonObject
    ): Observable<SimpleOkResponse1> {
        return apiService.getRetrofitServiceForAuthForApp(context)
            .confirmAppointment(jsonObject)
    }

    fun appointmentPayment(
        jsonObject: JsonObject
    ): Observable<SimpleOkResponse1> {
        return apiService.getRetrofitServiceForAuthForApp(context)
            .appointmentPayment(jsonObject)
    }

    fun updateAppointment(
        jsonObject: JsonObject
    ): Observable<UpdateAppointmentResponse> {
        return apiService.getRetrofitServiceForAuthForApp(context)
            .updateAppointment(jsonObject)
    }

    fun getAllProducts(
        barberId: String,
        userId: String
    ): Observable<Products> {
        return apiService.getRetrofitServiceForAuthForApp(context).getAllProducts(barberId, userId)
    }

    fun addToCart(
        jsonObject: JsonObject
    ): Observable<AddCartResponse> {
        return apiService.getRetrofitServiceForAuthForApp(context).addToCart(jsonObject)
    }

    fun getCartList(
    ): Observable<CartResponse> {
        return apiService.getRetrofitServiceForAuthForApp(context).getCartList()
    }

    fun getCartListByBarber(
        barberId: String
    ): Observable<CartResponse> {
        return apiService.getRetrofitServiceForAuthForApp(context).getCartListByBarber(barberId)
    }

    fun updateCart(
        jsonObject: JsonObject
    ): Observable<SimpleOkResponse2> {
        return apiService.getRetrofitServiceForAuthForApp(context).updateCart(jsonObject)
    }

    fun getCartBarberList(
    ): Observable<CartBarberResponse> {
        return apiService.getRetrofitServiceForAuthForApp(context).getCartBarberList()
    }

    fun addNewAddress(
        jsonObject: JsonObject
    ): Observable<SimpleOkResponse2> {
        return apiService.getRetrofitServiceForAuthForApp(context).addNewAddress(jsonObject)
    }

    fun getAddressList(
    ): Observable<AddressResponse> {
        return apiService.getRetrofitServiceForAuthForApp(context).getAddressList()
    }

    fun deleteAddress(
        jsonObject: JsonObject
    ): Observable<SimpleOkResponse2> {
        return apiService.getRetrofitServiceForAuthForApp(context).deleteAddress(jsonObject)
    }

    fun sendChatNotification(
        jsonObject: JsonObject
    ): Observable<SimpleOkResponse2> {
        return apiService.getRetrofitServiceForAuthForApp(context).sendChatNotification(jsonObject)
    }

    fun checkout(
        jsonObject: JsonObject
    ): Observable<CartPaymentResponse> {
        return apiService.getRetrofitServiceForAuthForApp(context).checkout(jsonObject)
    }

    fun checkoutAgain(
        jsonObject: JsonObject
    ): Observable<SimpleOkResponse2> {
        return apiService.getRetrofitServiceForAuthForApp(context).checkoutAgain(jsonObject)
    }

    fun getOrderList(
        status: String, page: Int
    ): Observable<OrderListResponse> {
        return apiService.getRetrofitServiceForAuthForApp(context).getOrderList(status, page)
    }

    fun getOrderDetailList(
        orderId: String
    ): Observable<OrderDetailResponse> {
        return apiService.getRetrofitServiceForAuthForApp(context).getOrderDetailList(orderId)
    }

    fun cancelOrder(
        jsonObject: JsonObject
    ): Observable<SimpleOkResponse2> {
        return apiService.getRetrofitServiceForAuthForApp(context).cancelOrder(jsonObject)
    }

    fun deleteAccount(): Observable<SimpleOkResponse2> {
        return apiService.getRetrofitServiceForAuthForApp(context).deletAccount()
    }

}