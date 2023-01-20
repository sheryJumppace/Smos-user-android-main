package com.smox.smoxuser.ui.activity.customer

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.android.volley.Request
import com.kaopiz.kprogresshud.KProgressHUD
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ActivityUpdatePasswordBinding
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.utils.shortToast
import org.json.JSONObject
import java.util.HashMap

class UpdatePasswordActivity : BaseActivity() {
    lateinit var binding:ActivityUpdatePasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_update_password)


        binding.btnSubmit.setOnClickListener{
            if (isValid()){
                updatePassword()
            }
        }

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

    }

    private fun updatePassword() {
        val params = HashMap<String, String>()
        params["old_password"] = binding.oldPassword.text.toString()
        params["password"] = binding.newPassword.text.toString()
        val progressHUD = KProgressHUD(this)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        progressHUD.show()

        APIHandler(
            this,
            Request.Method.PUT,
            Constants.API.password,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    val status : Boolean = result.getBoolean("error")
                    val message : String = result.getString("message")
                    shortToast(message)
                    if (!status)
                        finish()
                }
                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    private fun isValid(): Boolean {

        if(TextUtils.isEmpty(binding.oldPassword.text.toString())){
            binding.oldPassword.error = "Please type your old password"
            return false
        }

        if(TextUtils.isEmpty(binding.newPassword.text.toString())){
            binding.newPassword.error = "Please type your new password"
            return false
        }

        if(TextUtils.isEmpty(binding.repeatPassword.text.toString())){
            binding.repeatPassword.error = "Please type your retype password"
            return false
        }

        if (binding.oldPassword.text.toString().length < 6) {
            binding.oldPassword.error = "The password is too short,  it must at least 6 characters"
            return false
        }

        if (binding.newPassword.text.toString().length < 6) {
            binding.newPassword.error = "The password is too short,  it must at least 6 characters"
            return false
        }

        if (binding.repeatPassword.text.toString().length < 6) {
            binding.repeatPassword.error = "The password is too short,  it must at least 6 characters"
            return false
        }

        if(binding.newPassword.text.toString() != binding.repeatPassword.text.toString()){
            binding.newPassword.error = "Password confirmation doesn't match Password."
            binding.repeatPassword.setText("")
            return false
        }

        return true
    }
}