package com.smox.smoxuser.ui.activity.auth

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Request
import com.facebook.*
import com.smox.smoxuser.R
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.manager.SessionManager
import com.smox.smoxuser.model.Barber
import com.smox.smoxuser.model.type.ApplicationStatus
import com.smox.smoxuser.model.type.UserType
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.activity.HomeActivity
import com.smox.smoxuser.ui.activity.barber.BarberMainActivity
import com.smox.smoxuser.ui.activity.customer.AppointmentDetailsNewActivity
import com.smox.smoxuser.ui.activity.home.Home2Activity
import com.smox.smoxuser.utils.Prefrences
import com.smox.smoxuser.utils.SharedPrefsHelper
import com.smox.smoxuser.utils.chat.ChatHelper
import com.smox.smoxuser.utils.qb.isInternetConnected
import com.smox.smoxuser.utils.shortToast
import kotlinx.android.synthetic.main.activity_home2.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*


open class BaseLoginActivity() : BaseActivity(), GoogleApiClient.OnConnectionFailedListener {
    private val UNAUTHORIZED = 401
    private lateinit var userType: UserType
    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var callbackManager: CallbackManager

    private val RC_SIGN_IN = 7
    private val FACEBOOK_REQUEST = 64206

    var isHomeActivity: Boolean = false

    var keep_email: String = ""
    var keep_password: String = ""
    private lateinit var context: Context
    private var isFromSignup: Boolean = false

    //private lateinit var billingViewModel: BillingViewModel
    //private lateinit var fetchSubscriptionReceiver: BroadcastReceiver
    private var userID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        context = applicationContext

        //fetchSubscriptionReceiver = FetchSubscriptionReceiver()

        ///////////Google Login Initilization/////////////
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this@BaseLoginActivity, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()

        initializeFacebook()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)
            handleSignInResult(result!!)
        } else if (requestCode == FACEBOOK_REQUEST && resultCode == Activity.RESULT_OK) {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
        Log.e("In OnActivityResult:", "$requestCode,$resultCode")
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient.connect()
    }


    public override fun onDestroy() {
        super.onDestroy()
        mGoogleApiClient.disconnect()
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient.isConnected) {
            mGoogleApiClient.disconnect()
        }
    }

    override fun onResume() {
        super.onResume()
        if (mGoogleApiClient.isConnected) {
            mGoogleApiClient.connect()
        }

        /* LocalBroadcastManager.getInstance(this).registerReceiver(fetchSubscriptionReceiver,
             IntentFilter(ACTION_FETCH_SUBSCRIPTION)
         )*/
    }

    override fun onPause() {
        super.onPause()
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(fetchSubscriptionReceiver)
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        shortToast(p0.errorMessage)
    }

    fun loginWithFacebook(type: UserType, fromSignup: Boolean) {
        userType = type
        isFromSignup = fromSignup
//        val accessToken = AccessToken.getCurrentAccessToken()
//        val isLoggedIn = accessToken != null && !accessToken.isExpired
        LoginManager.getInstance()
            .logInWithReadPermissions(this, listOf("public_profile", "email", "user_friends"))

    }

    fun loginWithGoogle(type: UserType, fromSignup: Boolean) {
        userType = type
        isFromSignup = fromSignup
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }


    /////////Google plus result////////////////
    private fun handleSignInResult(result: GoogleSignInResult) {
        Log.e("", "handleSignInResult:" + result.isSuccess)
        if (result.isSuccess) {
            // Signed in successfully, show authenticated UI.
            val acct = result.signInAccount
            val socialID = acct!!.id
            var email = ""
            var firstName = ""
            var lastName = ""
            if (acct.givenName != null && acct.givenName != "") {
                firstName = acct.givenName.toString()
            }
            if (acct.familyName != null && acct.familyName != "") {
                lastName = acct.familyName.toString()
            }
            var imageUrl = ""
            if (acct.photoUrl != null && acct.photoUrl.toString() != "") {
                imageUrl = acct.photoUrl.toString()
            }
            if (acct.email != null && acct.email != "") {
                email = acct.email.toString()
            }

            loginWithSocialAccount(
                "google",
                email,
                firstName,
                lastName,
                imageUrl,
                socialID
            )
        } else {
        }
    }

    //==========Facebook initialize
    private fun initializeFacebook() {

        FacebookSdk.fullyInitialize()
        AppEventsLogger.activateApp(application)
        //AppEventsLogger.activateApp(context);
        callbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {

                    val accessToken = AccessToken.getCurrentAccessToken()
                    if (accessToken != null) {
                        val request = GraphRequest.newMeRequest(
                            accessToken
                        ) { _, response ->
                            if (response != null) {
                                try {
                                    val data = response.jsonObject
                                    var socialID = ""
                                    if (data.has("id")) {
                                        socialID = data.getString("id")
                                    }
                                    var email = ""
                                    if (data.has("email")) {
                                        email = data.getString("email");
                                    }
                                    var firstName = ""
                                    if (data.has("first_name")) {
                                        firstName = data.getString("first_name")
                                    }
                                    var lastName = ""
                                    if (data.has("last_name")) {
                                        lastName = data.getString("last_name")
                                    }
                                    val image =
                                        "http://graph.facebook.com/$socialID/picture?type=large"
                                    loginWithSocialAccount(
                                        "facebook",
                                        email,
                                        firstName,
                                        lastName,
                                        image,
                                        socialID
                                    )
                                } catch (e: Exception) {
                                    //e.printStackTrace()
                                        shortToast("Something went wrong.")
                                }

                            }
                        }
                        val parameters = Bundle()
                        parameters.putString(
                            "fields",
                            "id, name, first_name, last_name, picture.type(large), email, link"
                        )
                        request.parameters = parameters
                        request.executeAsync()
                    }
                }

                override fun onCancel() {
                    // App code
                }

                override fun onError(exception: FacebookException) {
                    // App code
                    Log.i("-FBException-", exception.message!!);
                }
            })
    }

    private fun loginWithSocialAccount(
        type: String,
        email: String?,
        firstName: String?,
        lastName: String?,
        image: String?,
        socialID: String?
    ) {
        val params = HashMap<String, String>()
        params["type"] = type
        params["email"] = email ?: ""
        params["first_name"] = firstName ?: ""
        params["last_name"] = lastName ?: ""
        params["image_url"] = image ?: ""
        params["link"] = ""
        params["social_id"] = socialID ?: ""
        params["user_type"] = UserType.Customer.toString()

        progressHUD.show()

        APIHandler(
            applicationContext,
            Request.Method.POST,
            Constants.API.loginWithSocial,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    didCompleteLogin(result, isFromSignup, true)
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                }
            })
    }

    fun loginWithEmail(email: String, password: String, userType: String) {
        val params = HashMap<String, String>()
        params["email"] = email
        params["password"] = password
        params["type"] = userType
        params["timezone"] = TimeZone.getDefault().id

        progressHUD.show()

        APIHandler(
            applicationContext,
            Request.Method.POST,
            Constants.API.login,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    didCompleteLogin(result, false, false)
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    fun updateAccountType(type: UserType) {
        userType = type;
        val params = HashMap<String, String>()
        params["type"] = type.name.toLowerCase()
        progressHUD.show()

        APIHandler(
            applicationContext,
            Request.Method.PUT,
            Constants.API.user_type,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    app.currentUser.accountType = type
                    sessionManager.userData = app.currentUser.getJsonString()
                    sessionManager.userData = app.currentUser.getJsonString()
                    progressHUD.dismiss()
                    didCompleteLogin(result, userType.equals(UserType.Barber), false)
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    fun signUpWithEmail(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String,
        type: UserType
    ) {
        val params = HashMap<String, String>()
        params["email"] = email
        params["password"] = password
        params["phone_number"] = phone
        params["first_name"] = firstName
        params["last_name"] = lastName
        params["type"] = type.name.toLowerCase()
        progressHUD.show()
        //Log.e("TAG", "signUpWithEmail: $params")
        APIHandler(
            applicationContext,
            Request.Method.POST,
            Constants.API.signUp,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    didCompleteLogin(result, true, false)
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    fun didCompleteLogin(result: JSONObject, isSignup: Boolean, isSocialLogin: Boolean) {
        val error: Boolean
        try {

            val gso: GoogleSignInOptions =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)
            googleSignInClient.signOut()

            LoginManager.getInstance().logOut()

            Log.e("User type result :- ", result.toString())

            error = result.getBoolean("error")
            // Check for error node in json
            if (!error) {
                if (result.has("result")) {

                    val json = result.getJSONObject("result")
                    val apiKey = json.getString("api_key")
                    sessionManager.apiKey = apiKey
                    val accType = json.getString("account_type").toString()
                    if (accType == "barber") {
                        shortToast("You are a styler, please login in styler app.")
                        logoutCall()
                    } else {
                        if (json.has("api_key")) {

                            val user = Barber(json)
                            app.currentUser = user

                            Log.e("User Data :- ", user.toString())
                            sessionManager.userData = app.currentUser.getJsonString()
                            //sessionManager.Sp_publishableKey = user.stripe_public_key
                            //sessionManager.Sp_privateKey = user.stripe_secret_key
                            sessionManager.Sp_publishableKey = Constants.KStripe.publishableKey
                            sessionManager.Sp_privateKey = Constants.KStripe.secretKey
                            sessionManager.accountType = json.getString("account_type").toString()
                            sessionManager.isSocialLogin = isSocialLogin
                            Prefrences.saveBoolean(Constants.API.ORDER_CANCELLED, false)

                            if (json.has("open_hours") && !json.isNull("open_hours")) {
                                sessionManager.userDataOpenDays =
                                    json.getJSONObject("open_hours").toString()
                                Log.e(
                                    "Open days Data:- ",
                                    json.getJSONObject("open_hours").toString()
                                )
                            }
                            userID = app.currentUser.id.toString()
                            sessionManager.userId = app.currentUser.id

                            sessionManager.customerStripeId = user.stripe_customer_id
                            sessionManager.stripeClientKey = user.stripe_client_key
                        }

                        if (json.has("is_subscribed")) {
                            sessionManager.isSubscribed = json.getBoolean("is_subscribed")
                        }

                        if (json.has("subscription_enddate")) {
                            sessionManager.subscription_enddate =
                                json.getString("subscription_enddate")
                        }

                        if (sessionManager.loginKeep && (!TextUtils.isEmpty(keep_email) && !TextUtils.isEmpty(
                                keep_password
                            ))
                        ) {
                            sessionManager.keep_me_email = keep_email
                            sessionManager.keep_me_password = keep_password
                        } else {
                            sessionManager.keep_me_email = ""
                            sessionManager.keep_me_password = ""
                        }

                        app.applicationStatus = ApplicationStatus.Login

                        if (isInternetConnected(this)) {
                            showMainPage(isSignup, 0)
                        } else {
                            shortToast(R.string.error_connection)
                        }
                    }
                }

            } else {
                // Error in login. Get the error message
                val errorMsg = result.getString("message")
                shortToast(errorMsg)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            shortToast(e.localizedMessage)
        }
    }

    companion object{
        @SuppressLint("StaticFieldLeak")
        private var _instance: BaseLoginActivity? = null

        fun getInstance(): BaseLoginActivity =
            _instance ?: synchronized(this) {
                _instance ?: BaseLoginActivity().also {
                    _instance = it
                }
            }
    }
    fun logoutCall() {
        val params = HashMap<String, String>()

        APIHandler(
            this,
            Request.Method.GET,
            Constants.API.logout,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    logout()
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)

                }

            }
        )
    }

    fun logout() {

            Log.e("TAG", "logout: inside", )
            val sessionManager = SessionManager.getInstance(applicationContext)
            sessionManager.userData = ""
            sessionManager.apiKey = ""
            sessionManager.userId = -1
            sessionManager.isSubscribed = false
            sessionManager.subscription_enddate = ""
            sessionManager.Sp_publishableKey = ""
            sessionManager.Sp_privateKey = ""
            val intent =
                Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()

    }



    fun showMainPage(isSignup: Boolean, appointId: Int) {
        sessionManager.isFirstTime = false
        Log.e("User type:- ", app.currentUser.getJsonString());
        val user = app.currentUser
        Log.e("User Data showManPage:-", user.toString())
        updateDeviceToken()
        val intent1: Intent
        if (appointId == 0) {
            intent1 = Intent(this@BaseLoginActivity, Home2Activity::class.java)
        } else {
            intent1 = Intent(this@BaseLoginActivity, AppointmentDetailsNewActivity::class.java)
            intent1.putExtra(Constants.API.APPOINT_ID, appointId)
        }
        startActivity(intent1)
        finish()

    }

    private fun updateDeviceToken() {
        val token = sessionManager.deviceToken ?: return
        Log.i("Device Token", token)
        val params = HashMap<String, String>()
        params["device_token"] = token
        params["type"] = "2"
        APIHandler(
            applicationContext,
            Request.Method.PUT,
            Constants.API.user_device,
            params,
            null
        )
    }


    //Login/SignUp QBChat

    private fun signIn(user: QBUser, listener: QBListener) {
        ChatHelper.login(user, object : QBEntityCallback<QBUser> {
            override fun onSuccess(userFromRest: QBUser, bundle: Bundle?) {
                if (userFromRest.fullName == user.fullName) {
                    loginToChat(user, listener)
                } else {
                    //Need to set password NULL, because server will update user only with NULL password
                    user.password = null
                    if (isInternetConnected(this@BaseLoginActivity)) {
                        updateUser(user, listener)
                    } else {
                        shortToast(R.string.error_connection)
                    }

                }
            }

            override fun onError(e: QBResponseException) {
                if (e.httpStatusCode == UNAUTHORIZED) {
                    if (isInternetConnected(this@BaseLoginActivity)) {
                        signUp(user, listener)
                    } else {
                        shortToast(R.string.error_connection)
                    }
                } else {
                    listener.onResult(getString(R.string.login_chat_login_error))
                }
            }
        })
    }

    private fun updateUser(user: QBUser, listener: QBListener) {
        ChatHelper.updateUser(user, object : QBEntityCallback<QBUser> {
            override fun onSuccess(qbUser: QBUser, bundle: Bundle?) {
                if (isInternetConnected(this@BaseLoginActivity)) {
                    loginToChat(user, listener)
                } else {
                    shortToast(R.string.error_connection)
                }
            }

            override fun onError(e: QBResponseException) {
                listener.onResult(getString(R.string.login_chat_login_error))
            }
        })
    }

    private fun loginToChat(user: QBUser, listener: QBListener) {
        //Need to set password, because the server will not register to chat without password
        user.password = Constants.KQBUUserPassword
        ChatHelper.loginToChat(user, object : QBEntityCallback<Void> {
            override fun onSuccess(void: Void?, bundle: Bundle?) {
                SharedPrefsHelper.saveQbUser(user)
                listener.onResult("")
            }

            override fun onError(e: QBResponseException) {
                listener.onResult(getString(R.string.login_chat_login_error))
            }
        })
    }

    private fun signUp(user: QBUser, listener: QBListener) {
        SharedPrefsHelper.removeQbUser()
        QBUsers.signUp(user).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(p0: QBUser?, p1: Bundle?) {
                if (isInternetConnected(this@BaseLoginActivity)) {
                    signIn(user, listener)
                } else {
                    shortToast(R.string.error_connection)
                }
            }

            override fun onError(exception: QBResponseException?) {
                listener.onResult(getString(R.string.login_chat_login_error))
            }
        })
    }

    interface QBListener {
        fun onResult(error: String)
    }

    fun keepMeLogin(keep_email: String, keep_password: String) {
        this.keep_email = keep_email
        this.keep_password = keep_password
    }

    /*private fun initSubscription() {
        billingViewModel = ViewModelProviders.of(this@BaseLoginActivity).get(BillingViewModel::class.java)
        billingViewModel.getActContext(this@BaseLoginActivity)

    }

    private inner class FetchSubscriptionReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent

            val isSusbcribeAvail = intent.getBooleanExtra("isSusbcribeAvail", false)
            if(isSusbcribeAvail){
                val orderId = intent.getStringExtra("orderId")
                if(userID.isNotEmpty()){
                    cancelSubscription("false", userID);
                } else {
                    progressHUD.dismiss()
                    nextToBarberMainScreen()
                }
            } else {
                progressHUD.dismiss()
                if(userID.isNotEmpty()){
                    cancelSubscription("true",userID);
                } else {
                    nextToBarberMainScreen()
                }
            }
            //progressHUD.dismiss()
        }
    }

    fun cancelSubscription(isCancel: String, userID: String) {
        val params = HashMap<String, String>()
        params["userId"] = userID
        //params["transactionId"] = transactionId
        params["isCancel"] = isCancel
        //params["endSubscriptionDate"] = ""
        //progressHUD.show()

        APIHandler(
            applicationContext,
            Request.Method.POST,
            Constants.API.cancelSubscription,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    val error = result.getBoolean("error");
                    if(!error){
                        val subsCancelResult = result.getBoolean("result");
                        sessionManager.isSubscribed = !subsCancelResult
                    }
                    nextToBarberMainScreen()
                }
                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    Toast.makeText(
                        applicationContext,
                        error, Toast.LENGTH_LONG
                    ).show()
                }
            })
    }*/

    private fun nextToBarberMainScreen() {
        val intent = Intent(this@BaseLoginActivity, BarberMainActivity::class.java)
        startActivity(intent)
    }
}
