package com.smox.smoxuser.manager

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.android.volley.*
import com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
import com.android.volley.DefaultRetryPolicy.DEFAULT_MAX_RETRIES
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.quickblox.messages.services.QBPushManager
import com.quickblox.messages.services.SubscribeService
import com.quickblox.users.QBUsers
import com.smox.smoxuser.App
import com.smox.smoxuser.R
import com.smox.smoxuser.ui.activity.HomeActivity
import com.smox.smoxuser.utils.SharedPrefsHelper
import com.smox.smoxuser.utils.chat.ChatHelper
import com.smox.smoxuser.utils.qb.QbDialogHolder
import com.smox.smoxuser.utils.qb.callback.QBPushSubscribeListenerImpl
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.util.*


class APIHandler {

    private var listener: NetworkListener?
    private var context: Context
    private var session: SessionManager

    interface NetworkListener {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered
        fun onResult(result: JSONObject)

        fun onFail(error: String?)
    }

    // Constructor where listener events are ignored
    constructor(
        context: Context,
        requestType: Int,
        tag: String,
        params: HashMap<String, String>,
        listener: NetworkListener?
    ) {
        this.context = context
        this.listener = listener
        session = SessionManager.getInstance(context)
        sendRequest(requestType, tag, params)
    }
    constructor(
        context: Context,
        requestType: Int,
        tag: String,
        params: JSONObject,
        listener: NetworkListener?,
        type: String
    ) {
        this.context = context
        this.listener = listener
        session = SessionManager.getInstance(context)
        sendRequestJson(requestType, tag, params)
    }

    constructor(
        context: Context,
    ){
        this.context=context
        this.listener=null
        session = SessionManager.getInstance(context)
    }

    /*constructor(
        context: Context,
        tag: String,
        params: Map<String, String>,
        file: ArrayList<File>,
        fileName: ArrayList<String>,
        listener: NetworkListener
    ) {
        this.context = context
        this.listener = listener
        session = SessionManager.getInstance(context)
        uploadImage(tag, params, file = file, fileName = fileName)

    }*/

    private fun sendRequest(requestType: Int, tag: String, params: Map<String, String>?) {
        // Tag used to cancel the request
        var url = Constants.KUrl.server + tag
        Log.e(TAG, "sendRequest: url: $url  request: $requestType  params: $params" )
        if (requestType == Request.Method.GET && params != null || requestType == Request.Method.DELETE && params != null) {
            url = "$url?"
            for ((key, value) in params) {
                url = "$url$key=$value&"
            }
        }
        App.instance.requestQueue?.cache?.clear()
        val strReq = object : StringRequest(requestType,
            url, Response.Listener { response ->
                //Log.e(TAG, "Response: $response")

                try {
                    when (JSONTokener(response).nextValue()) {
                        is JSONObject -> {
                            val jObj = JSONObject(response)
                            listener?.onResult(jObj)
                        }
                        is JSONArray -> {
                            val jsonArray = JSONArray(response)
                            val jObj = JSONObject()
                            jObj.put("result", jsonArray)
                            listener?.onResult(jObj)
                        }
                        else -> listener?.onFail("Fail to call server!")
                    }

                } catch (e: JSONException) {
                    // JSON error
                    e.printStackTrace()
                  //  Log.e(TAG, e.message)
                    listener?.onFail(e.message)
                }
            }, Response.ErrorListener { error ->
                val errorMessage: String =
                    if (error.javaClass == TimeoutError::class.java || error.javaClass == NoConnectionError::class.java) {
                        "the request has either time out or there is no connection"
                    } else if (error.javaClass == AuthFailureError::class.java) {
                        context.getString(R.string.authError )
                    } else if (error.javaClass == ServerError::class.java) {
                        "the server responded with a error response"
                    } else if (error.javaClass == NetworkError::class.java) {
                        "there was network error while performing the request"
                    } else if (error.javaClass == ParseError::class.java) {
                        "the server response could not be parsed"
                    } else {
                        val em = try {
                            val responseBody = String(error.networkResponse.data)
                            val data = JSONObject(responseBody)
                            data.getString("message")
                        } catch (e: JSONException) {
                            ""
                        }
                        if (em.isEmpty()) {
                            error.localizedMessage
                                ?: "Error Code: " + error.networkResponse.statusCode
                        } else {
                            em
                        }
                    }

                if (error.javaClass == AuthFailureError::class.java) {
                    logout()
                }
                listener?.onFail(errorMessage)
            }) {

            override fun getParams(): Map<String, String>? {
                return params
            }

            override fun getHeaders(): Map<String, String> {
                val map = HashMap<String, String>()
                //map.put("X-Device-Info","Android FOO BAR");
                //map.put("Content-Type", "application/json; charset=UTF-8");
                if (session.apiKey != null) {
                    Log.e("Authentication", session.apiKey!!)
                    map["Authentication"] = session.apiKey!!
                }
                return map
            }
        }

        strReq.setShouldCache(false)
        strReq.retryPolicy = DefaultRetryPolicy(
            0,
            DEFAULT_MAX_RETRIES,
            DEFAULT_BACKOFF_MULT
        )
        // Adding request to request queue
        App.instance.addToRequestQueue(strReq, tag)
    }

    private fun sendRequestJson(requestType: Int, tag: String, params: JSONObject?) {
        // Tag used to cancel the request
        var url = Constants.KUrl.server + tag
        Log.e(TAG, "sendRequest: url: $url  request: $requestType   params: $params" )
        App.instance.requestQueue?.cache?.clear()
        val strReq = object : JsonObjectRequest(requestType,
            url, params, object : Response.Listener<JSONObject?> {
                override fun onResponse(response: JSONObject?) {
                    //Log.e(TAG, "Response: $response")
                }
            }, Response.ErrorListener { error ->
                val errorMessage: String =
                    if (error.javaClass == TimeoutError::class.java || error.javaClass == NoConnectionError::class.java) {
                        "the request has either time out or there is no connection"
                    } else if (error.javaClass == AuthFailureError::class.java) {
                        "there was an Authentication Failure while performing the request"
                    } else if (error.javaClass == ServerError::class.java) {
                        "the server responded with a error response"
                    } else if (error.javaClass == NetworkError::class.java) {
                        "there was network error while performing the request"
                    } else if (error.javaClass == ParseError::class.java) {
                        "the server response could not be parsed"
                    } else {
                        val em = try {
                            val responseBody = String(error.networkResponse.data)
                            val data = JSONObject(responseBody)
                            data.getString("message")
                        } catch (e: JSONException) {
                            ""
                        }
                        if (em.isEmpty()) {
                            error.localizedMessage
                                ?: "Error Code: " + error.networkResponse.statusCode
                        } else {
                            em
                        }
                    }

                if (error.javaClass == AuthFailureError::class.java) {
                    logout()
                }
                listener?.onFail(errorMessage)
            }) {

            override fun getHeaders(): Map<String, String> {
                val map = HashMap<String, String>()
                //map.put("X-Device-Info","Android FOO BAR");
                map["Content-Type"] = "application/json;"
                if (session.apiKey != null) {
                    Log.e("Authentication", session.apiKey!!)
                    map["Authentication"] = session.apiKey!!
                }
                return map
            }
        }

        strReq.setShouldCache(false)
        strReq.retryPolicy = DefaultRetryPolicy(
            0,
            DEFAULT_MAX_RETRIES,
            DEFAULT_BACKOFF_MULT
        )
        // Adding request to request queue
        App.instance.addToRequestQueue(strReq, tag)
    }



    companion object {
        private const val TAG = "NETWORK"
    }

    fun logout() {
        App.instance.currentPage = 0
        val sessionManager = SessionManager.getInstance(context)
        if (sessionManager.userType == 1) {
            logoutQB()
        }
        sessionManager.userData = ""
        sessionManager.apiKey = ""
        sessionManager.isSubscribed = false
        sessionManager.subscription_enddate = ""
        sessionManager.Sp_publishableKey = ""
        sessionManager.Sp_privateKey = ""
        val intent =
            Intent(context, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
        try {
            (context as Activity).finish()
        } catch (e: Exception) {

        }
    }

    private fun logoutQB() {
        ChatHelper.destroy()
        if (QBPushManager.getInstance().isSubscribedToPushes) {
            QBPushManager.getInstance().addListener(object : QBPushSubscribeListenerImpl() {
                override fun onSubscriptionDeleted(b: Boolean) {
                    QBUsers.signOut().performAsync(null)
                    QBPushManager.getInstance().removeListener(this)
                }
            })
            SubscribeService.unSubscribeFromPushes(context)
        } else {
            QBUsers.signOut().performAsync(null)
        }

        SharedPrefsHelper.removeQbUser()
        QbDialogHolder.clear()
    }
}
