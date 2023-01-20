package com.smox.smoxuser.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.AddNewCardDialogBinding
import com.smox.smoxuser.utils.listeners.OnCardValid
import com.smox.smoxuser.utils.shortToast

class AddNewCardDialog(
    var context: Context, var onCardValid: OnCardValid
) {
    private var binding: AddNewCardDialogBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.add_new_card_dialog,
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

        dialog.findViewById<ImageView>(R.id.imgCLose).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<TextView>(R.id.txtAddCard1).setOnClickListener {
            if (isValid(context)) {
                doRequestForCheckingCard()
            }
        }

        dialog.show()
    }


    private fun doRequestForCheckingCard() {

        val cardParams = binding.cardWidget.cardParams
        cardParams?.name = binding.etCardHolder1.text.toString()


        Log.e("TAG", "doRequestForCheckingCard: ${cardParams?.metadata}")
        if (cardParams != null) {
            onCardValid.onCardEntered(cardParams)
            //onCardValid.onNewCard(binding.cardWidget.paymentMethodCreateParams)
        }

    }

    private fun isValid(context: Context): Boolean {
        if (binding.etCardHolder1.text.toString().isEmpty()) {
            binding.etCardHolder1.requestFocus()
            shortToast(context.getString(R.string.err_enter_card_holder_name))
            return false
        }
        return true
    }

    fun dismissDialog() {
        dialog.dismiss()
    }
}