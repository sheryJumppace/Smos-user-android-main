package com.smox.smoxuser.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ActivityServiceCheckoutBinding
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.manager.Constants.API.PAY_MESSAGE
import com.smox.smoxuser.manager.Constants.API.PAY_STATUS
import com.smox.smoxuser.utils.openKeyboard
import com.stripe.android.PaymentConfiguration
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.payments.paymentlauncher.PaymentLauncher
import com.stripe.android.payments.paymentlauncher.PaymentResult
import kotlinx.coroutines.launch

class ServiceCheckoutActivity : BaseActivity() {
    lateinit var binding: ActivityServiceCheckoutBinding
    private lateinit var paymentIntentClientSecret: String
    private lateinit var paymentLauncher: PaymentLauncher
    lateinit var timer: CountDownTimer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_service_checkout)

        paymentIntentClientSecret = intent.getStringExtra(Constants.API.PAYMENT_INTENT).toString()
        val calledFrom = intent.getStringExtra(Constants.API.CALLED_FROM).toString()
        PaymentConfiguration.init(this, Constants.KStripe.publishableKey)
        val paymentConfiguration = PaymentConfiguration.getInstance(this.applicationContext)
        paymentLauncher = PaymentLauncher.Companion.create(
            this,
            paymentConfiguration.publishableKey,
            paymentConfiguration.stripeAccountId
        ) { paymentResult ->
            if (progressHUD.isShowing)
                progressHUD.dismiss()
            val intent = Intent()
            when (paymentResult) {
                is PaymentResult.Completed -> {
                    "Completed!"
                    Log.e("PayByCardAct", "Completed ")
                    intent.putExtra(PAY_STATUS, "Success")
                    intent.putExtra(PAY_MESSAGE, "Your payment is success")
                    setResult(RESULT_OK, intent)
                    finish()
                }
                is PaymentResult.Canceled -> {
                    Log.e("PayByCardAct", ": Canceled")
                    intent.putExtra(PAY_STATUS, "Canceled")
                    intent.putExtra(PAY_MESSAGE, "Your payment is Canceled")
                    setResult(RESULT_CANCELED, intent)
                    finish()

                }
                is PaymentResult.Failed -> {
                    Log.e("PayByCardAct", "Failed: " + paymentResult.throwable.message)
                    intent.putExtra(PAY_STATUS, "Failed")
                    intent.putExtra(PAY_MESSAGE, paymentResult.throwable.message)
                    setResult(RESULT_CANCELED, intent)
                    finish()
                }
            }
        }

        binding.txtPay.setOnClickListener {
            hideKeyboard()
            if (calledFrom != "Cart")
                stopTimer()
            binding.cardWidget.paymentMethodCreateParams?.let { params ->
                progressHUD.show()
                val confirmParams = ConfirmPaymentIntentParams
                    .createWithPaymentMethodCreateParams(params, paymentIntentClientSecret)
                lifecycleScope.launch {
                    paymentLauncher.confirm(confirmParams)
                }
            }
        }
        binding.cardWidget.requestFocus()
        openKeyboard(this)

        //show timer when book appointment
        if (calledFrom == "Cart") {
            binding.waitTimer.visibility = View.GONE
        } else {
            binding.waitTimer.visibility = View.VISIBLE
            startTimer()
        }


    }

    private fun startTimer() {
        timer = object : CountDownTimer(120000, 1000) {
            override fun onFinish() {
                hideKeyboard()
                setResult(RESULT_CANCELED)
                finish()
            }

            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val min = (millisUntilFinished / 60000) % 60
                val sec = (millisUntilFinished / 1000) % 60
                binding.waitTimer.text = String.format(getString(R.string.timer), min, sec)
            }
        }
        timer.start()
    }

    private fun stopTimer() {
        timer.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("payment card entry page", "onDestroy: ")
    }


}