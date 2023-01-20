package com.smox.smoxuser.ui.activity.barber

import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import com.android.volley.Request
import com.smox.smoxuser.R
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.utils.shortToast
import kotlinx.android.synthetic.main.activity_stripe_connect.*
import org.json.JSONObject
import java.util.*

class StripeConnectActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stripe_connect)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@BankActivity, Constants.backButton))
        }

        if(!app.currentUser.stripe_public_key.isEmpty()) {
            txtPublicKey.setText(app.currentUser.stripe_public_key)
        }
        if(!app.currentUser.stripe_secret_key.isEmpty()) {
            txtPrivateKey.setText(app.currentUser.stripe_secret_key)
        }
        btnSave.setOnClickListener {
            saveStripeInfo()
        }

    }

    private fun saveStripeInfo() {
        val publicKey = txtPublicKey.text.toString()
        if (publicKey.isEmpty()) {
            txtPublicKey.requestFocus()
            shortToast(getString(R.string.text_type_stripe_public_key))
            return
        }
        val privateKey = txtPrivateKey.text.toString()
        if (privateKey.isEmpty()) {
            txtPrivateKey.requestFocus()
            shortToast(getString(R.string.text_type_stripe_secret_key))
            return
        }

        val params = HashMap<String, String>()
        params["stripe_public_key"] = publicKey
        params["stripe_secret_key"] = privateKey

        progressHUD.show()
        APIHandler(
            applicationContext,
            Request.Method.POST,
            Constants.API.save_key,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()

                    app.currentUser.connectAccount = false //Now onwards user change there key of account so
                    app.currentUser.stripe_public_key = txtPublicKey.text.toString()
                    app.currentUser.stripe_secret_key = txtPrivateKey.text.toString()

                    sessionManager.userData = app.currentUser.getJsonString()
                    sessionManager.Sp_publishableKey = txtPublicKey.text.toString()
                    sessionManager.Sp_privateKey = txtPrivateKey.text.toString()

                    val error = result.getBoolean("error")
                    val title = if (error) "Failed" else "Success"
                    val message = result.getString("message")
                    showAlertDialog(title, message, DialogInterface.OnClickListener { _, _ ->
                        if (!error) finish()
                    }, getString(R.string.ok), null, null)
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    override fun onResume() {
        super.onResume()
    }
}
