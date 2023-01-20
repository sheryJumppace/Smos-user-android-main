package com.smox.smoxuser.model

import com.smox.smoxuser.model.type.VerificationStatus
import org.json.JSONException
import org.json.JSONObject

import java.io.Serializable

open class ConnectAccount : Serializable {
    var id:String = ""
    var verificationStatus: VerificationStatus = VerificationStatus.None
    var disableReason:String = ""
    var currentlyDeadline = ArrayList<VerifyField>()
    var currentlyDue = ArrayList<VerifyField>()
    var eventuallyDue = ArrayList<VerifyField>()
    var pastDue = ArrayList<VerifyField>()

    constructor() : super()
    constructor(json: JSONObject){
        try {
            if (json.has("id")) {
                this.id = json.getString("id");
            }
            if (json.has("individual")) {
                val individual = json.getJSONObject("individual")
                if(individual.has("verification")){
                    val verification = individual.getJSONObject("verification")
                    if(verification.has("status")){
                        val status = verification.getString("status")
                        this.verificationStatus = VerificationStatus.valueOf(status.capitalize())
                    }
                }
            }
            if (json.has("requirements")){
                val requirements = json.getJSONObject("requirements")
                if(requirements.has("current_deadline")){
                    try {
                        val items = requirements.getJSONArray("current_deadline")
                        for (i in 0 until items.length()) {
                            val model = VerifyField(items.getString(i))
                            this.currentlyDeadline.add(model)
                        }
                    }catch (e:JSONException){

                    }

                }
                if(requirements.has("currently_due")){
                    try {
                        val items = requirements.getJSONArray("currently_due")
                        for (i in 0 until items.length()) {
                            val model = VerifyField(items.getString(i))
                            this.currentlyDue.add(model)
                        }
                    }catch (e:JSONException){

                    }
                }
                if(requirements.has("eventually_due")){
                    try {
                        val items = requirements.getJSONArray("eventually_due")
                        for (i in 0 until items.length()) {
                            val model = VerifyField(items.getString(i))
                            this.eventuallyDue.add(model)
                        }
                    }catch (e:JSONException){

                    }

                }
                if(requirements.has("past_due")){
                    try {
                        val items = requirements.getJSONArray("past_due")
                        for (i in 0 until items.length()) {
                            val model = VerifyField(items.getString(i))
                            this.pastDue.add(model)
                        }
                    }catch (e:JSONException){

                    }

                }
                if(requirements.has("disable_reason")){
                    this.disableReason = requirements.getString("disable_reason")
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    fun getReason(): String {
        val reason:String
        if(this.disableReason == ""){
            if(this.currentlyDue.count() > 0){
                reason = this.currentlyDue.map{it.title}.joinToString(", "){ it }
            }else{
                reason = this.verificationStatus.name
            }
        }else  if(this.disableReason == "requirements.past_due"){
            reason = this.pastDue.map{it.title}.joinToString(", "){ it }
        }else {
            reason = this.disableReason
        }
        return reason
    }
}
