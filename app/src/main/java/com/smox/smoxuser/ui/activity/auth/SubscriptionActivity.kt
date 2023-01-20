package com.smox.smoxuser.ui.activity.auth

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Request
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ActivitySubscriptionBinding
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Barber
import com.smox.smoxuser.ui.activity.barber.BarberMainActivity
import com.smox.smoxuser.utils.ACTION_BILLING_CONNECT
import com.smox.smoxuser.utils.ACTION_SUBSCRIPTION_PROCESS
import com.smox.smoxuser.utils.shortToast
import com.smox.smoxuser.viewmodel.BillingViewModel
import org.json.JSONObject

class SubscriptionActivity : BaseLoginActivity(), View.OnClickListener {
    private lateinit var binding: ActivitySubscriptionBinding
    private lateinit var billingViewModel: BillingViewModel
    private lateinit var billingConnectReceiver: BroadcastReceiver
    private lateinit var subscriptionProcessReceiver: BroadcastReceiver
    private var productPrice: String = ""
    private var fromSignup: Boolean = false
    val LOG_TAG = "SubscriptionActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription)

        progressHUD.show()

        billingConnectReceiver = BillingConnectReceiver()
        subscriptionProcessReceiver = SubscriptionProcessReceiver()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_subscription)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@SignupActivity, Constants.backButton))
        }

        binding.btnSubscribeNow.setOnClickListener(this)
        binding.btnSkip.setOnClickListener(this)

        fromSignup = intent.getBooleanExtra("FromSignUp", false)
        binding.btnSkip.visibility = if (fromSignup) View.VISIBLE else View.GONE

        fetchSubscriptionProduct()
    }

    private fun fetchSubscriptionProduct() {
        billingViewModel =
            ViewModelProviders.of(this@SubscriptionActivity).get(BillingViewModel::class.java)

    }

    private inner class BillingConnectReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent

            progressHUD.dismiss()
        }
    }

    private inner class SubscriptionProcessReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            //val subPurchase : Purchase = intent.getSerializableExtra("SubscriptionPurchase") as Purchase
            val orderId = intent.getStringExtra("orderId")
            val isAutoRenewing = intent.getBooleanExtra("isAutoRenewing", false)
            val purchaseToken = intent.getStringExtra("purchaseToken")
            val purchaseTime = intent.getLongExtra("purchaseTime", 0)
            val sku = intent.getStringExtra("sku")
            val packageName = intent.getStringExtra("packageName")
            val isAcknowledged = intent.getBooleanExtra("isAcknowledged", false)
            val originalJson = intent.getStringExtra("originalJson")
            val purchaseState = intent.getIntExtra("purchaseState", 0)
            barberSubscribe(
                orderId!!,
                isAutoRenewing,
                purchaseToken!!,
                purchaseTime,
                sku!!,
                packageName!!,
                isAcknowledged,
                originalJson!!,
                purchaseState
            )
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            billingConnectReceiver,
            IntentFilter(ACTION_BILLING_CONNECT)
        )
        LocalBroadcastManager.getInstance(this).registerReceiver(
            subscriptionProcessReceiver,
            IntentFilter(ACTION_SUBSCRIPTION_PROCESS)
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(billingConnectReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(subscriptionProcessReceiver)
    }

    override fun onClick(v: View?) {
        val id = v?.id
        when (id) {
            R.id.btnSubscribeNow -> {

            }
            R.id.btnSkip -> {
                val intent = Intent(this@SubscriptionActivity, BarberMainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

    }

    private fun barberSubscribe(
        orderId: String,
        isAutoRenewing: Boolean,
        purchaseToken: String,
        purchaseTime: Long,
        sku: String,
        packageName: String,
        isAcknowledged: Boolean,
        originalJson: String,
        purchaseState: Int
    ) {
        //barberSubscribe(orderId, isAutoRenewing, purchaseToken, purchaseTime, sku, packageName, isAcknowledged, originalJson)
        val receiptData = JSONObject()
        receiptData.put("orderId", orderId)
        receiptData.put("packageName", packageName)
        receiptData.put("productId", sku)
        receiptData.put("purchaseTime", purchaseTime)
        receiptData.put("purchaseState", purchaseState)
        receiptData.put("purchaseToken", purchaseToken)
        receiptData.put("autoRenewing", isAutoRenewing)

        val params = HashMap<String, String>()
        params["transactionId"] = orderId
        params["receiptData"] = receiptData.toString()
        params["deviceType"] = "1"
        params["amount"] = "28.99"

        progressHUD.show()
        APIHandler(
            applicationContext,
            Request.Method.POST,
            Constants.API.add_subscription,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    shortToast(resources.getString(R.string.subscribe_success))

                    if (fromSignup) {

                        val subJsonObj = result.getJSONObject("result")

                        if (subJsonObj.has("is_subscribed")) {
                            sessionManager.isSubscribed = subJsonObj.getBoolean("is_subscribed")
                        }

                        if (subJsonObj.has("subscription_enddate")) {
                            sessionManager.subscription_enddate =
                                subJsonObj.getString("subscription_enddate")
                        }
                        sessionManager.isSubscribed = true


                        val user = Barber(sessionManager.userData)
                        app.currentUser = user

                        val intent =
                            Intent(this@SubscriptionActivity, BarberMainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        finish()
                    }
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            }
        )
    }
}
