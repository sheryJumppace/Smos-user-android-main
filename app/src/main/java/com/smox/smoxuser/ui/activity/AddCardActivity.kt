package com.smox.smoxuser.ui.activity

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.widget.Toast
import com.android.volley.Request
import com.smox.smoxuser.R
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.manager.SessionManager
import com.smox.smoxuser.utils.shortToast
import com.stripe.android.Stripe
import com.stripe.android.model.Token
import kotlinx.android.synthetic.main.activity_add_card.*
import kotlinx.android.synthetic.main.activity_booking_appointment.toolbar
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class AddCardActivity : BaseActivity(), TextWatcher {


    private lateinit var month: String
    private lateinit var stripe: Stripe

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_card)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            toolbar.title = "ADD CARD"
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@BookingAppointmentActivity, Constants.backButton))
        }

        stripe = Stripe(this, SessionManager.getInstance(this).Sp_publishableKey.toString())
        et_expiry_date.addTextChangedListener(this)
        btnSave.setOnClickListener {
            btnSave.setEnabled(false)
            Handler().postDelayed({ btnSave.setEnabled(true) }, 500)

            if (isValid()) {
                //doRequestForCheckingCard()
            }
        }
    }

    //check card validity using stripe
    /*private fun doRequestForCheckingCard() {
        val card_number = et_card_number.text.toString()
        progressHUD.show()
        val card = Card(
            card_number.toInt(),
            Integer.valueOf(month),
            Integer.valueOf(et_expiry_date.text.toString().substring(3)).toString(),
            et_cvv.text.toString(),
            et_card_holder_name.text.toString(),
            null,
            null,
            null,
            null,
            null,
            null,
            Card.UNKNOWN,
            card_number.substring(card_number.length - 4),
            null,
            null,
            null,
            null,
            null,
            null
        )

        stripe.createToken(card, object : TokenCallback {
            override fun onSuccess(token: Token) {
                doRequestForAddCard(token)
            }

            override fun onError(error: Exception) {
                Toast.makeText(
                    this@AddCardActivity,
                    error.localizedMessage, Toast.LENGTH_LONG
                ).show()
                progressHUD.dismiss()
            }

        })
    }*/

    //api call for add card
    private fun doRequestForAddCard(token: Token) {
        val params = HashMap<String, String>()
        params["StripeCardToken"] = token.id
        params["CardHolderName"] = et_card_holder_name.text.toString()
        params["StripeCardId"] = token.card!!.id.toString()

        APIHandler(
            applicationContext,
            Request.Method.POST,
            Constants.API.addcard,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    shortToast(getString(R.string.text_added_card))
                    if (!(result.getBoolean("error"))) {
                        finish()
                    }
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)

                }
            })
    }

    //check input data validation
    private fun isValid(): Boolean {
        if (et_card_holder_name.text.toString().isEmpty()) {
            et_card_holder_name.requestFocus()
            shortToast(getString(R.string.err_enter_card_holder_name))
            return false
        }

        if (et_card_number.text.toString().isEmpty()) {
            et_card_number.requestFocus()
            shortToast(getString(R.string.err_enter_card_number))
            return false
        }

        if (et_card_number.text.toString().length < 16) {
            et_card_number.requestFocus()
            shortToast(getString(R.string.err_enter_valid_card))
            return false
        }

        if (et_expiry_date.text.toString().isEmpty()) {
            et_expiry_date.requestFocus()
            shortToast(getString(R.string.err_enter_expiry_date))
            return false
        }

        if (!validateCardExpiryDate(et_expiry_date.text.toString())) {
            et_expiry_date.requestFocus()
            shortToast(getString(R.string.err_enter_valid_expiry_date))
            return false
        }

        if (!validPastDate(et_expiry_date.text.toString())) {
            et_expiry_date.requestFocus()
            shortToast(getString(R.string.err_enter_valid_expiry_date))
            return false
        }

        if (et_cvv.text.toString().isEmpty()) {
            et_cvv.requestFocus()
            shortToast(getString(R.string.err_enter_cvv))
            return false
        }

        if (et_cvv.text.toString().length < 3 && et_cvv.text.toString().length < 5) {
            et_cvv.requestFocus()
            shortToast(getString(R.string.err_enter_valid_cvv))
            return false
        }
        return true
    }

    //check expiry date validity
    fun validateCardExpiryDate(expiryDate: String): Boolean {
        return expiryDate.matches(Regex("(?:0[1-9]|1[0-2])/[0-9]{2}"))
    }

    //Check past date is selected
    fun validPastDate(expiryDate: String): Boolean {
        val sdf = SimpleDateFormat("dd",Locale.getDefault())
        val currentDate = sdf.format(Date())
        val simpleDateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        simpleDateFormat.setLenient(false)
        val expiry: Date = simpleDateFormat.parse(currentDate + "/" + expiryDate)!!
        val expired: Boolean =
            getZeroTimeDate(expiry).after(getZeroTimeDate(Date())) || getZeroTimeDate(expiry).equals(
                getZeroTimeDate(Date())
            )
        return expired
    }

    //set time value to 0 of date
    private fun getZeroTimeDate(date: Date): Date {
        var date: Date = date
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        date = calendar.time
        return date
    }

    //Convert string to base 64 string
    private fun convertStringToBase64(data: String): String {
        val data = data.toByteArray(charset("UTF-8"))
        val base64 = Base64.encodeToString(data, Base64.DEFAULT)
        return base64
    }

    override fun afterTextChanged(s: Editable?) {

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, added: Int) {
        val len = s.toString().length

        if (len == 2) {
            month = s.toString()
        } else if (len < 2) {
            month = ""
        }

        if (added == 0 && len == 3) {
            et_expiry_date.setText(s.toString().replace("/", ""))
            et_expiry_date.setSelection(et_expiry_date.text.length)
        }

        if (added == 1 && len == 3) {
            et_expiry_date.setText(month + "/" + s!![2])
            et_expiry_date.setSelection(et_expiry_date.text.length)
        }
    }
}
