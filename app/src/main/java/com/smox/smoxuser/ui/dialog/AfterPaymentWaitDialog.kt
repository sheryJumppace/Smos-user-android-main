package com.smox.smoxuser.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.smox.smoxuser.R

class AfterPaymentWaitDialog(
    val context: Activity,
) : Dialog(context) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.after_payment_wait_dialog)
        setCanceledOnTouchOutside(false)
        if (window != null) {
            window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window!!.setGravity(Gravity.CENTER)
        }


        val txtMessage = findViewById<TextView>(R.id.txtmessage)
        val btnAccept = findViewById<AppCompatButton>(R.id.btnWait)
        btnAccept.isEnabled=false

        btnAccept.setOnClickListener {
            context.finish()
            dismiss()
        }

        object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                txtMessage.text = String.format(
                    context.getString(R.string.waitTimer),
                    millisUntilFinished / 1000
                )
            }

            override fun onFinish() {
                btnAccept.text = context.getString(R.string.ok)
                btnAccept.isEnabled=true
            }
        }.start()
    }

}