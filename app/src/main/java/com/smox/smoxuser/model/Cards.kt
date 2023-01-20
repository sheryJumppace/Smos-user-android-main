package com.smox.smoxuser.model

import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

class Cards:Serializable {

     var UserCardId: String? = null
     var UserId: String? = null
     var StripeCardId: String? = null
     var StripeCardToken: String? = null
     var StripeCustomerId: String? = null
     var CardHolderName: String? = null
     var CardMonth: String? = null
     var CardYear: String? = null
     var CardFingerprint: String? = null
     var CardLastFourDigit: String? = null
     var CardBrand: String? = null
     var IsRemoved: String? = null
     var created_at: String? = null
     var updated_at: String? = null
    

    constructor() : super()
    constructor(json: JSONObject) : super() {
        try {
            if (json.has("UserCardId")) {
                this.UserCardId = json.getString("UserCardId")
            }
            if (json.has("UserId")) {
                this.UserId = json.getString("UserId")
            }
            if (json.has("StripeCardId")) {
                this.StripeCardId = json.getString("StripeCardId")
            }
            if (json.has("StripeCardToken")) {
                this.StripeCardToken = json.getString("StripeCardToken")
            }
            if (json.has("CardLastFourDigit")) {
                this.CardLastFourDigit = json.getString("CardLastFourDigit")
            }
            if (json.has("StripeCustomerId")) {
                this.StripeCustomerId = json.getString("StripeCustomerId")
            }
            if (json.has("CardHolderName")) {
                this.CardHolderName = json.getString("CardHolderName")
            }
            if (json.has("CardMonth")) {
                this.CardMonth = json.getString("CardMonth")
            }
            if (json.has("CardYear")) {
                this.CardYear = json.getString("CardYear")
            }
            if (json.has("CardFingerprint")) {
                this.CardFingerprint = json.getString("CardFingerprint")
            }

            if (json.has("IsRemoved")) {
                this.IsRemoved = json.getString("IsRemoved")
            }

            if (json.has("CardBrand")) {
                this.CardBrand = json.getString("CardBrand")
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}