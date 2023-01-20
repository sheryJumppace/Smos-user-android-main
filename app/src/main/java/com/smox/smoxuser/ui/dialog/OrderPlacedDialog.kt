package com.smox.smoxuser.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.databinding.DataBindingUtil
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.AddNewCardDialogBinding
import com.smox.smoxuser.databinding.OrderPlacedDialogBinding
import com.smox.smoxuser.ui.activity.home.Home2Activity
import com.smox.smoxuser.ui.activity.orders.OrdersActivity
import com.smox.smoxuser.utils.listeners.OnCardValid
import com.smox.smoxuser.utils.shortToast

class OrderPlacedDialog(
    var context: Activity
) {
    private var binding: OrderPlacedDialogBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.order_placed_dialog,
        null,
        false
    )
    val dialog = Dialog(context)

    init {
        dialog.setContentView(binding.root)
        dialog.setCanceledOnTouchOutside(true)
        if (dialog.window != null) {
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.setGravity(Gravity.CENTER)
        }

        dialog.findViewById<AppCompatButton>(R.id.btnOk).setOnClickListener {
            val intent = Intent(context, OrdersActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            context.finish()
            dialog.dismiss()
        }

        dialog.show()
    }
}