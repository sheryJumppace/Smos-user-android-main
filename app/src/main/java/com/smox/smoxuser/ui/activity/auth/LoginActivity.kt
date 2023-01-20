package com.smox.smoxuser.ui.activity.auth

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
//import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ActivityLoginBinding
import com.smox.smoxuser.manager.SessionManager
import com.smox.smoxuser.model.type.UserType
import com.smox.smoxuser.ui.dialog.ShowStylerDownloadDialog
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : BaseLoginActivity(), View.OnClickListener {
    lateinit var binding: ActivityLoginBinding
    var userType:String =UserType.Customer.toString()
    var isPwdVisible=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //binding.txtEmail.setText("ibcmobile06@gmail.com")
        //binding.txtPassword.setText("123456")

        //binding.txtEmail.setText("rameshtesting@gmail.com")
        //binding.txtPassword.setText("123456")

        binding.btnFacebook.setOnClickListener(this)
        binding.btnGoogle.setOnClickListener(this)
        binding.btnForgot.setOnClickListener(this)
        binding.btnLogin.setOnClickListener(this)
        binding.registerBtn.setOnClickListener(this)
        binding.imageView4.setOnClickListener(this)

        //rajeshtesting@gmail.com // 123456 test user
        //santosh123@gmail.com // 123456 test user
        //testserverv4@gmail.com   //  123456  test barber
        //santosh.sharma@swspvtltd.com // user live db 123456
        //newtest@gmail.com// 123456 live barber id


        if (SessionManager.getInstance(applicationContext).isDownloadShow){
            ShowStylerDownloadDialog(this)
        }


        binding.remember.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                when {
                    binding.txtEmail.text.toString().isEmpty() -> {
                        binding.txtEmail.error = "Please type your email"
                        binding.txtEmail.requestFocus()
                    }
                    binding.txtPassword.text.toString().isEmpty() -> {
                        binding.txtPassword.error = "Please type your password"
                        binding.txtPassword.requestFocus()
                    }
                    binding.txtPassword.text.toString().length < 6 -> {
                        binding.txtPassword.error = "The password is too short, it must at least 6 characters"
                        binding.txtPassword.requestFocus()
                    }
                    else -> {
                        SessionManager.getInstance(this).setValue("email",binding.txtEmail.text.toString())
                        SessionManager.getInstance(this).setValue("pass",binding.txtPassword.text.toString())
                    }
                }
                // show toast , check box is checked

            } else {
                // show toast , check box is not checked
                SessionManager.getInstance(this).setValue("email","")
                SessionManager.getInstance(this).setValue("pass","")
            }
        }

        if (SessionManager.getInstance(this).getValue("email")?.isNotEmpty()!!){
            binding.txtEmail.setText(SessionManager.getInstance(this).getValue("email"))
            binding.txtPassword.setText(SessionManager.getInstance(this).getValue("pass"))
        }



    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnLogin -> {
                login()
            }
            R.id.registerBtn -> {

                signUp()
            }
            R.id.imageView4 -> {
               if (isPwdVisible) {
                   binding.txtPassword.transformationMethod=PasswordTransformationMethod.getInstance()
                   binding.imageView4.setBackgroundResource(R.drawable.visible_eye)
                   isPwdVisible=false
               }
                else {
                   binding.txtPassword.transformationMethod=HideReturnsTransformationMethod.getInstance()
                   binding.imageView4.setBackgroundResource(R.drawable.hide_pwd)
                   isPwdVisible=true
               }
            }
            R.id.btnForgot -> {
                forgot()
            }
            R.id.btnFacebook -> {
                //loginWithFacebook(UserType.None, false)
            }
            R.id.btnGoogle -> {
                btnGoogle.isEnabled = false
                //loginWithGoogle(UserType.None, false)
            }
        }
    }

    private fun forgot() {
        val intent = Intent(this@LoginActivity, ForgotActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
    }

    private fun signUp() {
        val intent = Intent(this@LoginActivity, SignupActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
        finish()
    }

    private fun login() {
        val email = binding.txtEmail.text.toString()
        val password = binding.txtPassword.text.toString()
        if (binding.remember.isChecked){
            SessionManager.getInstance(this).setValue("email",binding.txtEmail.text.toString())
            SessionManager.getInstance(this).setValue("pass",binding.txtPassword.text.toString())
        }

        if (validation()) {
            loginWithEmail(email, password,userType)
        }
    }

    private fun validation()
            : Boolean {
        return when {
            binding.txtEmail.text.toString().isEmpty() -> {
                txtEmail.error = "Please type your email"
                false
            }
            binding.txtPassword.text.toString().isEmpty() -> {
                txtPassword.error = "Please type your password"
                false
            }
            binding.txtPassword.text.toString().length < 6 -> {
                txtPassword.error = "The password is too short, it must at least 6 characters"
                false
            }
            else -> true
        }
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
    }
}