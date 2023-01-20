package com.smox.smoxuser.ui.activity.barber

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Patterns
import android.webkit.URLUtil
import android.widget.DatePicker
import android.widget.Toast
import com.android.volley.Request
import com.smox.smoxuser.R
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.activity.WebViewActivity
import com.smox.smoxuser.ui.fragment.StatePickerDialogFragment
import com.smox.smoxuser.utils.shortToast
import kotlinx.android.synthetic.main.activity_bank.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class BankActivity : BaseActivity(), DatePickerDialog.OnDateSetListener,
    StatePickerDialogFragment.Listener {

    companion object {
        private const val ADD_PAYMENT_METHOD_REQUEST = 0x01
    }

    private var cardLastDigits: String? = null
    private var cardTokenId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@BankActivity, Constants.backButton))
        }

        val sp = Html.fromHtml(getString(R.string.stripe_terms))
        txtTerms.text = sp

        btnMobilePay.setOnClickListener {
            btnMobilePay.isEnabled = false
            addCard()
        }

        txtTerms.setOnClickListener {
            txtTerms.isEnabled = false
            terms()
        }

        //txtBirthday.isEnabled = false
        txtBirthday.setOnClickListener {
            openDatePicker()
        }
        //txtState.isEnabled = false
        txtState.setOnClickListener {
            didOpenStatePicker()
        }

        btnSave.setOnClickListener {
            addPersonalInfo()
        }

//        txtFirstName.setText("Luz")
//        txtLastName.setText("Mendez")
//        txtEmail.setText("lumend@chammy.info")
//        txtPhone.setText("5069654022")
//        txtBirthday.setText("1977-08-02")
//        txtLastSSN.setText("0369")
//        txtAddressLine1.setText("55 N Macarthur Blvd")
//        txtCity.setText("Irving")
//        txtState.setText("TX-Texas")
//        txtZip.setText("75063")
//        txtWeb.setText("https://www.facebook.com/public/Luz-Mendez")
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ADD_PAYMENT_METHOD_REQUEST) {
                data?.apply {
                    cardLastDigits = getStringExtra("last_digits")
                    cardTokenId = getStringExtra("token")
                    txtMobilePay.text = "***$cardLastDigits"
                }
            }
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val formatter = SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
        val time = formatter.format(cal.time)
        txtBirthday.setText(time)
    }

    override fun onSelectState(state: String) {
        txtState.text = state
    }

    private fun addCard() {
        val intent = Intent(this@BankActivity, PaymentMethodActivity::class.java)
        startActivityForResult(intent, ADD_PAYMENT_METHOD_REQUEST)
        overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
    }

    private fun terms() {
        val intent = Intent(this@BankActivity, WebViewActivity::class.java)
        intent.putExtra("url", Constants.KUrl.stripe)
        intent.putExtra("title", resources.getString(R.string.title_terms))
        startActivity(intent)
        overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
    }

    private fun openDatePicker() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dialog = DatePickerDialog(
            this@BankActivity,
            R.style.DialogTheme,
            this@BankActivity,
            year,
            month,
            day
        )
        dialog.show()
    }

    private fun didOpenStatePicker() {
        val statePickerDialogFragment = StatePickerDialogFragment()
        statePickerDialogFragment.show(supportFragmentManager, "State Picker")
    }

    private fun addPersonalInfo() {
        val firstName = txtFirstName.text.toString()
        if (firstName.isEmpty()) {
            txtFirstName.error = "Please type your first name"
            txtFirstName.requestFocus()
            return
        }
        val lastName = txtLastName.text.toString()
        if (lastName.isEmpty()) {
            txtLastName.error = "Please type your last name"
            txtLastName.requestFocus()
            return
        }
        val email = txtEmail.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmail.error = "Please type your email"
            txtEmail.requestFocus()
            return
        }
        val phoneNumber = txtPhone.text.toString()
        if (phoneNumber.isEmpty()) {
            txtPhone.error = "Please type your phone number"
            txtPhone.requestFocus()

            return
        }
        val birthday = txtBirthday.text.toString()
        if (birthday.isEmpty()) {
            txtBirthday.error = "Please type your birthday"
            txtBirthday.requestFocus()
            return
        }
        val ssnLast = txtLastSSN.text.toString()
        if (ssnLast.length != 4) {
            txtLastSSN.error = "Please type SSN last 4 digits"
            txtLastSSN.requestFocus()
            return
        }
        val addressLine1 = txtAddressLine1.text.toString()
        if (addressLine1.isEmpty()) {
            txtAddressLine1.error = "Please type your home address"
            txtAddressLine1.requestFocus()
            return
        }

        val addressLine2 = txtAddressLine2.text.toString()

        val city = txtCity.text.toString()
        if (city.isEmpty()) {
            txtCity.error = "Please type your city"
            txtCity.requestFocus()
            return
        }
        val state = txtState.text.toString()
        if (state.isEmpty()) {
            txtState.error = "Please select your state"
            txtState.requestFocus()
            return
        }

        val zipCode = txtZip.text.toString()
        if (zipCode.isEmpty()) {
            txtZip.error = "Please type your zip code"
            txtZip.requestFocus()
            return
        }

        val link = txtWeb.text.toString()
        if (!URLUtil.isValidUrl(link)) {
            txtWeb.error = "Please type your profile link"
            txtWeb.requestFocus()
            return
        }

        if (cardTokenId == null) {
            showAlertDialog(
                "",
                "Please add  your debit card",
                DialogInterface.OnClickListener { _, _ ->
                    addCard()
                },
                getString(R.string.ok),
                null,
                null
            )
            return
        }
        val prefixSate = state.split(" - ")[0]

        val formatter = SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
        val date = formatter.parse(birthday)

        formatter.applyPattern("yyyy")
        val year = formatter.format(date)
        formatter.applyPattern("MM")
        val month = formatter.format(date)
        formatter.applyPattern("dd")
        val day = formatter.format(date)


        val params = HashMap<String, String>()
        params["first_name"] = firstName
        params["last_name"] = lastName
        params["email"] = email
        params["phone"] = phoneNumber
        params["dob_day"] = day
        params["dob_month"] = month
        params["dob_year"] = year
        params["ssn_last_4"] = ssnLast
        params["address_line1"] = addressLine1
        params["address_line2"] = addressLine2
        params["city"] = city
        params["state"] = prefixSate
        params["postal_code"] = zipCode
        params["link"] = link
        params["token_id"] = cardTokenId!!
        params["card_last_4"] = cardLastDigits!!

        progressHUD.show()
        APIHandler(
            applicationContext,
            Request.Method.POST,
            Constants.API.register_card,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
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
        btnMobilePay.isEnabled = true
        txtTerms.isEnabled = true
    }
}
