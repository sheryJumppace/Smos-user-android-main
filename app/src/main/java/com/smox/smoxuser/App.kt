package com.smox.smoxuser

import android.app.Application
import android.text.TextUtils
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.FirebaseApp
import com.quickblox.auth.session.QBSettings
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Barber
import com.smox.smoxuser.model.type.ApplicationStatus
import com.smox.smoxuser.utils.ActivityLifecycle
import com.smox.smoxuser.utils.Prefrences

//Chat settings
const val CHAT_PORT = 5223
const val SOCKET_TIMEOUT = 300
const val KEEP_ALIVE: Boolean = true
const val USE_TLS: Boolean = true
const val AUTO_JOIN: Boolean = false
const val AUTO_MARK_DELIVERED: Boolean = true
const val RECONNECTION_ALLOWED: Boolean = true
const val ALLOW_LISTEN_NETWORK: Boolean = true


//Chat credentials range
private const val MAX_PORT_VALUE = 65535
private const val MIN_PORT_VALUE = 1000
private const val MIN_SOCKET_TIMEOUT = 300
private const val MAX_SOCKET_TIMEOUT = 60000

class App : Application() {
    val TAG = App::class.java.simpleName

    var currentUser = Barber()
    var currentPage = 0
    var myLocation: LatLng? = null
    var currentActivity: Class<*>? = null
    var unreadEvents = 0

    //protected lateinit var sessionManager: SessionManager
    var applicationStatus = ApplicationStatus.Background
    var checkSubscription: Boolean = true

    // commit changes
    var requestQueue: RequestQueue? = null
        get() {
            if (field == null) {
                field = Volley.newRequestQueue(applicationContext)
            }
            return field
        }

    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        registerActivityLifecycleCallbacks(ActivityLifecycle)
        initFabric()
        checkAppCredentials()
        checkChatSettings()
        initCredentials()
        //sessionManager = SessionManager.getInstance(applicationContext)
        FirebaseApp.initializeApp(this)
        Prefrences.with(this)

    }


    fun <T> addToRequestQueue(req: Request<T>, tag: String) {
        req.tag = if (TextUtils.isEmpty(tag)) TAG else tag
        requestQueue?.add(req)
    }

    fun <T> addToRequestQueue(req: Request<T>) {
        req.setTag(TAG)
        requestQueue?.add(req)
    }

    fun cancelPendingRequests(tag: Any) {
        requestQueue?.cancelAll(tag)
    }


    private fun checkAppCredentials() {
        if (Constants.kQBApplicationID.isEmpty() || Constants.kQBAuthKey.isEmpty() || Constants.kQBAuthSecret.isEmpty() || Constants.kQBAccountKey.isEmpty()) {
            throw AssertionError(getString(R.string.error_qb_credentials_empty))
        }
    }


    private fun checkChatSettings() {
        if (Constants.KQBUUserPassword.isEmpty() || CHAT_PORT !in MIN_PORT_VALUE..MAX_PORT_VALUE
            || SOCKET_TIMEOUT !in MIN_SOCKET_TIMEOUT..MAX_SOCKET_TIMEOUT
        ) {
            throw AssertionError(getString(R.string.error_chat_credentails_empty))
        }
    }

    private fun initCredentials() {
        QBSettings.getInstance().init(
            applicationContext,
            Constants.kQBApplicationID,
            Constants.kQBAuthKey,
            Constants.kQBAuthSecret
        )
        QBSettings.getInstance().accountKey = Constants.kQBAccountKey

        // Uncomment and put your Api and Chat servers endpoints if you want to point the sample
        // against your own server.
        //
        // QBSettings.getInstance().setEndpoints("https://your_api_endpoint.com", "your_chat_endpoint", ServiceZone.PRODUCTION);
        // QBSettings.getInstance().zone = ServiceZone.PRODUCTION
    }

    private fun initFabric() {
        if (!BuildConfig.DEBUG) {}
    }

}