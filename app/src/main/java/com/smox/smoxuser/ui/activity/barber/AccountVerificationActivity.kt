package com.smox.smoxuser.ui.activity.barber

import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import com.android.volley.Request
import com.smox.smoxuser.R
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Verify
import com.smox.smoxuser.model.VerifyField
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.adapter.VerifyAdapter
import com.smox.smoxuser.utils.shortToast
import kotlinx.android.synthetic.main.activity_account_verification.*
import org.json.JSONObject
import java.util.HashMap

class AccountVerificationActivity : BaseActivity() {

    private lateinit var verify: Verify

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_verification)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@AccountVerificationActivity, Constants.backButton))
        }

        verify = intent.getSerializableExtra("verify") as Verify
        val adapter = VerifyAdapter(verify.fields)
        verify_field_list.adapter = adapter
        adapter.notifyDataSetChanged()

        btnVerify.setOnClickListener {
            val fields = adapter.getVerifyFields()
            verifyIdentity(fields)
        }
    }

    private fun verifyIdentity(fields: ArrayList<VerifyField>) {
        val params = HashMap<String, String>()
        fields.forEach {
            params[it.id] = it.value
        }
        params["token_id"] = verify.accountToken
        progressHUD.show()
        APIHandler(
            applicationContext,
            Request.Method.POST,
            Constants.API.update_card,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    val message = result.getString("message")
                    showAlertDialog("Success", message, DialogInterface.OnClickListener { _, _ ->
                        finish()
                    }, getString(R.string.ok), null, null)
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

}
