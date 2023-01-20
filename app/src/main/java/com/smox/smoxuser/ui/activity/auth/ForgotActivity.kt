package com.smox.smoxuser.ui.activity.auth

import android.content.DialogInterface
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.android.volley.Request
import com.smox.smoxuser.databinding.ActivityForgotBinding
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.utils.shortToast
import org.json.JSONObject
import java.util.HashMap

class ForgotActivity : BaseActivity() {
    private lateinit var binding:ActivityForgotBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =ActivityForgotBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnSubmit.setOnClickListener {
            resetPassword()
        }

    }

    private fun resetPassword() {
        val email = binding.txtEmail.text.toString()

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.txtEmail.error = "Please type your email"
            return
        }

        val params = HashMap<String, String>()
        params["email"] = email

        progressHUD.show()
        APIHandler(
            this,
            Request.Method.POST,
            Constants.API.forgot,
            params,
            object:APIHandler.NetworkListener{
                override fun onFail(error: String?) {
                    shortToast(error)
                    progressHUD.dismiss()
                }

                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    binding.txtEmail.setText("")
                    showMessage(result.getString("message"))
                }
        })
    }

    private fun showMessage(message: String) {
        val builder = AlertDialog.Builder(this@ForgotActivity)
        builder.setMessage(message)
        builder.setCancelable(true)

        builder.setPositiveButton("OK",
            DialogInterface.OnClickListener { dialog, _ -> dialog.cancel() })
        val alert = builder.create()
        alert.show()
    }

}
