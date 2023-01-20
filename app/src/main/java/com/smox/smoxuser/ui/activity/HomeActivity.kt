package com.smox.smoxuser.ui.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
//import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.smox.smoxuser.PushNotificationUtils
import com.smox.smoxuser.R
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.manager.SessionManager
import com.smox.smoxuser.model.Barber
import com.smox.smoxuser.model.type.UserType
import com.smox.smoxuser.ui.activity.auth.BaseLoginActivity
import com.smox.smoxuser.ui.activity.auth.LoginActivity
import com.smox.smoxuser.ui.activity.auth.SignupActivity
import com.smox.smoxuser.ui.activity.home.Home2Activity
import com.smox.smoxuser.utils.qb.isInternetConnected
import com.smox.smoxuser.utils.shortToast
import kotlinx.android.synthetic.main.activity_home.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class HomeActivity : BaseLoginActivity() {

    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        isHomeActivity = true
        PushNotificationUtils(this).createNotificationChannel()
        var appointId = 0
        if (intent.hasExtra(Constants.API.APPOINT_ID)) {
            appointId = intent.getIntExtra(Constants.API.APPOINT_ID, 0)
        }

        if (sessionManager.loginKeep && sessionManager.apiKey != null && sessionManager.apiKey?.length!! > 0) {
            val user = Barber(sessionManager.userData)
            app.currentUser = user
            user.email

            handleDeepLink()

            if (user.id < 1) {
                return
            }

            when {
                isInternetConnected(this) -> showMainPage(false, appointId)
                else -> shortToast(R.string.error_connection)
            }
        }

        btnGoogle.setOnClickListener {
            btnGoogle.isEnabled = false
            loginWithGoogle(UserType.None, false)
        }
        btnFacebook.setOnClickListener {
            btnFacebook.isEnabled = false
            loginWithFacebook(UserType.None, false)
        }
        btnLogin.setOnClickListener {
            btnLogin.isEnabled = false
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
        }
        btnSignUp.setOnClickListener {

            btnSignUp.isEnabled = false
            val intent = Intent(this@HomeActivity, SignupActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
        }
        btnRemind.setOnClickListener {
            val intent = Intent(this@HomeActivity, Home2Activity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
        }

    }

    fun handleDeepLink() {
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(
                this
            ) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    val intent = intent
                    val data: Uri? = intent.data
                    if (data.toString().toLowerCase().contains("orderId")) {
                        Log.e("TAG", "found data ")
                    } else if (data.toString().toLowerCase().contains("offer")) {
                        if (data!!.getQueryParameter("offer") != null) {
                            //TO DO
                        }
                    } else {
                        //TO DO
                        finish()
                    }
                }
            }
            .addOnFailureListener(
                this
            ) { e -> Log.w("TAG", "getDynamicLink:onFailure", e) }
    }

    override fun onResume() {
        super.onResume()
        btnLogin.isEnabled = true
        btnSignUp.isEnabled = true
        btnFacebook.isEnabled = true
        btnGoogle.isEnabled = true
        getHashKey()
        //getFirebaseToken()
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        shortToast(resources.getString(R.string.back_again_exit))

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }

    private fun getHashKey() {
        try {
            val info = packageManager.getPackageInfo(
                applicationContext.packageName,
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {

        } catch (e: NoSuchAlgorithmException) {

        }
    }

    private fun getFirebaseToken() {

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            var token = ""
            if (it.isComplete)
                token = it.result.toString()

            SessionManager.getInstance(applicationContext).deviceToken = token
        }

        //SessionManager.getInstance(applicationContext).deviceToken = FirebaseInstanceId.getInstance().token
    }
}