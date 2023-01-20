package com.smox.smoxuser.model

import com.google.gson.annotations.SerializedName

data class CartPaymentResponse(

    @SerializedName("error") val error: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: PaymentResultt
)

class PaymentResultt(
    @SerializedName("id") val id : String,
    @SerializedName("amount") val amount : Int,
    @SerializedName("amount_capturable") val amount_capturable : Int,
    @SerializedName("amount_received") val amount_received : Int,
    @SerializedName("application") val application : String,
    @SerializedName("application_fee_amount") val application_fee_amount : Int,
    @SerializedName("automatic_payment_methods") val automatic_payment_methods : String,
    @SerializedName("canceled_at") val canceled_at : String,
    @SerializedName("cancellation_reason") val cancellation_reason : String,
    @SerializedName("capture_method") val capture_method : String,
    @SerializedName("client_secret") val client_secret : String,
    @SerializedName("confirmation_method") val confirmation_method : String,
    @SerializedName("created") val created : Int,
    @SerializedName("currency") val currency : String,
    @SerializedName("customer") val customer : String,
    @SerializedName("description") val description : String,
    @SerializedName("invoice") val invoice : String,
    @SerializedName("last_payment_error") val last_payment_error : String,
    @SerializedName("livemode") val livemode : Boolean,
    @SerializedName("metadata") val metadata : List<String>,
    @SerializedName("next_action") val next_action : String,
    @SerializedName("on_behalf_of") val on_behalf_of : String,
    @SerializedName("payment_method") val payment_method : String,
    @SerializedName("payment_method_types") val payment_method_types : List<String>,
    @SerializedName("processing") val processing : String,
    @SerializedName("receipt_email") val receipt_email : String,
    @SerializedName("review") val review : String,
    @SerializedName("setup_future_usage") val setup_future_usage : String,
    @SerializedName("shipping") val shipping : String,
    @SerializedName("source") val source : String,
    @SerializedName("statement_descriptor") val statement_descriptor : String,
    @SerializedName("statement_descriptor_suffix") val statement_descriptor_suffix : String,
    @SerializedName("status") val status : String,
    @SerializedName("transfer_group") val transfer_group : String,
    @SerializedName("checkout_id") val checkout_id : List<Int>
)
