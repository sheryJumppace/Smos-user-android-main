package com.smox.smoxuser.ui.activity.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
//import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ActivitySignUpV2Binding
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.manager.SessionManager
import com.smox.smoxuser.model.type.UserType
import com.smox.smoxuser.ui.activity.WebViewActivity
import com.smox.smoxuser.ui.activity.home.Home2Activity
import com.smox.smoxuser.utils.shortToast
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_signup.*
import java.util.*
private const val RC_SIGN_IN = 123
private var type = UserType.Customer

class SignupActivity : BaseLoginActivity(), View.OnClickListener {
    private var isPhoneVerified = false
    private lateinit var binding: ActivitySignUpV2Binding
    var userType:String =UserType.Customer.toString()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpV2Binding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnFacebook.setOnClickListener(this)
        binding.btnGoogle.setOnClickListener(this)
        binding.btnSignUp.setOnClickListener(this)
        binding.txtPhone.setOnClickListener(this)
        binding.lnrLogin.setOnClickListener(this)

        //binding.txtPhone.isEnabled = false
        binding.txtPhone.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                isPhoneVerified = false
            }
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.lnrLogin -> {
               // lnrLogin.isEnabled = false
                login()
            }
            R.id.btnSignUp -> {
                signUp()
            }
            R.id.btnFacebook -> {
               // btnFacebook.isEnabled = false
                //loginWithFacebook(type, true)

                val subscribeIntent = Intent(this@SignupActivity, Home2Activity::class.java)
                subscribeIntent.putExtra("FromSignUp", true)
                startActivity(subscribeIntent)
            }
            R.id.btnGoogle -> {
               // btnGoogle.isEnabled = false
                loginWithGoogle(type, true)
            }
            /* R.id.btnTerms -> {
                btnTerms.isEnabled = false
                terms()
            }*/
           // R.id.btnVerify -> doPhoneNumberVerification()
            R.id.txtPhone -> doPhoneNumberVerification()
            /*R.id.btnSubscription -> {
                val subscribeIntent = Intent(this@SignupActivity, SubscriptionActivity::class.java)
                subscribeIntent.putExtra("FromSignUp", true)
                startActivity(subscribeIntent)
            }*/
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == RESULT_OK) {
                val phone = response!!.user.phoneNumber.toString()
                binding.txtPhone.setText(phone)
                isPhoneVerified = true
                binding.btnVerify.visibility = View.VISIBLE
            } else {
                if (response != null) {
                    val error = response.error
                    if (error != null) {
                        shortToast(response.error!!.localizedMessage)
                    }
                }
            }
        }
    }

    private fun login() {
        val intent = Intent(this@SignupActivity, LoginActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
        finish()
    }

    private fun terms() {
        val intent = Intent(this@SignupActivity, WebViewActivity::class.java)
        intent.putExtra("url", Constants.KUrl.terms)
        intent.putExtra("title", resources.getString(R.string.title_terms))
        startActivity(intent)
        overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
    }

    private fun signUp() {
        if (!validation()) {
            return
        }
        val firstName = binding.txtFirstName.text.toString().trim()
        val lastName = binding.txtLastName.text.toString().trim()
        val email = binding.txtEmail.text.toString().trim()
        val phone = binding.txtPhone.text.toString().trim()
        //val phone = "9876543214"
        val password = binding.txtPassword.text.toString()
        signUpWithEmail(firstName, lastName, email, phone, password, type)
    }

    private fun validation()
            : Boolean {
        //var validate = true
        if (binding.txtFirstName.text.toString().isEmpty()) {
            binding.txtFirstName.error = "Please type your first name"
            return false
        }
        if (binding.txtLastName.text.toString().isEmpty()) {
            binding.txtLastName.error = "Please type your last name"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(binding.txtEmail.text.toString()).matches()) {
            binding.txtEmail.error = "Please type your email"
            return false
        }
        if (binding.txtPassword.text.toString().length < 6) {
            binding.txtPassword.error = "The password is too short, it must at least 6 characters"
            return false
        }
        if (!isPhoneVerified) {
            binding.txtPhone.error = "Please verify your phone number"
            return false
        }

        return true
    }

    private fun doPhoneNumberVerification() {
        val providers = listOf<AuthUI.IdpConfig>(
            AuthUI.IdpConfig.PhoneBuilder().build()
        )
        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setTheme(R.style.FirebasePhoneAuth)
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN
        )
    }

    private fun getFirebaseToken() {

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            var token=""
            if (it.isComplete)
                token = it.result.toString()

            SessionManager.getInstance(applicationContext).deviceToken = token
        }
        //SessionManager.getInstance(applicationContext).deviceToken = FirebaseInstanceId.getInstance().token
    }

    override fun onResume() {
        super.onResume()
        getFirebaseToken()
       /*// lnrLogin.isEnabled = true
        btnFacebook.isEnabled = true
        btnGoogle.isEnabled = true
        btnTerms.isEnabled = true*/
    }
}
