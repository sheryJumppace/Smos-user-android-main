package com.smox.smoxuser.ui.activity

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.smox.smoxuser.R
import com.smox.smoxuser.data.AppointmentRepository
import com.smox.smoxuser.data.BarberRepository
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Appointment
import com.smox.smoxuser.model.Barber
import com.smox.smoxuser.model.type.AppointmentType
import com.smox.smoxuser.ui.activity.home.Home2Activity
import com.smox.smoxuser.utils.shortToast
import com.smox.smoxuser.viewmodel.BarberListViewModel
import com.smox.smoxuser.viewmodel.BarberListViewModelFactory
import com.stripe.android.Stripe

import com.stripe.android.model.Token
import kotlinx.android.synthetic.main.activity_add_card.*
import kotlinx.android.synthetic.main.activity_booking_appointment.toolbar
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class AppointmentCheckoutActivity : BaseActivity(), TextWatcher {

    private lateinit var month: String
    private lateinit var stripe: Stripe
    private lateinit var barber:Barber
    private lateinit var viewModel: BarberListViewModel

    private var appointment: Appointment? = null
    private var price = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_card)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            toolbar.title = "CHECKOUT"
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@BookingAppointmentActivity, Constants.backButton))
        }
        appointment = intent.getSerializableExtra("appointment") as Appointment
        price = intent.getFloatExtra("price", 0.0f)

        val factory = BarberListViewModelFactory(BarberRepository.getInstance())
        viewModel = ViewModelProvider(this, factory).get(BarberListViewModel::class.java)
        barber = BarberRepository.getInstance().getBarber(appointment!!.barberId) ?: Barber()

        //stripe = Stripe(this)

        et_expiry_date.addTextChangedListener(this)
        btnSave.setText(getString(R.string.text_proceed))
        btnSave.setOnClickListener {
            btnSave.setEnabled(false)
            Handler().postDelayed({ btnSave.setEnabled(true) }, 500)

            if (isValid()) {
                //doRequestForCheckingCard()
            }
        }
    }

    //check card validity using stripe
  /*  private fun doRequestForCheckingCard() {
        val card_number = et_card_number.text.toString()
        progressHUD.show()
        val card = Card(
            card_number,
            Integer.valueOf(month),
            Integer.valueOf(et_expiry_date.text.toString().substring(3)),
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

        try {
            stripe.createToken(card, barber.stripe_public_key, object : TokenCallback {
                override fun onSuccess(token: Token) {
                    if (card != null) {
                        completeCharge(
                            token.id, token.card!!.id.toString()
                        )
                    } else {
                        Toast.makeText(
                            this@AppointmentCheckoutActivity,
                            getString(R.string.text_select_card), Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onError(error: Exception) {
                    Toast.makeText(
                        this@AppointmentCheckoutActivity,
                        error.localizedMessage, Toast.LENGTH_LONG
                    ).show()
                    progressHUD.dismiss()
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            progressHUD.dismiss()
        }
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
        val sdf = SimpleDateFormat("dd")
        val currentDate = sdf.format(Date())
        val simpleDateFormat = SimpleDateFormat("dd/MM/yy")
        simpleDateFormat.setLenient(false)
        val expiry: Date = simpleDateFormat.parse(currentDate + "/" + expiryDate)
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

    private fun completeCharge(
        source: String,
        stripeCardId: String
    ) {
        if (appointment == null) return
        val params = HashMap<String, String>()
        params["source"] = source
        params["barber_id"] = appointment!!.barberId.toString()
        params["appointment_id"] = appointment!!.id.toString()
        params["amount"] = (price * 100).toInt().toString()
        params["cardId"] = stripeCardId

        val ids = appointment!!.services.map { it.id.toString() }.joinToString(",") { it }

        val d = if (appointment?.officialDate == null) Date() else appointment?.officialDate
        val formatter = SimpleDateFormat(Constants.KDateFormatter.server, Locale.getDefault())
        val serverDate = formatter.format(d!!)
        formatter.applyPattern(Constants.KDateFormatter.hourDetail)
        val localDate = formatter.format(d)

        formatter.applyPattern(Constants.KDateFormatter.server)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        val utcDate = formatter.format(d)

        val message = String.format(
            "%s on %s with %s",
            appointment!!.services[0].title,
            localDate,
            app.currentUser.firstName
        )

        params["services"] = ids
        params["duration"] = appointment!!.duration.toString()
        //params["date"] = serverDate
        params["date"] = utcDate
        params["utc_date"] = utcDate
        params["comment"] = appointment!!.comment
        params["message"] = message

        /*val dateFormat = SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
        val date = dateFormat.format(Date())
        params["date"] = date*/

        for ((key, value) in params) {
            println("$key = $value")
        }

        progressHUD.show()
        APIHandler(
            applicationContext,
            Request.Method.POST,
            Constants.API.charge,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {

                    val errorStatus = result.getBoolean("error")

                    val apiMessage = result.getString("message")

                    if (errorStatus) {
                        showAlertDialog(
                            "",
                            apiMessage,
                            DialogInterface.OnClickListener { _, _ ->
                                finishPayment()
                            },
                            getString(R.string.ok),
                            null,
                            null
                        )
                    } else {
                        appointment!!.status = AppointmentType.Completed
                        AppointmentRepository.getInstance().updateAppointment(appointment!!)

                        progressHUD.dismiss()

                        val message =
                            if (appointment!!.id > 0) "Your Purchase Was Successful!" else "Your Purchase Was Successful and your appointment has been scheduled"
                        //val message = if(appointment!!.id  > 0) "Your Purchase Was Successful!" else apiMessage

                        showAlertDialog(
                            "Success",
                            message,
                            DialogInterface.OnClickListener { _, _ ->
                                finishPayment()
                            },
                            getString(R.string.ok),
                            null,
                            null
                        )
                    }
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    private fun finishPayment() {
        //val intent = Intent(this@AppointmentCheckoutActivity, CustomerMainActivity::class.java)
        val intent = Intent(this@AppointmentCheckoutActivity, Home2Activity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}