package com.smox.smoxuser.ui.activity.customer

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.smox.smoxuser.R
import com.smox.smoxuser.data.AppointmentRepository
import com.smox.smoxuser.data.BarberRepository
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Appointment
import com.smox.smoxuser.model.Barber
import com.smox.smoxuser.stripe.controller.ErrorDialogHandler
import com.smox.smoxuser.ui.activity.AppointmentCheckoutActivity
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.activity.CardListActivity
import com.smox.smoxuser.ui.adapter.CheckOutAdapter
import com.smox.smoxuser.viewmodel.AppointmentViewModel
import com.smox.smoxuser.viewmodel.AppointmentViewModelFactory
import com.smox.smoxuser.viewmodel.BarberListViewModel
import com.smox.smoxuser.viewmodel.BarberListViewModelFactory
import com.stripe.android.PaymentSession
import com.stripe.android.PaymentSessionData
import com.stripe.android.model.Customer
import kotlinx.android.synthetic.main.content_check_out.*
import java.text.SimpleDateFormat
import java.util.*

class CheckOutActivity : BaseActivity() {

    private var mCustomer: Customer? = null
    private lateinit var mErrorDialogHandler: ErrorDialogHandler
    private var mPaymentSession: PaymentSession? = null
    private var mPaymentSessionData: PaymentSessionData? = null

    private lateinit var mProgressBar: ProgressBar
    private lateinit var mSelectPaymentButton: Button
    private lateinit var mCheckOutButton: Button

    private lateinit var barber: Barber
    private lateinit var viewModel: BarberListViewModel

    private var appointment: Appointment? = null
    private var price = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@CheckOutActivity, Constants.backButton))
        }

        appointment = intent.getSerializableExtra("appointment") as Appointment

        if (appointment != null) {
            updateUI()
        } else {
            val appointmentId = intent.getIntExtra("appointment_id", 0)
            val factory =
                AppointmentViewModelFactory(AppointmentRepository.getInstance(), appointmentId)
            val viewModel =
                ViewModelProvider(this, factory).get(AppointmentViewModel::class.java)
            viewModel.fetchList(this)
            viewModel.appointment.observe(this, Observer {
                if (it != null) {
                    appointment = it
                    updateUI()
                }
            })
        }

        val factory = BarberListViewModelFactory(BarberRepository.getInstance())
        viewModel = ViewModelProviders.of(this, factory).get(BarberListViewModel::class.java)
        barber = BarberRepository.getInstance().getBarber(appointment!!.barberId) ?: Barber()

//        PaymentConfiguration.init(sessionManager.Sp_publishableKey!!)

//        mProgressBar = findViewById(R.id.customer_progress_bar)
//        mProgressBar.visibility = View.VISIBLE
        mSelectPaymentButton = findViewById(R.id.btnAddCard)
        mCheckOutButton = findViewById(R.id.btnCheckout)
        mCheckOutButton.text = String.format("CHECK OUT ($%.2f)", price)

        mErrorDialogHandler = ErrorDialogHandler(supportFragmentManager)

//        setupCustomerSession() // CustomerSession only needs to be initialized once per app.
        mSelectPaymentButton.isEnabled = true
        mSelectPaymentButton.setOnClickListener {
            //            mPaymentSession!!.presentPaymentMethodSelection()
            val intent = Intent(this@CheckOutActivity, CardListActivity::class.java)
            startActivityForResult(intent, 101)
        }

        mCheckOutButton.setOnClickListener {
            if (barber.stripe_public_key.isBlank()) {
                showAlertDialog(
                    "",
                    resources.getString(R.string.text_key_not_get_error),
                    DialogInterface.OnClickListener { _, _ -> },
                    getString(R.string.ok),
                    null,
                    null
                )
            } else {
                val intent = Intent(this@CheckOutActivity, AppointmentCheckoutActivity::class.java)
                intent.putExtra("appointment", appointment)
                intent.putExtra("price", price)
                startActivity(intent)
            }
        }
    }

    private fun updateUI() {
        if (appointment != null) {
            appointment?.services?.forEach {
                price += it.price
            }

            val txtTotal = findViewById<TextView>(R.id.txtTotal)
            txtTotal.text = String.format("$%.2f", price)

            val adapter = CheckOutAdapter(appointment!!.services)
            val listView = findViewById<RecyclerView>(R.id.service_list)
            listView.adapter = adapter
            adapter.notifyDataSetChanged()

            val dateFormat =
                SimpleDateFormat(Constants.KDateFormatter.hourDetail, Locale.getDefault())
            if (appointment?.officialDate != null) {
                txtOfficialDate.text = dateFormat.format(appointment!!.officialDate!!)
            }
            if (appointment?.completedDate != null) {
                txtCompletedDate.text = dateFormat.format(appointment!!.completedDate!!)
            }

        }
    }

    private fun setupCustomerSession() {
//        CustomerSession.initCustomerSession(
//            ExampleEphemeralKeyProvider({ string ->
//                if (string.startsWith("Error: ")) {
//                    mErrorDialogHandler.show(string)
//                }
//            }, applicationContext)
//        )
//
//        CustomerSession.getInstance()
//            .retrieveCurrentCustomer(object : CustomerSession.CustomerRetrievalListener {
//                override fun onCustomerRetrieved(customer: Customer) {
//                    mCustomer = customer
//                    mProgressBar.visibility = View.INVISIBLE
//                    setupPaymentSession()
//                }
//
//                override fun onError(
//                    errorCode: Int,
//                    errorMessage: String?,
//                    stripeError: StripeError?
//                ) {
//                    mCustomer = null
//                    mSelectPaymentButton.isEnabled = false
////                    mCheckOutButton.isEnabled = false
//                    mErrorDialogHandler.show(errorMessage!!)
//                    mProgressBar.visibility = View.INVISIBLE
//                }
//            })
    }

    private fun setupPaymentSession() {
//        mPaymentSession = PaymentSession(this)
//        val paymentSessionInitialized = mPaymentSession!!.init(
//            PaymentSessionListenerImpl(this),
//            PaymentSessionConfig.Builder()
//                .setShippingInfoRequired(false)
//                .setShippingMethodsRequired(false)
//                .build()
//        )
//        if (paymentSessionInitialized) {
//
//            mPaymentSession!!.setCartTotal(price.toLong() * 100)
//        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /*if (requestCode == 101 && resultCode == RESULT_OK) {
            val bundle = data!!.extras
            var card: Cards = bundle!!.getSerializable("card") as Cards
            btnAddCard.text = "ADD CARD(" + card.CardBrand + " " + card.CardLastFourDigit + ")"
            this.card = card
            mCheckOutButton.isEnabled = true
        }*/

//        data?.apply {
//            mPaymentSession?.handlePaymentData(requestCode, resultCode, data)
//        }

    }

    override fun onDestroy() {
        super.onDestroy()
//        mPaymentSession?.onDestroy()
    }

//    private fun onPaymentSessionDataChanged(data: PaymentSessionData) {
//        mPaymentSessionData = data
//        mProgressBar.visibility = View.VISIBLE
//
//        CustomerSession.getInstance()
//            .retrieveCurrentCustomer(object : CustomerSession.CustomerRetrievalListener {
//                override fun onCustomerRetrieved(customer: Customer) {
//                    mCustomer = customer
//                    mProgressBar.visibility = View.INVISIBLE
//                    if (mPaymentSessionData != null) {
//                        if (data.selectedPaymentMethodId != null && mCustomer != null) {
//
//                            val source = mCustomer!!
//                                .getSourceById(data.selectedPaymentMethodId!!)
//                            if (source != null) {
//                                val cardSource = source.asSource()
//                                if (cardSource != null) {
//                                    val scd = cardSource.sourceTypeModel as SourceCardData
//                                    btnAddCard.text = String.format(
//                                        "ADD CARD(%s %s)",
//                                        scd.brand?.toLowerCase()?.capitalize(),
//                                        scd.last4
//                                    )
//
//                                }
////                                mCheckOutButton.isEnabled = data.isPaymentReadyToCharge
//                            }
//                        }
//                    }
//                }
//
//                override fun onError(
//                    errorCode: Int,
//                    errorMessage: String?,
//                    stripeError: StripeError?
//                ) {
//                    mProgressBar.visibility = View.INVISIBLE
//                }
//            })
//    }

//    private class PaymentSessionListenerImpl(activity: CheckOutActivity) :
//        PaymentSession.ActivityPaymentSessionListener<CheckOutActivity>(activity) {
//
//        override fun onCommunicatingStateChanged(isCommunicating: Boolean) {
//            val activity = listenerActivity ?: return
//
//            activity.mProgressBar.visibility = if (isCommunicating) View.VISIBLE else View.INVISIBLE
//        }
//
//        override fun onError(errorCode: Int, errorMessage: String?) {
//            val activity = listenerActivity ?: return
//
//            activity.mErrorDialogHandler.show(errorMessage!!)
//        }
//
//        override fun onPaymentSessionDataChanged(data: PaymentSessionData) {
//            val activity = listenerActivity ?: return
//
//            activity.onPaymentSessionDataChanged(data)
//        }
//    }
}

