package com.smox.smoxuser.ui.activity.customer

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.smox.smoxuser.BuildConfig
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ActivityBookAppointmentPaymentBinding
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.manager.Constants.API.PAY_MESSAGE
import com.smox.smoxuser.manager.Constants.API.PAY_STATUS
import com.smox.smoxuser.manager.SessionManager
import com.smox.smoxuser.model.AddCardResponse
import com.smox.smoxuser.model.Appointment
import com.smox.smoxuser.model.SavedCardListResponse
import com.smox.smoxuser.retrofit.ApiRepository
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.activity.ServiceCheckoutActivity
import com.smox.smoxuser.ui.adapter.NewCardAdapter
import com.smox.smoxuser.ui.adapter.NewServiceAdapter
import com.smox.smoxuser.ui.dialog.AddNewCardDialog
import com.smox.smoxuser.utils.currentDate
import com.smox.smoxuser.utils.getIPAddress
import com.smox.smoxuser.utils.listeners.OnCardValid
import com.smox.smoxuser.utils.shortToast
import com.smox.smoxuser.viewmodel.PaymentViewModel
import com.stripe.android.Stripe
import com.stripe.android.model.CardParams
import com.stripe.android.model.PaymentMethodCreateParams
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*


class BookAppointmentPaymentActivity : BaseActivity(), NewServiceAdapter.OnServiceClicked,
    NewCardAdapter.CardActions, OnCardValid {
    private val TAG = "BookAppointmentPaymentA"
    lateinit var binding: ActivityBookAppointmentPaymentBinding
    lateinit var appointment: Appointment
    lateinit var serviceAdapter: NewServiceAdapter
    private lateinit var cardAdapter: NewCardAdapter
    private lateinit var stripe: Stripe
    var totalPay = 0.0
    var duration = 0
    lateinit var addcard: AddNewCardDialog
    val cardList: ArrayList<SavedCardListResponse.CardList> = ArrayList()
    var selectedCardPos = 0
    lateinit var resultPhone: ActivityResultLauncher<Intent>
    lateinit var viewModel: PaymentViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_book_appointment_payment)

        if (intent.hasExtra("appointment")) {
            appointment = intent.getSerializableExtra("appointment") as Appointment
        }

        viewModel = ViewModelProvider(this).get(PaymentViewModel::class.java)

        var fromTime = ""
        var toTime = ""
        if (!appointment.timeslot.isNullOrEmpty()) {
            val start = appointment.timeslot[0]
            val end = appointment.timeslot[appointment.timeslot.size - 1]
            fromTime = start.split("-")[0]
            toTime = end.split("-")[1]
        }

        binding.txtTimeSlot.text = "$fromTime-$toTime"
        appointment.slotTime = "$fromTime-$toTime"

        val outputFormat =
            SimpleDateFormat(Constants.KDateFormatter.displayFull, Locale.getDefault())
        binding.txtDate.text = outputFormat.format(appointment.preferredDate)

        stripe = Stripe(applicationContext, Constants.KStripe.publishableKey)

        setAdapter()
        getAllSavedStripeCards()

        serviceAdapter = NewServiceAdapter(this, appointment.services, false, this)
        binding.rvServiceList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvServiceList.setHasFixedSize(true)
        binding.rvServiceList.adapter = serviceAdapter
        for (items in appointment.services) {
            totalPay += items.price
            duration += items.duration
        }
        binding.txtTotalPay.text = "$$totalPay"
        appointment.duration = duration

        binding.imgAddCard.setOnClickListener {
            addcard = AddNewCardDialog(this, this)
        }

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.txtNext.setOnClickListener {
//            Log.d("++--++", "appointment.id : ${appointment.id}")

            appointment.id = 2

            if (appointment.id == -1) bookNewAppointment()
            else completePayment()
            // {"source":"debit card","barber_id":"11","appointment_id":"-1","amount":"3000","cardId":"cardList[selectedCardPos].id","stripe_customer_id":"cus_Lk5ysT1cxZgxYr","services":"98","duration":"20","date":"May 23, 11:08 PM","utc_date":"2022-05-24 07:00:00","comment":"","message":"Standard Brush Cut W / WO beard on May 24, 2022 12:20 PM with Smox","timeslots":["12:20 PM-12:30 PM","12:30 PM-12:40 PM"],"only_date":"2022-05-24"}
        }

        resultPhone =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val intent = result.data
                intent?.getStringExtra(PAY_STATUS)
                intent?.getStringExtra(PAY_MESSAGE)
                confirmAppointment()
            }

        initObservals()
    }

    private fun setAdapter() {
        cardAdapter = NewCardAdapter(this, this)
        binding.rvCard.layoutManager = LinearLayoutManager(this)
        binding.rvCard.setHasFixedSize(true)
        binding.rvCard.adapter = cardAdapter
        cardAdapter.doRefresh(cardList)
    }

    override fun onServiceClicked(pos: Int) {

    }

    override fun onCardSelected(pos: Int) {
        Log.e(TAG, "onSelectedClick: $pos")
        selectedCardPos = pos
    }

    private fun bookNewAppointment() {
        progressHUD.show()
        val jsonObject = getJsonObject()

        Log.e(TAG, "bookNewAppointment: asdasdasds $jsonObject")
        if (appointment.id == -1) viewModel.bookAppointment(this, true, jsonObject)
        else viewModel.bookAppointment(this, false, jsonObject)
    }


    private fun initObservals() {
        viewModel.isSuccess.observe(this) {
            progressHUD.dismiss()
            if (it) {
                appointment.id = viewModel.payStartRes.get()?.result?.appointment_id!!
                val intent = Intent(
                    this@BookAppointmentPaymentActivity,

                    ServiceCheckoutActivity::class.java
                )
                intent.putExtra(
                    Constants.API.PAYMENT_INTENT, viewModel.payStartRes.get()?.result?.client_secret
                )

                resultPhone.launch(intent)
            } else {

                showAlertDialog(
                    "", viewModel.errorMessage.get(), DialogInterface.OnClickListener { _, _ ->

                    }, getString(R.string.ok), null, null
                )
            }
        }

        viewModel.isBookingConfirmed.observe(this) {
            progressHUD.dismiss()
            if (it) {
                val message = "Your purchase was successful and your appointment has been scheduled"
                showAlertDialog(
                    "Success", message, DialogInterface.OnClickListener { dialog, _ ->
                        dialog.dismiss()
                        finishPayment()
                    }, getString(R.string.ok), null, null
                )
            } else {
                appointment.id = -1
                showAlertDialog(
                    "", viewModel.errorMessage.get(), DialogInterface.OnClickListener { _, _ ->
                        onBackPressed()
                        SessionManager.getInstance(this).isBack = true
                    }, getString(R.string.ok), null, null
                )

                /* val dialog = AfterPaymentWaitDialog(this)
                 dialog.show()*/
            }
        }

        viewModel.isConfirmDone.observe(this) {
            progressHUD.dismiss()
            if (!it) {
                if (viewModel.errorMessage.get().equals("Payment is Failed", true)) {
                    appointment.id = -1
                    bookNewAppointment()
                } else {
                    showAlertDialog(
                        "", viewModel.errorMessage.get(), DialogInterface.OnClickListener { _, _ ->

                        }, getString(R.string.ok), null, null
                    )
                }
            }
        }

    }

    private fun confirmAppointment() {
        progressHUD.show()
        val jsonObject = getJsonObject()
        viewModel.confirmAppointment(this, jsonObject)

    }

    private fun getJsonObject(): JsonObject {
        val ids = appointment.services.map { it.id.toString() }.joinToString(",") { it }


        val formatter = SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
        val displayFormatter =
            SimpleDateFormat(Constants.KDateFormatter.displayFull, Locale.getDefault())
        val bookDate = formatter.parse(appointment.strOnlyDate)
        val formDate = displayFormatter.format(bookDate)

        formatter.applyPattern(Constants.KDateFormatter.hourDetail)
        val localDate = formatter.format(Date())

        val d = if (appointment.officialDate == null) Date() else appointment.officialDate
        //val d = Date()
        formatter.applyPattern(Constants.KDateFormatter.server)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        val utcDate = formatter.format(d)

        val message = String.format(
            "%s on %s with %s",
            appointment.services[0].title,
            "" + formDate + " " + appointment.slotTime.split("-")[0],
            app.currentUser.firstName
        )

        val arr = JsonArray()
        for (item in appointment.timeslot) {
            arr.add(item)
        }
        val versionName = BuildConfig.VERSION_NAME
        val timeZone = TimeZone.getDefault().id

        val networkTS = getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        }
        val time = networkTS.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.time;

        val formatterr = SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault())

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time!!
        val newTimerr = formatterr.format(calendar.time)

        Log.e(TAG, "getJsonObject: $time   $versionName    $timeZone   $newTimerr")

        val jsonObject = JsonObject()
        jsonObject.addProperty("source", "debit card")
        jsonObject.addProperty("barber_id", appointment.barberId.toString())
        jsonObject.addProperty("appointment_id", appointment.id.toString())
        jsonObject.addProperty("amount", (totalPay * 100).toInt().toString())
        jsonObject.addProperty("cardId", "cardList[selectedCardPos].id")
        jsonObject.addProperty(
            "stripe_customer_id", SessionManager.getInstance(this).customerStripeId
        )
        jsonObject.addProperty("services", ids)
        jsonObject.addProperty("duration", appointment.duration.toString())
        jsonObject.addProperty(
            "date", appointment.strOnlyDate + " " + getTimeIn24(appointment.slotTime.split("-")[0])
        )
        jsonObject.addProperty("utc_date", utcDate)
        jsonObject.addProperty("comment", appointment.comment)
        jsonObject.addProperty("message", message)
        jsonObject.add("timeslots", arr)
        jsonObject.addProperty("only_date", appointment.strOnlyDate)
        jsonObject.addProperty("app_type", "Android Smox Trimsetters")
        jsonObject.addProperty("app_version", "v-" + BuildConfig.VERSION_NAME)
        jsonObject.addProperty("device_date", currentDate())
        jsonObject.addProperty("user_timezone", TimeZone.getDefault().id)
        jsonObject.addProperty("user_ip", getIPAddress(true))
        return jsonObject
    }

    private fun completePayment() {
        progressHUD.show()
        val jsonObject = getJsonObject()
Log.d("++--++","===>\n\nJsonObject \n $jsonObject")
        viewModel.confirmAppointmentPayment(this, jsonObject)
    }


    override fun onCardEntered(cardParam: CardParams) {
        progressHUD.show()

        val card: MutableMap<String, Any> = HashMap()
        card["card[number]"] = cardParam.typeDataParams["number"].toString()
        card["card[exp_month]"] = cardParam.typeDataParams["exp_month"].toString()
        card["card[exp_year]"] = cardParam.typeDataParams["exp_year"].toString()
        card["card[cvc]"] = cardParam.typeDataParams["cvc"].toString()
        card["card[name]"] = cardParam.typeDataParams["name"].toString()


        ApiRepository(this).createCardToken(card).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<AddCardResponse> {
                override fun onSubscribe(d: Disposable) {}

                override fun onNext(cardResponse: AddCardResponse) {
                    Log.e(TAG, "onNext: $cardResponse")

                    addNewCardOnStripe(cardResponse)

                }

                override fun onError(e: Throwable) {
                    progressHUD.dismiss()
                    Log.e(TAG, "onError: ${e.localizedMessage}")
                }

                override fun onComplete() {
                }
            })

    }

    private fun getTimeIn24(time: String): String {
        val date12Format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date24Format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return date24Format.format(date12Format.parse(time)!!)
    }

    private fun addNewCardOnStripe(cardResponse: AddCardResponse) {

        val tokenId: MutableMap<String, Any> = HashMap()
        tokenId["source"] = cardResponse.id

        ApiRepository(this).addNewCardOnStripe(
            SessionManager.getInstance(this).customerStripeId!!, tokenId
        ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<AddCardResponse> {
                override fun onSubscribe(d: Disposable) {}

                override fun onNext(cardResponse: AddCardResponse) {
                    progressHUD.dismiss()
                    addcard.dismissDialog()
                    shortToast("Card added successfully")
                    getAllSavedStripeCards()
                }

                override fun onError(e: Throwable) {
                    progressHUD.dismiss()
                    Log.e(TAG, "onError: ${e.localizedMessage}")

                }

                override fun onComplete() {
                }
            })

    }

    fun getAllSavedStripeCards() {
        progressHUD.show()
        ApiRepository(this).getAllCardsOnStripe(
            SessionManager.getInstance(this).customerStripeId!!
        ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<SavedCardListResponse> {
                override fun onSubscribe(d: Disposable) {}

                override fun onNext(cardResponse: SavedCardListResponse) {
                    progressHUD.dismiss()
                    if (cardResponse.data.isNotEmpty()) {
                        cardList.clear()
                        cardList.addAll(cardResponse.data)
                        cardAdapter.doRefresh(cardList)

                    } else {
                        binding.rvCard.visibility = View.GONE
                        binding.txtNoCardFound.visibility = View.VISIBLE
                    }
                }

                override fun onError(e: Throwable) {
                    progressHUD.dismiss()
                    Log.e(TAG, "onError: ${e.localizedMessage}")

                }

                override fun onComplete() {
                }
            })
    }

    private fun finishPayment() {
        val intent = Intent(this@BookAppointmentPaymentActivity, ThanksPageActivity::class.java)
        //intent.putExtra("appointment", appointment)
        intent.putExtra("barberName", appointment.barberName)
        intent.putExtra("timeSlot", appointment.slotTime)
        intent.putExtra("bookDate", appointment.strOnlyDate)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onNewCard(card: PaymentMethodCreateParams?) {

        /*stripe.createPaymentMethod(
            card!!,
            null,
            app.currentUser.stripe_customer_id,
            //app.currentUser.stripe_client_key,
            object : ApiResultCallback<PaymentMethod> {
                override fun onError(e: Exception) {
                    progressHUD.dismiss()
                    Toast.makeText(
                        this@BookAppointmentPaymentActivity,
                        e.localizedMessage, Toast.LENGTH_LONG
                    ).show()
                    Log.e(TAG, "onError: ${e.localizedMessage}")
                }

                override fun onSuccess(result: PaymentMethod) {
                    progressHUD.dismiss()
                    Log.e(TAG, "onSuccess: $result")
                    Log.e(TAG, "onSuccess: id ${result.id}")
                    Log.e(TAG, "onSuccess: ${result.card?.expiryMonth}")
                    //addNewCard(result, cardParam.name.toString())

                }
            })*/

    }

    //api call for add card
    /*private fun addNewCard(token: Token, cardHolderName: String) {
        val params = java.util.HashMap<String, String>()
        params["StripeCardToken"] = token.id
        params["CardHolderName"] = cardHolderName
        params["StripeCardId"] = token.card!!.id.toString()

        APIHandler(
            applicationContext,
            Request.Method.POST,
            Constants.API.addcard,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    Toast.makeText(
                        this@BookAppointmentPaymentActivity,
                        result.getString("message"),
                        Toast.LENGTH_LONG
                    ).show()
                    if (!(result.getBoolean("error"))) {
                        addcard.dismissDialog()
                        //getAllSavedCardList()
                    }
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    Toast.makeText(
                        this@BookAppointmentPaymentActivity,
                        error, Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
*/
    /* private fun getAllSavedCardList() {
        progressHUD.show()
        val params = HashMap<String, String>()
        APIHandler(
            this,
            Request.Method.GET,
            Constants.API.getlistcard,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()

                    cardList.clear()
                    if (result.has("result")) {
                        val jsonArray = result.getJSONArray("result")

                        for (i in 0 until jsonArray.length()) {
                            val json = jsonArray.getJSONObject(i)
                            val card = Cards(json)
                            cardList.add(card)
                        }
                        if (cardList.isNotEmpty())
                            setAdapter(cardList)
                        else {
                            binding.rvCard.visibility = View.GONE
                            binding.txtNoCardFound.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    Toast.makeText(
                        applicationContext,
                        error, Toast.LENGTH_LONG
                    ).show()
                }
            })
    }*/

}

