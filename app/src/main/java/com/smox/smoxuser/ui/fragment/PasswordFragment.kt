package com.smox.smoxuser.ui.fragment

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.android.volley.Request
import com.kaopiz.kprogresshud.KProgressHUD

import com.smox.smoxuser.databinding.FragmentPasswordBinding
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.utils.shortToast
import org.json.JSONObject
import java.util.HashMap


class PasswordFragment : Fragment() {
    private lateinit var txtCurrentPassword:EditText
    private lateinit var txtNewPassword:EditText
    private lateinit var txtConfirmPassword:EditText
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentPasswordBinding.inflate(inflater, container, false)
        txtCurrentPassword = binding.txtPassword
        txtNewPassword = binding.txtNewPassword
        txtConfirmPassword = binding.txtConfirmPassword

        binding.btnSignUp.setOnClickListener{
            updatePassword()
        }
        return binding.root
    }

    private fun updatePassword() {
        val oldPassword =  txtCurrentPassword.text.toString()
        val newPassword =  txtNewPassword.text.toString()
        val confirmPassword =  txtConfirmPassword.text.toString()

        if(TextUtils.isEmpty(oldPassword)){
            txtCurrentPassword.error = "Please type your old password"
            return
        }

        if(TextUtils.isEmpty(newPassword)){
            txtCurrentPassword.error = "Please type your new password"
            return
        }

        if(TextUtils.isEmpty(confirmPassword)){
            txtCurrentPassword.error = "Please type your retype password"
            return
        }

        if (oldPassword.length < 6) {
            txtCurrentPassword.error = "The password is too short,  it must at least 6 characters"
            return
        }

        if (newPassword.length < 6) {
            txtNewPassword.error = "The password is too short,  it must at least 6 characters"
            return
        }

        if (confirmPassword.length < 6) {
            txtConfirmPassword.error = "The password is too short,  it must at least 6 characters"
            return
        }

        if(newPassword != confirmPassword){
            txtNewPassword.error = "Password confirmation doesn't match Password."
            txtConfirmPassword.setText("")
            return
        }

        val params = HashMap<String, String>()
        params["old_password"] = oldPassword
        params["password"] = newPassword
        val progressHUD = KProgressHUD(activity)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        progressHUD.show()

        APIHandler(
            context!!,
            Request.Method.PUT,
            Constants.API.password,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    var status : Boolean = result.getBoolean("error")
                    var message : String = result.getString("message")
                    if(!status){
                        fragmentManager?.popBackStack()
                    }
                    shortToast(message)
                }
                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

}
