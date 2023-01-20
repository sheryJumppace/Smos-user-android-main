package com.smox.smoxuser.model

import androidx.databinding.ObservableBoolean
import androidx.room.Ignore
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.type.LoginType
import com.smox.smoxuser.model.type.UserType
import org.json.JSONException
import org.json.JSONObject

import java.io.Serializable
import java.util.ArrayList

@Suppress("UNUSED_PARAMETER")
open class SmoxUser : Serializable {

    var id:Int = 0
    var email:String = ""
    var phone:String = ""
    var firstName:String = ""
    var lastName:String = ""
    var image:String = ""
    var accountType: UserType = UserType.None
    var loginType: LoginType = LoginType.Email
    @Ignore
    var isFavorite:ObservableBoolean = ObservableBoolean(false)
    var stripe_url:String = ""
    var website:String = ""
    var isWebsiteVisible:ObservableBoolean = ObservableBoolean(false)
    var stripe_public_key:String = ""
    var stripe_secret_key:String = ""
    var stripe_client_key:String = ""
    var contacts = ArrayList<SmoxUser>()

    @Ignore
    var isSelected:ObservableBoolean = ObservableBoolean(false)

    constructor() : super()
    constructor(json: JSONObject) {
        try {
            if (json.has("id")) {
                id = json.getInt("id")
            }
            
            if (json.has("first_name")){
                firstName = json.getString("first_name")
            }
            if (json.has("last_name")) {
                lastName = json.getString("last_name")
            }
            if (json.has("email")) {
                email = json.getString("email")
            }
            if (json.has("phone")) {
                phone = json.getString("phone")
            }
            if (json.has("image")) {
                image = json.getString("image")
            }
            if (json.has("account_type")) {
                val type = json.getString("account_type")
                accountType = UserType.valueOf(type.capitalize())
            }
            if (json.has("login_type")) {
                val type = json.getString("login_type")
                loginType = LoginType.valueOf(type.capitalize())
            }
            if (json.has("favorite")) {
                this.isFavorite.set(json.getInt("favorite") > 0)
            }
            if (json.has("stripe_url")) {
                this.stripe_url = json.getString("stripe_url")
            }
            if (json.has("stripe_public_key")) {
                this.stripe_public_key = json.getString("stripe_public_key")
            }
            if (json.has("stripe_secret_key")) {
                this.stripe_secret_key = json.getString("stripe_secret_key")
            }

            if (json.has("stripe_client_key")) {
                this.stripe_client_key = json.getString("stripe_client_key")
            }

            if (json.has("website")) {
                this.isWebsiteVisible.set(true)
                this.website=json.getString("website")
            }else
                this.isWebsiteVisible.set(false)


        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }
    var name: String
        get() {
            var userName = ""
            if (firstName != "" || lastName != "") {
                userName = "$firstName $lastName"
            }
            return userName
        }
        set(key) {

        }
    var imageUrl: String
        get() {
            return Constants.downloadUrlll(image)
        }
        set(key) {

        }

    /*override fun toString(): String {
        return "SmoxUser(id=$id, email='$email', phone='$phone', firstName='$firstName', lastName='$lastName', image='$image', accountType=$accountType, loginType=$loginType, isFavorite=$isFavorite, stripe_url='$stripe_url', stripe_public_key='$stripe_public_key', stripe_secret_key='$stripe_secret_key', stripe_client_key='$stripe_client_key', contacts=$contacts, isSelected=$isSelected)"
    }*/
}
