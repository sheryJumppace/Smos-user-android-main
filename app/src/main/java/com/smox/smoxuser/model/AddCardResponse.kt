package com.smox.smoxuser.model

import com.google.gson.annotations.SerializedName

data class AddCardResponse(
    @SerializedName("id") val id: String,
    @SerializedName("object") val cardType: String,
    @SerializedName("card") val card: StripeCard,
    @SerializedName("client_ip") val client_ip: String,
    @SerializedName("created") val created: Int,
    @SerializedName("livemode") val livemode: Boolean,
    @SerializedName("type") val type: String,
    @SerializedName("used") val used: Boolean
) {

    class StripeCard(
        @SerializedName("id") val id: String,
        @SerializedName("object") val cardType: String,
        @SerializedName("address_city") val address_city: String,
        @SerializedName("address_country") val address_country: String,
        @SerializedName("address_line1") val address_line1: String,
        @SerializedName("address_line1_check") val address_line1_check: String,
        @SerializedName("address_line2") val address_line2: String,
        @SerializedName("address_state") val address_state: String,
        @SerializedName("address_zip") val address_zip: String,
        @SerializedName("address_zip_check") val address_zip_check: String,
        @SerializedName("brand") val brand: String,
        @SerializedName("country") val country: String,
        @SerializedName("cvc_check") val cvc_check: String,
        @SerializedName("dynamic_last4") val dynamic_last4: String,
        @SerializedName("exp_month") val exp_month: Int,
        @SerializedName("exp_year") val exp_year: Int,
        @SerializedName("fingerprint") val fingerprint: String,
        @SerializedName("funding") val funding: String,
        @SerializedName("last4") val last4: Int,
        @SerializedName("metadata") val metadata: Metadataa,
        @SerializedName("name") val name: String,
        @SerializedName("tokenization_method") val tokenization_method: String
    )

    class Metadataa ()
}