package com.smox.smoxuser.ui.activity.barber

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.smox.smoxuser.R
import com.smox.smoxuser.data.AppointmentRepository
import com.smox.smoxuser.databinding.ActivityAppointmentDetailBinding
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Appointment
import com.smox.smoxuser.model.Category
import com.smox.smoxuser.model.Service
import com.smox.smoxuser.model.type.AppointmentType
import com.smox.smoxuser.model.type.UserType
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.activity.customer.CheckOutActivity
import com.smox.smoxuser.ui.adapter.CategorySelectorAdapter
import com.smox.smoxuser.ui.adapter.ServiceAdapter
import com.smox.smoxuser.ui.dialog.AddUpNextOptionDialog
import com.smox.smoxuser.utils.shortToast
import com.smox.smoxuser.viewmodel.AppointmentViewModel
import com.smox.smoxuser.viewmodel.AppointmentViewModelFactory
import kotlinx.android.synthetic.main.activity_appointment_detail.*
import kotlinx.android.synthetic.main.activity_booking_appointment.*
import kotlinx.android.synthetic.main.activity_booking_preference.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class AppointmentDetailActivity : BaseActivity(),
    CategorySelectorAdapter.CategorySelectActions {

    private lateinit var viewModel: AppointmentViewModel
    private lateinit var adapter: ServiceAdapter
    private var isBarber = false
    private lateinit var appointment: Appointment
    var items: ArrayList<Category> = ArrayList()
    private var itemsSelected: ArrayList<Category> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityAppointmentDetailBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_appointment_detail)
        setSupportActionBar(binding.toolbar)

        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@AppointmentDetailActivity, Constants.backButton))
        }

        isBarber = app.currentUser.accountType == UserType.Barber

        adapter = ServiceAdapter(true)
        binding.serviceList.adapter = adapter

        val id = intent.getIntExtra("appoint_id", 0)

        val factory = AppointmentViewModelFactory(AppointmentRepository.getInstance(), id)
        viewModel = ViewModelProvider(this, factory).get(AppointmentViewModel::class.java)
        viewModel.fetchList(this)
        viewModel.appointment.observe(this, Observer {
            if (it != null) {
                appointment = it
                updateUI(binding)
            }
        })
        viewModel.isSuccessToSentPaymentRequest.observe(this, Observer {
            if (it != null && it) {
//                appointment.isPaid = true
//                binding.viewFooter.visibility = View.GONE
            }
        })

        binding.clickListener = View.OnClickListener {
            when (it.id) {
                R.id.txtContact -> {
                    if (appointment.user.phone.isEmpty()) {
                        contactWithEmail(appointment.user.email)
                    } else {
                        contactWithPhone(appointment.user.phone)
                    }
                }
                R.id.txtAddress -> openDirection()
                R.id.btnStatus -> {
                    if (isBarber && appointment.status == AppointmentType.Pending) {
                        updateAppointment(AppointmentType.Approved)
                    }
                }
                R.id.btnEdit -> {
                    val intent = Intent(
                        this@AppointmentDetailActivity,
                        BookingAppointmentActivity::class.java
                    )
                    val user = if (isBarber) app.currentUser else appointment.user
                    intent.putExtra("barber", user)
                    intent.putExtra("appointment", appointment)
                    intent.putExtra("CatList", items)
                    startActivity(intent)
                    overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
                }
                R.id.btnComplete -> {
                    if (isBarber) {
                        updateAppointment(AppointmentType.Completed)
                    } else {
                        val intent =
                            Intent(this@AppointmentDetailActivity, CheckOutActivity::class.java)
                        intent.putExtra("appointment", appointment)
                        startActivity(intent)
                        overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
                    }
                }
                R.id.btnCancel -> {
                    if (appointment.status == AppointmentType.Completed && !appointment.isPaid) {
                        if (isBarber) {
                            viewModel.sendPaymentRequest(
                                this@AppointmentDetailActivity,
                                appointment
                            )
                        } else {
                            val intent =
                                Intent(this@AppointmentDetailActivity, CheckOutActivity::class.java)
                            intent.putExtra("appointment", appointment)
                            startActivity(intent)
                            overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
                        }
                    } else {
                        showCancelDialog()
                    }
                }
            }
        }
    }

    private fun doRequestForCategoryList() {
        //progressHUD.show()
        val params = HashMap<String, String>()
        APIHandler(
            this,
            Request.Method.GET,
            Constants.API.get_category + "/" + app.currentUser.id,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    //progressHUD.dismiss()

                    items.clear()
                    if (result.has("result")) {
                        val jsonArray = result.getJSONArray("result")

                        for (i in 0 until jsonArray.length()) {
                            val json = jsonArray.getJSONObject(i)
                            val category = Category(json)
                            items.add(category)
                        }

                        if (items != null && !items.isEmpty() && appointment.services.size > 0) {
                            itemsSelected.clear()
                            for (category_item in items) {
                                for (service in appointment.services) {
                                    if (service.category_id == category_item.cat_id) {
                                        itemsSelected.add(category_item)
                                        break
                                    }
                                }
                            }
                        }
                        updateServiceUI(0)
                    }
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    updateServiceUI(0)
                    shortToast(error)

                }
            })
    }

    private fun updateUI(binding: ActivityAppointmentDetailBinding) {

        val btnParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        if (!isBarber && appointment.isPaid) {
            btnComplete.visibility = View.GONE
            btnParams.weight = 2f
            btnEdit.layoutParams = btnParams
        }

        updateServiceUI(0)

        appointment.user.phone.isEmpty()
        binding.appointment = appointment

        if (isBarber) {
            binding.txtLocation.visibility = View.GONE
            binding.txtAddress.visibility = View.GONE
        }

        if (appointment.status == AppointmentType.Completed) {
            if (appointment.isPaid) {
                binding.viewFooter.visibility = View.GONE
            } else {
                binding.viewButtonGroup.visibility = View.GONE
                binding.btnCancel.setBackgroundResource(R.drawable.bg_smox_button)
                binding.btnCancel.setTextColor(Color.WHITE)
                val title = if (isBarber) {
                    R.string.send_payment_request
                } else {
                    R.string.pay_now
                }
                binding.btnCancel.setText(title)
            }
        } else if (appointment.status == AppointmentType.Canceled || appointment.status == AppointmentType.NoShow || appointment.status == AppointmentType.UntimelyCanceled) {
            binding.viewFooter.visibility = View.GONE
        } else {
            binding.viewFooter.visibility = View.VISIBLE
            binding.btnCancel.setText(R.string.cancel)
        }
        binding.toolbarLayout.title = appointment.user.name
        doRequestForCategoryList()
    }

    private fun contactWithEmail(email: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:$email")
        try {
            startActivity(emailIntent)
        } catch (e: ActivityNotFoundException) {
            //TODO: Handle case where no email app is available
        }
    }

    private fun contactWithPhone(phone: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
        startActivity(intent)
    }

    private fun openDirection() {
        val url =
            "http://maps.google.com/maps?saddr=" + appointment.user.latitude.toString() + "&daddr=" + appointment.user.longitude.toString()
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url)
        )
        startActivity(intent)
    }

    private fun updateAppointment(status: AppointmentType) {
        val completedDate = Date()
        val dateFormat = SimpleDateFormat(Constants.KDateFormatter.server, Locale.getDefault())
        //var date = dateFormat.format(completedDate)
        var date = Constants.convertLocalToUTC(completedDate, dateFormat)

        val message = when (status) {
            AppointmentType.Completed -> String.format(
                "%s appointment with %s has been completed",
                appointment.services[0].title,
                app.currentUser.firstName
            )
            AppointmentType.Approved -> {
                dateFormat.applyPattern(Constants.KDateFormatter.hourDetail)
                val time = dateFormat.format(appointment.preferredDate)

                dateFormat.applyPattern(Constants.KDateFormatter.server)
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                date = dateFormat.format(appointment.preferredDate)

                String.format(
                    "Your %s appointment with %s is scheduled for %s",
                    appointment.services[0].title,
                    app.currentUser.firstName,
                    appointment.getDaySms()
                )
            }
            AppointmentType.Canceled -> String.format(
                "%s appointment is cancelled by %s",
                appointment.services[0].title,
                app.currentUser.firstName
            )
            else -> ""
        }


        val tag = Constants.API.appointment_status + "/" + appointment.id.toString()
        val receiver = if (isBarber) appointment.customerId else appointment.barberId
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

                    if (status == AppointmentType.Approved) {
                        btnStatus.text = "APPROVED"
                        appointment.officialDate = appointment.preferredDate
                        appointment.strOfficialDate = appointment.strPreferredDate
                    } else if (status == AppointmentType.Completed) {
                        appointment.completedDate = completedDate
                    }

                    appointment.status = status

                    val s =
                        if (status == AppointmentType.UntimelyCanceled || status == AppointmentType.NoShow) "canceled" else status.name
                    val builder = AlertDialog.Builder(this@AppointmentDetailActivity)
                    builder.setMessage("Appointment has been $s")
                    builder.setPositiveButton(R.string.ok) { _, _ ->
                        viewModel.updateAppointment(appointment)
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

    private fun showCancelDialog() {
        /*showAlertDialog("Warning", "Are you sure you want to cancel this appointment?", DialogInterface.OnClickListener{ _, _ ->
            updateAppointment(AppointmentType.Canceled)
        }, getString(R.string.ok), null, getString(R.string.cancel))*/

        var title = "Are you sure you want to cancel this appointment?"
        var buttonTitle = "OK"
        var status = AppointmentType.Canceled

        /*appointment.officialDate?.apply {
            val durationInSec = 60 * 60 // One Hour
            val diffInSec =  TimeUnit.MILLISECONDS.toSeconds(Date().time - this.time)
            if(isBarber){
                if(diffInSec >= 0){
                    buttonTitle = "NO SHOW"
                    status = AppointmentType.NoShow
                    title = String.format("Does the client fail to be present for the time of the scheduled service? Styler will retain a cancellation fee of %.2f%%", appointment.cancellationFee)
                }
            }else{
                if(durationInSec >=  -diffInSec){
                    status = AppointmentType.UntimelyCanceled
                    title = String.format("Are you sure you want to cancel this appointment? Styler will retain a cancellation fee of %.2f%% for any appointments that are canceled by client with less a 1 hour.", appointment.cancellationFee)
                }
            }
        }*/
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

    private fun updateServiceUI(position: Int) {
        if (itemsSelected.isNotEmpty() && itemsSelected.size > 0) {
            setAdapter(position)
            tvNotFoundServices.visibility = View.GONE
        }
        else tvNotFoundServices.visibility = View.VISIBLE
    }

    private fun setListData(position: Int) {
        var listService: ArrayList<Service> = ArrayList()
        for (service in appointment.services) {
            if (service.category_id == itemsSelected.get(position).cat_id) {
                listService.add(service)
            }
        }
        adapter.submitList(listService)
        adapter.notifyDataSetChanged()
    }

    override fun onItemClick(pos: Int) {
        setListData(pos)
    }

    private fun setAdapter(position: Int) {
        if (itemsSelected.size > 0) {
            var categoryAdapter =
                CategorySelectorAdapter(this, this@AppointmentDetailActivity, false)
            category_selected_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            category_selected_list.setHasFixedSize(true)
            category_selected_list.adapter = categoryAdapter

            categoryAdapter.doRefresh(itemsSelected)
            setListData(position)
        }
    }
}
