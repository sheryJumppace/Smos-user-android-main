package com.smox.smoxuser.ui.activity.customer

import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.android.volley.Request
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ActivityContactUsBinding
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.utils.longToast
import com.smox.smoxuser.utils.shortToast
import org.json.JSONObject
import java.util.*

class ContactUsActivity : BaseActivity() {
    lateinit var binding: ActivityContactUsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_contact_us)

        binding.btnSubmit.setOnClickListener {
            if (isValid()) {
                sendEmailToServer()
            }
        }
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun sendEmailToServer() {

        val params = HashMap<String, String>()
        params["email"] = binding.etEmail.text.toString()
        params["description"] = binding.etDescription.text.toString()

        APIHandler(
            this,
            Request.Method.POST,
            Constants.API.contactUs,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    val status: Boolean = result.getBoolean("error")
                    val message: String = result.getString("message")
                    shortToast(message)
                    if (!status)
                        finish()
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    longToast(error!!)
                }
            })
    }

    private fun isValid(): Boolean {
        if (TextUtils.isEmpty(binding.etEmail.text.toString())) {
            binding.etEmail.error = "Please type your email id"
            return false
        }

        if (TextUtils.isEmpty(binding.etDescription.text.toString())) {
            binding.etDescription.error = "Please type your message"
            return false
        }


        if (!Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString()).matches()) {
            binding.etEmail.error = "Please type valid email id"
            return false
        }
        return true
    }
}
