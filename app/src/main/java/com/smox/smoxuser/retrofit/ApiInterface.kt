package com.smox.smoxuser.retrofit


import io.reactivex.Observable
import com.google.gson.*
import com.smox.smoxuser.model.*
import retrofit2.http.*

interface ApiInterface {

    @FormUrlEncoded
    @POST("customers/{stripe_cust_id}/sources")
    fun addNewCardOnStripe(
        @Path("stripe_cust_id") custId: String,
        @FieldMap token: MutableMap<String, Any>
    ): Observable<AddCardResponse>

    @FormUrlEncoded
    @POST("tokens")
    fun createCardToken(@FieldMap params: MutableMap<String, Any>): Observable<AddCardResponse>

    @GET("customers/{stripe_cust_id}/sources")
    fun getAllCardsOnStripe(@Path("stripe_cust_id") custId: String): Observable<SavedCardListResponse>

    @POST("charges")
    fun bookAppointment(@Body jsonObject: JsonObject): Observable<PaymentStartResponse>

    @POST("charges")
    fun confirmAppointment(@Body jsonObject: JsonObject): Observable<SimpleOkResponse1>

    @POST("charge_link")
    fun appointmentPayment(@Body jsonObject: JsonObject): Observable<SimpleOkResponse1>

    @POST("update_appointment")
    fun updateAppointment(@Body jsonObject: JsonObject): Observable<UpdateAppointmentResponse>

    @GET("products/{barberId}/{user_id}")
    fun getAllProducts(@Path("barberId") barberId: String, @Path("user_id") userId: String): Observable<Products>

    @POST("cart")
    fun addToCart(@Body jsonObject: JsonObject): Observable<AddCartResponse>

    @GET("cart")
    fun getCartList(): Observable<CartResponse>

    @GET("cart/{barberId}")
    fun getCartListByBarber(@Path("barberId") barberId: String): Observable<CartResponse>

    @POST("cart")
    fun updateCart(@Body jsonObject: JsonObject): Observable<SimpleOkResponse2>

    @GET("cart/barbers")
    fun getCartBarberList(): Observable<CartBarberResponse>

    @POST("deliveryAddress")
    fun addNewAddress(@Body jsonObject: JsonObject): Observable<SimpleOkResponse2>

    @GET("deliveryAddress")
    fun getAddressList(): Observable<AddressResponse>

    //@DELETE("deliveryAddress/delete")
    @HTTP(method = "DELETE", path = "deliveryAddress/delete", hasBody = true)
    fun deleteAddress(@Body jsonObject: JsonObject): Observable<SimpleOkResponse2>

    @POST("send_notification")
    fun sendChatNotification(@Body jsonObject: JsonObject): Observable<SimpleOkResponse2>

    @POST("checkout")
    fun checkout(@Body jsonObject: JsonObject): Observable<CartPaymentResponse>

    @POST("checkout")
    fun checkoutAgain(@Body jsonObject: JsonObject): Observable<SimpleOkResponse2>

    @GET("orderlist/customer/{status}")
    fun getOrderList(@Path("status") status: String,@Query("page") pageNumber:Int): Observable<OrderListResponse>

    @GET("order/{orderId}")
    fun getOrderDetailList(@Path("orderId") orderId: String): Observable<OrderDetailResponse>

    @POST("cancelOrder")
    fun cancelOrder(@Body jsonObject: JsonObject): Observable<SimpleOkResponse2>

    @POST("delete_account")
    fun deletAccount(): Observable<SimpleOkResponse2>
}