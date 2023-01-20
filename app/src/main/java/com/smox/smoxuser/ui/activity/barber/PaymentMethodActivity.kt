package com.smox.smoxuser.ui.activity.barber

import android.os.Bundle
import com.smox.smoxuser.R
import com.smox.smoxuser.ui.activity.BaseActivity

import kotlinx.android.synthetic.main.activity_payment_method.*


class PaymentMethodActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_method)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@PaymentMethodActivity, Constants.backButton))
        }

        btnAddCard.setOnClickListener{
           // addCard()
        }
    }
//
//    @SuppressLint("SetTextI18n")
//    private fun addCard() {
//        val card = card_input_widget.card
//        if (card == null) {
//            txtComment.text = "Card Input Error"
//            txtComment.setTextColor(ContextCompat.getColor(applicationContext, R.color.red))
//            return
//        }
//        card.currency = "USD"
//        createCardToken(card)
//    }
//
//    private fun createCardToken(card: Card){
//        val stripe = Stripe(applicationContext, sessionManager.Sp_publishableKey)
//        progressHUD.show()
//        stripe.createToken(card, object:TokenCallback{
//            override fun onError(error: Exception) {
//                progressHUD.dismiss()
//                txtComment.text = error.localizedMessage
//                txtComment.setTextColor(ContextCompat.getColor(applicationContext, R.color.red))
//            }
//
//            override fun onSuccess(token: Token) {
//                progressHUD.dismiss()
//                val cardLastDigits = token.card?.last4
//
//                val intent = Intent()
//                intent.putExtra("last_digits", cardLastDigits)
//                intent.putExtra("token", token.id)
//                setResult(RESULT_OK, intent)
//                finish()
//            }
//
//        })
//    }

}
