package com.smox.smoxuser.manager

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.util.Log
import com.smox.smoxuser.manager.Constants.API.DARK_MODE_SET
import com.smox.smoxuser.manager.Constants.API.IS_BACK
import com.smox.smoxuser.manager.Constants.API.IS_DARK_MODE_ON
import com.smox.smoxuser.manager.Constants.API.IS_DOWNLOAD_POPUP_SHOW


class SessionManager(context: Context) {

    // Shared Preferences
    private var pref: SharedPreferences

    private var editor: Editor

    // Shared pref mode
    private val privateMode = 0


    // commit changes
    var apiKey: String?
        get() = pref.getString(APP_KEY, "")
        set(key) {
            editor.putString(APP_KEY, key)
            editor.commit()

            Log.d(TAG, "User login session modified!")
        }

    var loginKeep: Boolean
        get() = pref.getBoolean(LOGIN_KEEP, true)
        set(status) {
            editor.putBoolean(LOGIN_KEEP, status)
            editor.commit()
        }
    var userData: String?
        get() = pref.getString(USER_DATA, "")
        set(key) {
            editor.putString(USER_DATA, key)
            editor.commit()
        }

    var userDataOpenDays: String?
        get() = pref.getString(USER_DATA_OPEN_DAYS, "")
        set(key) {
            editor.putString(USER_DATA_OPEN_DAYS, key)
            editor.commit()
        }

    var deviceToken: String?
        get() = pref.getString(DEVICE_TOKEN, "")
        set(key) {
            editor.putString(DEVICE_TOKEN, key)
            editor.commit()
        }
    var userType: Int
        get() = pref.getInt(USER_TYPE, 0)
        set(key) {
            editor.putInt(USER_TYPE, key)
            editor.commit()
        }
    var isFirstTime: Boolean
        get() = pref.getBoolean(FIRST_TIME, true)
        set(status) {
            editor.putBoolean(FIRST_TIME, status)
            editor.commit()
        }

    var isSocialLogin: Boolean
        get() = pref.getBoolean(IS_SOCIAL_LOGIN, false)
        set(isSocialLogin) {
            editor.putBoolean(IS_SOCIAL_LOGIN, isSocialLogin)
            editor.commit()
        }

    var keep_me_email: String?
        get() = pref.getString(KEEP_ME_EMAIL, "")
        set(key) {
            editor.putString(KEEP_ME_EMAIL, key)
            editor.commit()
        }

    var keep_me_password: String?
        get() = pref.getString(KEEP_ME_PASSWORD, "")
        set(key) {
            editor.putString(KEEP_ME_PASSWORD, key)
            editor.commit()
        }


    var isSubscribed: Boolean
        get() = pref.getBoolean(IS_SUBSCRIBED, false)
        set(status) {
            editor.putBoolean(IS_SUBSCRIBED, status)
            editor.commit()
        }

    var subscription_enddate: String?
        get() = pref.getString(SUBSCRIPTION_ENDDATE, "")
        set(key) {
            editor.putString(SUBSCRIPTION_ENDDATE, key)
            editor.commit()
        }

    init {
        pref = context.applicationContext.getSharedPreferences(PREF_NAME, privateMode)
        editor = pref.edit()
        editor.apply()
    }

    var Sp_publishableKey: String?
        get() = pref.getString(PUBLISHABLE_KEY, "")
        set(key) {
            editor.putString(PUBLISHABLE_KEY, key)
            editor.commit()
        }

    var Sp_privateKey: String?
        get() = pref.getString(PRIVATE_KEY, "")
        set(key) {
            editor.putString(PRIVATE_KEY, key)
            editor.commit()
        }


    fun setValue(key:String,v:String){
        editor.putString(key, v)
        editor.commit()
    }

    fun getValue(key:String): String? {
        return pref.getString(key,"")
    }

    var userId : Int
        get() = pref.getInt(USER_ID,0)
        set(key){
            editor.putInt(USER_ID, key)
            editor.commit()
        }

    var customerStripeId : String?
    get() = pref.getString(STRIPE_ID,"")
    set(customer_stripe_key) {
        editor.putString(STRIPE_ID,customer_stripe_key)
        editor.commit()
    }

    var isDarkModeOn: Boolean
        get() = pref.getBoolean(IS_DARK_MODE_ON, false)
        set(status) {
            editor.putBoolean(IS_DARK_MODE_ON, status)
            editor.commit()
        }

    var isDarkModeSet: Boolean
        get() = pref.getBoolean(DARK_MODE_SET, false)
        set(status) {
            editor.putBoolean(DARK_MODE_SET, status)
            editor.commit()
            editor.apply()
        }

    var isDownloadShow: Boolean
        get() = pref.getBoolean(IS_DOWNLOAD_POPUP_SHOW, true)
        set(status) {
            editor.putBoolean(IS_DOWNLOAD_POPUP_SHOW, status)
            editor.commit()
            editor.apply()
        }

    var accountType : String?
        get() = pref.getString(ACCOUNT_TYPE,"")
        set(value){
            editor.putString(ACCOUNT_TYPE,value)
            editor.commit()
        }

    var stripeClientKey : String?
        get() = pref.getString(STRIPE_CLIENT_ID,"")
        set(value){
            editor.putString(STRIPE_CLIENT_ID,value)
            editor.commit()
        }

    var isBack: Boolean
        get() = pref.getBoolean(IS_BACK, false)
        set(status) {
            editor.putBoolean(IS_BACK, status)
            editor.commit()
            editor.apply()
        }


    companion object {
        // LogCat tag
        private val TAG = SessionManager::class.java.simpleName
        // Product Buy

        // Shared preferences file name
        const val PREF_NAME = "SMOX_TRIM_SETTERS"

        const val APP_KEY = "API_KEY"
        const val LOGIN_KEEP = "login_keep"
        const val USER_DATA = "user_data"
        const val USER_DATA_OPEN_DAYS = "user_data_open_days"
        const val DEVICE_TOKEN = "device_token"
        const val PUBLISHABLE_KEY = "publishableKey"
        const val PRIVATE_KEY = "privateKey"
        const val USER_TYPE = "user_type"
        const val FIRST_TIME = "first_time"
        const val KEEP_ME_EMAIL = "keepMeEmail"
        const val KEEP_ME_PASSWORD = "keepMePassword"
        const val IS_SUBSCRIBED = "isSubscribed"
        const val SUBSCRIPTION_ENDDATE = "subscriptionEndDate"
        const val USER_ID = "userID"
        const val ACCOUNT_TYPE="account_type"
        const val STRIPE_ID="stripe_id"
        const val STRIPE_CLIENT_ID="stripe_client_id"
        const val IS_SOCIAL_LOGIN = "isSocialLogin"

        @Volatile
        private var _instance: SessionManager? = null

        fun getInstance(context: Context): SessionManager =
            _instance ?: synchronized(this) {
                _instance ?: SessionManager(context).also {
                    _instance = it
                }
            }
    }

    override fun toString(): String {
        return "SessionManager(pref=$pref, editor=$editor, privateMode=$privateMode)"
    }


}
