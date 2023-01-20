package com.smox.smoxuser.ui.fragment.barber

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.android.volley.Request
import com.app.imcovery.recyclerdraghelper.OnDragStartEndListener
import com.app.imcovery.recyclerdraghelper.RecyclerDragHelper
import com.smox.smoxuser.App
import com.smox.smoxuser.R
import com.smox.smoxuser.data.AppointmentRepository
import com.smox.smoxuser.data.BarberRepository
import com.smox.smoxuser.data.ServiceRepository
import com.smox.smoxuser.databinding.FragmentUpNextBinding
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Appointment
import com.smox.smoxuser.model.Barber
import com.smox.smoxuser.model.Category
import com.smox.smoxuser.model.OpenDay
import com.smox.smoxuser.model.type.AppointmentType
import com.smox.smoxuser.model.type.UpNextStatus
import com.smox.smoxuser.model.type.UserType
import com.smox.smoxuser.ui.activity.barber.AppointmentDetailActivity
import com.smox.smoxuser.ui.activity.barber.CategoryActivity
import com.smox.smoxuser.ui.activity.barber.UpNextOptionsActivity
import com.smox.smoxuser.ui.adapter.AppointAdapter
import com.smox.smoxuser.utils.ACTION_FETCH_SUBSCRIPTION
import com.smox.smoxuser.utils.shortToast
import com.smox.smoxuser.viewmodel.AppointmentListViewModel
import com.smox.smoxuser.viewmodel.AppointmentListViewModelFactory
import com.smox.smoxuser.viewmodel.BillingViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class UpNextFragment : ContactPickerFragment(), OnDragStartEndListener {
    private val UPNEXT_OPTION_REQUEST = 100
    private val SERVICE_SELECT_REQUEST = 200
    private var isStartHours = false
    private var isClosedToday = false
    private lateinit var viewModel: AppointmentListViewModel
    private lateinit var txtDate: TextView
    private lateinit var txtTime: TextView
    private lateinit var txtWaitList: TextView
    private lateinit var txtUpNext: TextView
    private lateinit var txtLastClient: TextView
    private lateinit var txtCountTimer: TextView
    private lateinit var barber: Barber
    private var timer = Timer()
    private var countTime: Long = 0
    private var canShowService: Boolean = true
    private var dragFromPosition = 0
    private var dragToPosition = 0
    private var countUpdateTime: Long = 0
    private var upNextStatus = ""
    private var upNexts = ArrayList<Appointment>()
    private lateinit var openTime: ArrayList<OpenDay>
    private var approved = ArrayList<Appointment>()
    private var approvedTemp = ArrayList<Appointment>()
    private var isCheckCompleted = false
    private lateinit var billingViewModel: BillingViewModel
    private lateinit var fetchSubscriptionReceiver: BroadcastReceiver
    private lateinit var mActivity: Activity
    private var isRequiredBindData: Boolean = true
    private var adapter = AppointAdapter()
    var dragHelper: RecyclerDragHelper? = null
    var items: ArrayList<Category> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mActivity = requireActivity()
        val binding = FragmentUpNextBinding.inflate(inflater, container, false)
        barber = if (arguments != null) {
            val barberID = arguments!!.getInt("barber_id")
            canShowService = arguments!!.getBoolean("canShowService")
            BarberRepository.getInstance().getBarber(barberID) ?: app.currentUser
        } else {
            canShowService = true
            app.currentUser
        }

        fetchSubscriptionReceiver = FetchSubscriptionReceiver()

        val factory = AppointmentListViewModelFactory(AppointmentRepository.getInstance())
        viewModel = ViewModelProvider(this, factory).get(AppointmentListViewModel::class.java)

        val refreshLayout = binding.swipeRefreshLayout
        refreshLayout.setOnRefreshListener {
            refreshLayout.isRefreshing = false
            updateData()
        }
        refreshLayout.setColorSchemeResources(
            R.color.gold,
            android.R.color.holo_green_dark,
            android.R.color.holo_orange_dark,
            android.R.color.holo_blue_dark
        )

        txtDate = binding.txtDate
        txtTime = binding.txtTime
        txtWaitList = binding.txtWaitList
        txtUpNext = binding.txtUpNext
        txtLastClient = binding.txtLastClient
        txtCountTimer = binding.txtCountTimer

        updateTime()
        //val adapter = AppointAdapter()

        if (barber.accountType == UserType.Barber) {
            binding.btnAdd.setOnClickListener {
                if (sessionManager.isSubscribed) {
                    pickFromContacts()
                } else {
                    shortToast(resources.getString(R.string.err_subscribe))
                }
            }

            binding.txtCountTimer.setOnClickListener {
                val intent = Intent(activity, UpNextOptionsActivity::class.java)
                startActivityForResult(intent, UPNEXT_OPTION_REQUEST)
                activity!!.overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
            }

            binding.txtUpNext.setOnClickListener {
                if (sessionManager.isSubscribed) {
                    val intent = Intent(activity, CategoryActivity::class.java)
                    val user = app.currentUser
                    intent.putExtra("barber_id", user.id)
                    intent.putExtra("category_item", items)
                    startActivityForResult(intent, SERVICE_SELECT_REQUEST)
                    activity?.overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
                } else {
                    shortToast(resources.getString(R.string.err_subscribe))
                }

            }
            adapter.isEditable = true
        } else {
            binding.btnAdd.visibility = View.GONE
            binding.imgUpnext.visibility = View.GONE
        }

        adapter.setShowService(canShowService)
        adapter.setItemClickListener(object : AppointAdapter.ItemClickListener {
            override fun onItemClick(view: View, appointmentId: Int) {
                val appointment =
                    AppointmentRepository.getInstance().getAppointmentById(appointmentId)
                if (view.id == R.id.btnDelete && appointment != null) {
                    showDeleteConfirmDialog(appointment)
                } else {
                    if (barber.accountType == UserType.Barber || appointment?.customerId == barber.id) {
                        val intent = Intent(activity, AppointmentDetailActivity::class.java)
                        intent.putExtra("appoint_id", appointmentId)
                        startActivity(intent)
                        activity!!.overridePendingTransition(
                            R.anim.activity_enter,
                            R.anim.activity_exit
                        )
                    }
                }
            }
        })
        binding.appointList.adapter = adapter
        subscribeUi(adapter)

        dragHelper = RecyclerDragHelper(upNexts as ArrayList<Appointment>, adapter)
        dragHelper!!.onDragStartEndListener = this@UpNextFragment
        val itemTouchHelper = ItemTouchHelper(dragHelper!!)
        itemTouchHelper.attachToRecyclerView(binding.appointList)

        doRequestForCategoryList()
        return binding.root
    }

    private fun doRequestForCategoryList() {
        progressHUD.show()
        val params = HashMap<String, String>()
        APIHandler(
            activity!!,
            Request.Method.GET,
            Constants.API.get_category + "/" + app.currentUser.id,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()

                    items.clear()
                    if (result.has("result")) {
                        val jsonArray = result.getJSONArray("result")

                        for (i in 0 until jsonArray.length()) {
                            val json = jsonArray.getJSONObject(i)
                            val category = Category(json)
                            items.add(category)
                        }

                    }
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    override fun onResume() {
        super.onResume()
        activity?.apply {
            (this as AppCompatActivity).supportActionBar?.setTitle(R.string.upnext)
        }
        App.instance.checkSubscription = true
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(
            fetchSubscriptionReceiver,
            IntentFilter(ACTION_FETCH_SUBSCRIPTION)
        )
        updateData()
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(fetchSubscriptionReceiver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPNEXT_OPTION_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                data?.apply {
                    val status = getStringExtra("status")
                    val s: String
                    s = when (status) {
                        UpNextStatus.Automatically.name -> ""
                        UpNextStatus.Reset.name -> ""
                        else -> {
                            status!!
                        }
                    }
                    updateCountTime(s)
                }
            }
        } else if (requestCode == SERVICE_SELECT_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                val services = ServiceRepository.getInstance(barber.id).getSelectedServices()
                var duration = 0
                services.forEach {
                    duration += it.duration
                }
                val ids = services.map { it.id.toString() }.joinToString(",") { it }
                viewModel.createAppointmentWithWalkIn(activity!!, ids, duration)
            }
        }
    }

    private fun subscribeUi(adapter: AppointAdapter) {
        val dateFormat = SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
        //viewModel.fetchList(activity!!, dateFormat.format(Date()), barberId = barber.id)
        isRequiredBindData = true
        initSubscription()
        viewModel.fetchList(
            activity!!,
            Constants.convertLocalToUTC(Date(), dateFormat),
            barberId = barber.id
        )
        viewModel.appointments.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { appointments ->
                if (appointments != null) {
                    adapter.submitList(appointments)
                    viewModel.staus.value?.apply {
                        upNextStatus = this
                    }
                    upNexts.clear()
                    upNexts.addAll(appointments)
                    approvedTemp =
                        upNexts.filter { it.status == AppointmentType.Approved } as ArrayList<Appointment>
                    updateUI()
                    isCheckCompleted = false
                }
            })
    }

    fun updateData() {
        isRequiredBindData = false
        val dateFormat = SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
        val date = dateFormat.format(Date())
        initSubscription()
        with(viewModel) {
            //fetchList(activity!!, date, barberId = barber.id)
            fetchList(
                activity!!,
                Constants.convertLocalToUTC(Date(), dateFormat),
                barberId = barber.id
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        approved =
            upNexts.filter { it.status == AppointmentType.Approved } as ArrayList<Appointment>
        val completed = upNexts.filter { it.status == AppointmentType.Completed }

        val dateFormat = SimpleDateFormat(Constants.KDateFormatter.defaultDate, Locale.getDefault())
        txtDate.text = dateFormat.format(Date())

        if (!isStartHours || isClosedToday) {
            txtUpNext.text = "N/A"
        } else if (upNextStatus.isBlank()) {
            val a = getUpNext()
            txtUpNext.text = if (a == null) "N/A" else a.username.capitalize()
        } else {
            txtUpNext.text = upNextStatus
        }

        val dd = SimpleDateFormat(Constants.KDateFormatter.hourAM, Locale.getDefault())

        /*txtLastClient.text = if(completed.isEmpty()) "N/A"
        else {
            val cd = completed[completed.size - 1].completedDate
            if(cd != null){
                dd.format(cd)
            }else{
                "N/A"
            }

        }*/

//        txtLastClient.text = if (!isStartHours || completed.isEmpty()) "N/A"
//        else {

        var lastClientName = "N/A";
        if (approved.size > 0) {
            lastClientName = approved[approved.size - 1].username.capitalize()
        }
        if (!TextUtils.isEmpty(lastClientName)) {
            txtLastClient.text = lastClientName
        } else {
            txtLastClient.text = "N/A"
        }
//        }

        updateTime()
        calcStartCountTime()
        updateWaitTime()
    }

    private fun updateTime() {
        try {

         val dateFormatNow = SimpleDateFormat(
            Constants.KDateFormatter.local_full,
            Locale.getDefault()
        )


        if (App.instance.currentUser.accountType == UserType.Customer) {
            openTime =  barber.openDays
        } else if(!sessionManager.userDataOpenDays.isNullOrEmpty()){
            openTime = barber.getOpenDays(JSONObject(sessionManager.userDataOpenDays))
        }
        Log.e(
            "List size :-",
            "open time size is:- ${openTime.size} , \n User data open days:- ${
                openTime.joinToString(
                    separator = "\n"
                )
            }"
        )

        val calendar1 = Calendar.getInstance()
        val dayOfWeek = calendar1[Calendar.DAY_OF_WEEK]
        //Log.e("Day :-", "Day of week is:- $dayOfWeek")

        // Set the calendar to sunday of the current week
        val displayFormat = SimpleDateFormat("HH:mm")
        val parseFormat = SimpleDateFormat("hh:mm a")
        val date = parseFormat.parse(openTime.get(dayOfWeek - 1).startTime)
        val format = displayFormat.format(date)

        val split = format.split(":")

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, split.get(0).toInt())
        calendar.set(Calendar.MINUTE, split.get(1).toInt())
        calendar.set(Calendar.SECOND, 0)
        //Log.e("Day :-", "Current week = " + dateFormatNow.format(calendar.time))
        try {
            isClosedToday = openTime.get(dayOfWeek - 1).isClosed
            if (isClosedToday) txtUpNext.text = "N/A"
        } catch (e: Exception) {
            e.printStackTrace()
        }

        timer.scheduleAtFixedRate(object : TimerTask() {
            @SuppressLint("SetTextI18n")
            override fun run() {
                if (activity != null) {
                    activity!!.runOnUiThread {
                        val dateFormat = SimpleDateFormat(
                            Constants.KDateFormatter.hourAM,
                            Locale.getDefault()
                        )
                        txtTime.text = dateFormat.format(Date())

                        val date = Date()
                        if (date >= calendar.time) {
                            isStartHours = true
                            //Log.e("Is Time Condition :-", "true")
                            if (!upNextStatus.equals(UpNextStatus.Paused.name)) {
                                calcStartCountTime()
                                if (!countTime.equals(0L) && upNextStatus.isBlank()) {
                                    updateWaitTime(countTime)
                                    countTime--
                                } else {
                                    updateWaitTime()
                                }
                            } else {
                                updateWaitTime(countTime)
                            }
                            isStartCount()
                            //Log.e("Countdown", " Value is:- $countTime")
                        } else {
                            isStartHours = false
                            //Log.e("Is Time Condition :-", "false")
                        }
                    }
                }
            }
        }, 0, 1000)
        }catch (ex:Exception){
                Log.e("Is Time Condition :-", ex.message.toString())
        }

    }

    private fun isStartCount(): Boolean {
        for (a in approved) {
            if (a.customerId == app.currentUser.id) break

            a.officialDate?.apply {
                val durationInSec = a.duration * 60
                val diffInSec = TimeUnit.MILLISECONDS.toSeconds(Date().time - this.time)

                //Log.e("isStartCount ", "durationInSec:- $durationInSec, diffInSec:- $diffInSec")

                if (durationInSec >= diffInSec) {
                    if (diffInSec > 0) {
                        return true
                    }
                } else {
                    if (barber.accountType == UserType.Barber) {
                        if (!isCheckCompleted) {
                            isCheckCompleted = true
                            a.status = AppointmentType.Completed

                            if (a != null && a.services != null && a.services.size > 0)
                                viewModel.completeAppointment(activity!!, a)
//                            val dialog = AppointmentCompleteDialog(context!!, a)
//                            dialog.show()
//                            dialog.setItemClickListener(object :AppointmentCompleteDialog.ItemClickListener{
//                                override fun updateAppointment(type: AppointmentType) {
//                                    a.status = type
//                                    viewModel.completeAppointment(activity!!, a)
//                                }
//                            })
                        }

                    } else {
                        updateUI()
                    }
                }
            }
        }
        return false
    }

    private fun updateWaitTime(countDown: Long = 0): Unit {
        val formatter = SimpleDateFormat(Constants.KDateFormatter.second, Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("GMT")
        txtCountTimer.text = formatter.format(Date(countDown * 1000))
        Log.d("Timer text:- ", txtCountTimer.text.toString())
    }

    private fun calcStartCountTime() {
        var nWait = 0 //  Number of the waiting list
        var isOverlay = false // If service is processing now,  true else false
        var t: Long = 0
        if (!isStartHours) {
            updateWaitTime()
        } else
            for (a in approved) {
                if (a.customerId == app.currentUser.id) break
                a.officialDate?.apply {
                    if (this.time < (Date().time + 3600000)) {
                        val durationInSec = a.duration * 60
                        val diffInSec = TimeUnit.MILLISECONDS.toSeconds(Date().time - this.time)
                        if (durationInSec > diffInSec) {
                            t += durationInSec
                            if (diffInSec > 0 && !isOverlay) {
                                isOverlay = true
                                t -= diffInSec
                            }
                            nWait++
                        }
                    }
                }
            }
        // 3  line changed by kavita
//        countUpdateTime = t
//        countTime = t
//        updateWaitTime()
        // condition by manish
        if (countUpdateTime == t) {
            countTime = 0
            updateWaitTime()
        } else {
            countUpdateTime = t
            countTime = t
        }
        if (upNextStatus.equals(UpNextStatus.Paused.name)) {
            if (isStartHours && approved.size > 0 && Date().time > approved[0].officialDate!!.time) {
                countTime = t
                updateWaitTime(t)
            }
        }

        txtWaitList.text =
            if (!isStartHours) "N/A"
            else if (nWait == 0) "N/A" else (nWait).toString().padStart(2, '0')
    }

    private fun getUpNext(): Appointment? {
        for (a in approved) {
            if (a.officialDate != null) {
                if (Date().time < a.officialDate!!.time) {
                    return a
                }
            }
        }
        return null
    }

    private fun updateCountTime(status: String) {
        val params = HashMap<String, String>()
        params["status"] = status

        val dateFormat = SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
        //val date = dateFormat.format(Date())
        val date = Constants.convertLocalToUTC(Date(), dateFormat)
        params["date"] = date

        progressHUD.show()

        APIHandler(
            context!!,
            Request.Method.PUT,
            Constants.API.upnext_time,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    upNextStatus = status
                    updateUI()
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    private fun showDeleteConfirmDialog(appointment: Appointment) {
        val builder = AlertDialog.Builder(activity!!)
        builder.setTitle("Delete Appointment")
        builder.setMessage("Are you sure you want to delete the selected appointment?")
        builder.setPositiveButton("Delete") { _, _ ->
            appointment.status = AppointmentType.Deleted
            viewModel.completeAppointment(activity!!, appointment)
        }
        builder.setNegativeButton("Cancel") { _, _ ->

        }
        builder.show()
    }

    private fun initSubscription() {
        if (sessionManager.isSubscribed) {
            cancelSubscription("false", app.currentUser.id.toString());
        } else {
            cancelSubscription("true", app.currentUser.id.toString());
        }
        //progressHUD.show()
        // billingViewModel = ViewModelProviders.of(this).get(BillingViewModel::class.java)
    }

    private inner class FetchSubscriptionReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent

            /*val isSusbcribeAvail = intent.getBooleanExtra("isSusbcribeAvail", false)
            if(isSusbcribeAvail){
                val orderId = intent.getStringExtra("orderId")
                cancelSubscription("false", app.currentUser.id.toString());
            } else {
                *//*progressHUD.dismiss()
                if(userID.isNotEmpty()){
                    cancelSubscription("true",userID);
                } else {
                    openNextScreen()
                }*//*
                cancelSubscription("true", app.currentUser.id.toString());
            }*/

            //progressHUD.dismiss()
        }
    }

    fun cancelSubscription(isCancel: String, userID: String) {
        val params = HashMap<String, String>()
        params["userId"] = userID
        //params["transactionId"] = transactionId
        params["isCancel"] = isCancel
        //params["endSubscriptionDate"] = ""
        progressHUD.show()

        APIHandler(
            mActivity,
            Request.Method.POST,
            Constants.API.cancelSubscription,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    val error = result.getBoolean("error");
                    if (!error) {
                        val subsCancelResult = result.getBoolean("result");
                        // sessionManager.isSubscribed = subsCancelResult
                    }
                    try {
                        val dateFormat =
                            SimpleDateFormat(
                                Constants.KDateFormatter.serverDay,
                                Locale.getDefault()
                            )
                        if (isRequiredBindData) {
                            viewModel.fetchList(
                                activity!!,
                                Constants.convertLocalToUTC(Date(), dateFormat),
                                barberId = barber.id
                            )
                            viewModel.appointments.observe(
                                viewLifecycleOwner,
                                androidx.lifecycle.Observer { appointments ->

                                    if (appointments != null) {
                                        adapter.submitList(appointments)
                                        viewModel.staus.value?.apply {
                                            upNextStatus = this
                                        }
                                        upNexts.clear()
                                        upNexts.addAll(appointments)
                                        approvedTemp =
                                            upNexts.filter { it.status == AppointmentType.Approved } as ArrayList<Appointment>
                                        updateUI()
                                        isCheckCompleted = false
                                    }
                                })
                        } else {
                            with(viewModel) {
                                fetchList(
                                    activity!!,
                                    Constants.convertLocalToUTC(Date(), dateFormat),
                                    barberId = barber.id
                                )
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    override fun onDragStartListener(fromPosition: Int) {
        Log.e("Activity=>", "From Pos: $fromPosition")
        dragFromPosition = fromPosition
    }

    override fun onDragEndListener(toPosition: Int) {
        Log.e("Activity=> ", "To Pos: $toPosition")
        dragToPosition = toPosition
        /*val listData = adapter.getListData()
        if (listData.size > 0)
            for (x in 0 until listData.size) {
                val a = listData.get(x)
                if (a.status == AppointmentType.Approved) {
                    Log.e("List index:- $x and ${a.id}", a.toString())
                }
            }*/
        changeItemPosition()
    }

    private fun changeItemPosition() {
        val item: Appointment = approvedTemp.removeAt(dragFromPosition)
        approvedTemp.add(dragToPosition, item)
        if (approvedTemp.size > 0) {
            val jsonArray = JSONArray()
            for (x in 0 until approvedTemp.size) {
                val a = approvedTemp.get(x)
                if (a.status == AppointmentType.Approved) {
                    val jsonObject = JSONObject()
                    Log.e("List index ", "Item Position:- $x and ${a.id} ${a.toString()}")
                    jsonObject.put("id", a.id)
                    jsonObject.put("position_id", x + 1)
                    jsonArray.put(x, jsonObject)
                }
            }
            val jsonObject = JSONObject()
            jsonObject.put("appointment_data", jsonArray)
            Log.e("Data of Json Object", jsonObject.toString())
            viewModel.sendReorderAppointment(activity!!, jsonObject)
        }
    }

}
