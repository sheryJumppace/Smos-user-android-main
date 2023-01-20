package com.smox.smoxuser.ui.activity.customer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.android.volley.Request
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.smox.smoxuser.R
import com.smox.smoxuser.data.AppointmentRepository
import com.smox.smoxuser.data.ReviewRepository
import com.smox.smoxuser.databinding.ActivityAppointmentDetailsNewBinding
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Appointment
import com.smox.smoxuser.model.Category
import com.smox.smoxuser.model.type.AppointmentType
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.activity.barber.BookingAppointmentActivity
import com.smox.smoxuser.ui.dialog.AddUpNextOptionDialog
import com.smox.smoxuser.ui.dialog.DetailRatingDialog
import com.smox.smoxuser.ui.dialog.RatingDialog
import com.smox.smoxuser.utils.FULL_IMAGE_PATH
import com.smox.smoxuser.utils.shortToast
import com.smox.smoxuser.viewmodel.AppointmentViewModel
import com.smox.smoxuser.viewmodel.AppointmentViewModelFactory
import com.smox.smoxuser.viewmodel.ReviewListViewModel
import com.smox.smoxuser.viewmodel.ReviewListViewModelFactory
import kotlinx.android.synthetic.main.activity_appointment_detail.*
import kotlinx.android.synthetic.main.dialog_detail_rating.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AppointmentDetailsNewActivity : BaseActivity() {
    private val TAG = "AppointmentDetailsNewAc"
    lateinit var binding: ActivityAppointmentDetailsNewBinding
    private lateinit var viewModel: AppointmentViewModel
    private lateinit var viewModelRating: ReviewListViewModel
    private var isBarber = false
    private lateinit var appointment: Appointment
    var items: ArrayList<Category> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_appointment_details_new)

        val id = intent.getIntExtra(Constants.API.APPOINT_ID, 0)

        val factoryRating = ReviewListViewModelFactory(ReviewRepository.getInstance())
        viewModelRating =
            ViewModelProviders.of(this, factoryRating).get(ReviewListViewModel::class.java)

        val factory = AppointmentViewModelFactory(AppointmentRepository.getInstance(), id)
        viewModel = ViewModelProvider(this, factory).get(AppointmentViewModel::class.java)
        viewModel.appointment.observe(this, Observer {
            if (it != null) {
                appointment = it
                updateUI()
            }
        })
        viewModelRating.isUpdated.observe(this, Observer {
            if (it){
                viewModel.fetchList(this)
            }
        })
        viewModel.appointment.value=null
        viewModel.fetchList(this)

        clickListner()

    }

    private fun clickListner() {
        binding.clickListener = View.OnClickListener {
            when (it.id) {
                R.id.btnEdit -> {
                    val intent = Intent(
                        this@AppointmentDetailsNewActivity,
                        BookingAppointmentActivity::class.java
                    )
                    val user = if (isBarber) app.currentUser else appointment.user
                    intent.putExtra("barber", user)
                    intent.putExtra("appointment", appointment)
                    intent.putExtra("CatList", items)
                    startActivity(intent)
                    overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
                }
                R.id.txtCancel -> {
                    showCancelDialog()
                }
                R.id.txtAddReview -> {
                    showRatingDialog()
                }
                R.id.txtViewRating->{
                    showDetailRatingDialog()
                }
            }
        }
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun showDetailRatingDialog() {
        val dialog = DetailRatingDialog(this, appointment.comment,appointment.cleanRating!!,appointment.workRating!!,
        appointment.behaveRating!!, appointment.rating!!)
        dialog.show()
    }

    private fun showRatingDialog() {
        val dialog = RatingDialog(this)
        dialog.show()

        dialog.confirmButton.setOnClickListener {
            val comment = dialog.valueEditText.text.toString()
            val ratingClean = dialog.ratingBarClean.rating
            val ratingWork = dialog.ratingBarWork.rating
            val ratingBehave = dialog.ratingBarBehave.rating
            if (comment.isNotEmpty()) {
                viewModelRating.addReview(
                    this,
                    comment = comment,
                    clean = ratingClean.toInt(),
                    work = ratingWork.toInt(),
                    behave = ratingBehave.toInt(),
                    barberId = appointment.barberId,
                    appointId = appointment.id
                )
            }
            dialog.dismiss()
        }
    }

    private fun showCancelDialog() {
        var title = "Are you sure you want to cancel this appointment?"
        var buttonTitle = "OK"
        val status = AppointmentType.Canceled

        if (appointment.status == AppointmentType.Approved) {
            appointment.officialDate?.apply {
                //val durationInSec = 60 * 60 // One Hour
                val diffInSec = TimeUnit.MILLISECONDS.toSeconds(Date().time - this.time)
                if (isBarber) {
                    if (diffInSec >= 0) {
                        buttonTitle = "NO SHOW"
                        //status = AppointmentType.NoShow
                        title = String.format(
                            "Does the client fail to be present for the time of the scheduled service? Styler will retain a cancellation fee of %.2f%%",
                            appointment.cancellationFee
                        )
                    }
                } else {
                    //if(durationInSec >=  -diffInSec){
                    //status = AppointmentType.UntimelyCanceled
                    title = String.format(
                        "Are you sure you want to cancel this appointment? Styler will retain a cancellation fee of %.2f%% for any appointments that are canceled by client with less a 1 hour.",
                        appointment.cancellationFee
                    )
                    //}
                }
            }
        }

        if (appointment.status == AppointmentType.Approved) {
            if (isBarber) {
                buttonTitle = "NO SHOW"
                //status = AppointmentType.NoShow
                title = String.format(
                    "Does the client fail to be present for the time of the scheduled service? Styler will retain a cancellation fee of %.2f%%",
                    appointment.cancellationFee
                )
            } else {
                //status = AppointmentType.UntimelyCanceled
                title = String.format(
                    "Are you sure you want to cancel this appointment? Styler will retain a cancellation fee of %.2f%% for any appointments that are cancelled by client.",
                    appointment.cancellationFee
                )
            }
        }

        val dialog = AddUpNextOptionDialog(this)

        dialog.show()
        dialog.txtTitle.text = title
        dialog.valueEditText.visibility = View.GONE
        dialog.confirmButton.text = buttonTitle
        dialog.confirmButton.setOnClickListener {
            updateAppointment(status)
            dialog.dismiss()
        }
    }

    private fun updateAppointment(status: AppointmentType) {
        val completedDate = Date()
        val dateFormat = SimpleDateFormat(Constants.KDateFormatter.server, Locale.getDefault())
        //var date = dateFormat.format(completedDate)
        val date = Constants.convertLocalToUTC(completedDate, dateFormat)

        val message = String.format(
            "%s appointment is cancelled by %s",
            appointment.services[0].title,
            app.currentUser.firstName
        )

        val tag = Constants.API.appointment_status + "/" + appointment.id.toString()
        val receiver = appointment.barberId
        val params = HashMap<String, String>()
        params["status"] = status.name.toLowerCase()
        params["receiver"] = receiver.toString()
        params["message"] = message
        params["date"] = date
        params["fee"] = appointment.cancellationFee.toString()
        params["barber_id"] = appointment.barberId.toString()
        progressHUD.show()

        for ((key, value) in params) {
            println("$key = $value")
        }
        println(tag)

        APIHandler(
            applicationContext,
            Request.Method.POST,
            tag,
            params,
            object : APIHandler.NetworkListener {
                @SuppressLint("SetTextI18n")
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()

                    val message = result.getString("message")
                    val error = result.getBoolean("error")

                    appointment.status = status

                    val s =
                        if (status == AppointmentType.UntimelyCanceled || status == AppointmentType.NoShow) "canceled" else status.name
                    val builder = AlertDialog.Builder(this@AppointmentDetailsNewActivity)
                    builder.setMessage(message)
                    builder.setPositiveButton(R.string.ok) { _, _ ->
                        if (!error)
                            viewModel.updateAppointment(appointment)
                        startActivity(Intent(this@AppointmentDetailsNewActivity, AppointmentListActivity::class.java)
                            .putExtra("calledFrom", "ThanksPage").setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))
                        finish()
                    }
                    builder.show()
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    private fun updateUI() {

        binding.appointment = appointment
        binding.llFooter.visibility = View.VISIBLE

        val options: RequestOptions =
            RequestOptions().placeholder(R.drawable.big_placeholder)
                .error(R.drawable.big_placeholder)

        val url = if (appointment.user.image.startsWith("http"))
            appointment.user.image
        else
            FULL_IMAGE_PATH + appointment.user.image

        Glide.with(this)
            .load(url)
            .apply(options)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.imgProfile)

        if (appointment.services.isNotEmpty()) {
            binding.llService.removeAllViews()
            for (items in appointment.services) {
                val textView = TextView(this)
                textView.gravity = Gravity.CENTER
                textView.text = items.title
                textView.compoundDrawablePadding = 8
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_round_circle_hint,
                    0,
                    0,
                    0
                )
                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                if (appointment.services.size > 1) {
                    params.marginStart = 30
                }
                textView.layoutParams = params
                binding.llService.addView(textView)
            }
        }

        var fromTime = ""
        var toTime = ""
        if (!appointment.timeslot.isNullOrEmpty()) {
            val start = appointment.timeslot[0]
            val end = appointment.timeslot[appointment.timeslot.size - 1]
            fromTime = start.split("-")[0]
            toTime = end.split("-")[1]
        }
        binding.txtTime.text = "$fromTime-$toTime"

        val localDateFormat = SimpleDateFormat(Constants.KDateFormatter.local, Locale.getDefault())
        val appDateTime=localDateFormat.parse("${appointment.appointmentDate} $fromTime")

        if (Date().time<(appDateTime.time-3600000))
            binding.txtCancel.visibility=View.VISIBLE
        else
            binding.txtCancel.visibility=View.GONE

        if (appointment.rating!=null)
        binding.ratingBar.rating=appointment.rating!!

        when (appointment.status) {
            AppointmentType.Pending -> {
                binding.imgStatus.background =
                    ContextCompat.getDrawable(this, R.drawable.ic_pending_time_icon)
                binding.llReviewFooter.visibility = View.GONE
            }
            AppointmentType.Completed -> {
                binding.llFooter.visibility = View.GONE
                if (appointment.comment.isEmpty()){
                    binding.llReviewFooter.visibility = View.VISIBLE
                }else binding.llReviewFooter.visibility = View.GONE
                binding.imgStatus.background =
                    ContextCompat.getDrawable(this, R.drawable.ic_completed_icon)
            }
            AppointmentType.Deleted -> {
                binding.llFooter.visibility = View.GONE
                binding.llReviewFooter.visibility = View.GONE
                binding.imgStatus.background =
                    ContextCompat.getDrawable(this, R.drawable.ic_completed_icon)
            }
            AppointmentType.Approved -> {
                binding.imgStatus.background =
                    ContextCompat.getDrawable(this, R.drawable.ic_like_thumb)
            }
            AppointmentType.Canceled, AppointmentType.NoShow, AppointmentType.UntimelyCanceled -> {
                binding.llFooter.visibility = View.GONE
                binding.llReviewFooter.visibility = View.GONE
            }
            else ->
                binding.llFooter.visibility = View.VISIBLE
        }
    }
}