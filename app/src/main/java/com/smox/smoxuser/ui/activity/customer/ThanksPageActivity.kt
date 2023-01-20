package com.smox.smoxuser.ui.activity.customer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import androidx.databinding.DataBindingUtil
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ActivityThanksPageBinding
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Appointment
import com.smox.smoxuser.ui.activity.home.Home2Activity
import java.text.SimpleDateFormat
import java.util.*

class ThanksPageActivity : AppCompatActivity() {
    lateinit var binding:ActivityThanksPageBinding
    lateinit var appointment: Appointment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_thanks_page)

        var barberName=""
        var timeSlot=""
        var bookDateStr=""
        if (intent.hasExtra("barberName")) {
            //appointment = intent.getSerializableExtra("appointment") as Appointment
            barberName=intent.getStringExtra("barberName").toString()
            timeSlot=intent.getStringExtra("timeSlot").toString()
            bookDateStr=intent.getStringExtra("bookDate").toString()
        }
        binding.txtAppointments.setOnClickListener {
            startActivity(Intent(this@ThanksPageActivity, AppointmentListActivity::class.java)
                .putExtra("calledFrom", "ThanksPage"))
        }

        val bookDate=SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault()).parse(bookDateStr)
        val dateFormat = SimpleDateFormat(Constants.KDateFormatter.displayFull, Locale.getDefault())
        val date = dateFormat.format(bookDate)

        val message="<b>Thank you!</b> for booking appointment with <b>"+
                barberName+
                "</b> on <b>"+date+"</b> between <b>"+timeSlot+"</b>"

        binding.txtBookingText.text = Html.fromHtml(message)

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()

        val intent = Intent(this@ThanksPageActivity, Home2Activity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}