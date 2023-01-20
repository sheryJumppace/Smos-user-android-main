package com.smox.smoxuser.ui.fragment.barber


import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.applandeo.materialcalendarview.CalendarUtils
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.google.android.material.appbar.AppBarLayout
import com.smox.smoxuser.App
import com.smox.smoxuser.R
import com.smox.smoxuser.data.AppointmentRepository
import com.smox.smoxuser.databinding.FragmentCalendarBinding
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Event
import com.smox.smoxuser.model.type.UserType
import com.smox.smoxuser.ui.activity.barber.AppointmentDetailActivity
import com.smox.smoxuser.ui.adapter.AppointAdapter
import com.smox.smoxuser.ui.adapter.EventPostAdapter
import com.smox.smoxuser.ui.dialog.EventDialog
import com.smox.smoxuser.utils.ACTION_FETCH_SUBSCRIPTION
import com.smox.smoxuser.utils.shortToast
import com.smox.smoxuser.viewmodel.AppointmentListViewModel
import com.smox.smoxuser.viewmodel.AppointmentListViewModelFactory
import com.smox.smoxuser.viewmodel.BillingViewModel
import kotlinx.android.synthetic.main.fragment_calendar.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class CalendarFragment : ContactPickerFragment(), EventPostAdapter.EventActions {

    enum class State {
        EXPANDED,
        COLLAPSED,
        IDLE
    }

    var items: ArrayList<Event> = ArrayList()
    private lateinit var viewModel: AppointmentListViewModel
    public lateinit var widget: CalendarView
    private lateinit var mRecyclerView: RecyclerView
    private var firstVisibleInListView: Int = 0
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var cal_coordinateLayout: CoordinatorLayout
    private lateinit var lnrCalendar: LinearLayout
    private lateinit var calendar_app_bar: AppBarLayout
    //private lateinit var rBlurView : RealtimeBlurView
    //private lateinit var mainBlurView : BlurView
    private lateinit var mainRootView: ViewGroup

    private lateinit var billingViewModel: BillingViewModel
    private lateinit var fetchSubscriptionReceiver: BroadcastReceiver
    private lateinit var mActivity: Activity
    private var isRequiredBindData: Boolean = true
    private var adapter = AppointAdapter()
    private var strDate = ""
    private lateinit var eventPostAdapter:EventPostAdapter
    private var event:Event?= null
    private var pos = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mActivity = requireActivity()
        val binding = FragmentCalendarBinding.inflate(inflater, container, false)

        widget = binding.calendarView

        fetchSubscriptionReceiver = FetchSubscriptionReceiver()

        val factory = AppointmentListViewModelFactory(AppointmentRepository.getInstance())
        viewModel = ViewModelProvider(this, factory).get(AppointmentListViewModel::class.java)

        cal_coordinateLayout = binding.calCoordinateLayout
        lnrCalendar = binding.lnrCalendar
        calendar_app_bar = binding.calendarAppBar
        //rBlurView = binding.rBlurView
        //mainBlurView = binding.mainBlurView


//        mParent = binding.calendarContainer
        layoutManager = LinearLayoutManager(activity)
        firstVisibleInListView = layoutManager.findFirstVisibleItemPosition()

        mRecyclerView = binding.appointList
        mRecyclerView.layoutManager = layoutManager

        eventPostAdapter = EventPostAdapter(this)
        //val adapter = AppointAdapter()
        adapter.setShowService(true)
        adapter.setItemClickListener(object : AppointAdapter.ItemClickListener {
            override fun onItemClick(view: View, appointmentId: Int) {
                val intent = Intent(activity, AppointmentDetailActivity::class.java)
                intent.putExtra("appoint_id", appointmentId)
                startActivity(intent)
                activity!!.overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
            }
        })
        binding.appointList.adapter = adapter
        //val eventPostAdapter = EventPostAdapter()
        binding.eventList.adapter = eventPostAdapter
        subscribeUi(adapter, eventPostAdapter)
        initCalendar()
        binding.btnAdd.setOnClickListener {
            if (sessionManager.isSubscribed) {
                pickFromContacts(widget.firstSelectedDate.timeInMillis)
            } else {
                shortToast(resources.getString(R.string.err_subscribe))
            }
        }
        binding.btnAdd.visibility = if (app.currentUser.accountType == UserType.Barber)
            View.VISIBLE
        else
            View.GONE

        binding.btnAddEvent.setOnClickListener {

            if (sessionManager.isSubscribed) {
                btnAddEvent.isEnabled = false
                event=null
                openPostingEventView()
            } else {
                shortToast(resources.getString(R.string.err_event))
            }
        }
        binding.btnAddEvent.visibility = if (app.currentUser.accountType == UserType.Barber)
            View.VISIBLE
        else
            View.GONE

        var radius: Float = 25f
        var minBlurRadius: Float = 10f
        var step: Float = 4f

        var windowBack = requireActivity().window.decorView.background

        //var blurAlgo = SupportRe
        var dView = requireActivity().window.decorView
        var rootView = dView.findViewById<View>(android.R.id.content) as ViewGroup
        mainRootView = dView.findViewById<View>(android.R.id.content) as ViewGroup
        var windowBack1 = dView.background

        /* mainBlurView.setupWith(rootView)
             .setFrameClearDrawable(windowBack1)
             .setBlurAlgorithm(RenderScriptBlur(context))
             .setBlurRadius(20f)
             .setHasFixedTransformationMatrix(true)*/


        return binding.root
    }

    override fun onResume() {
        super.onResume()
        super.onResume()
        activity?.apply {
            (this as AppCompatActivity).supportActionBar?.setTitle(R.string.calendar)
        }
        App.instance.checkSubscription = true;
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(
            fetchSubscriptionReceiver,
            IntentFilter(ACTION_FETCH_SUBSCRIPTION)
        )
        /*if(widget.selectedDates.count() == 1){
            val formatter =  SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
            val clickedDayCalendar = widget.selectedDates[0]
            //val strDate = formatter.format(clickedDayCalendar.time)
            clickedDayCalendar.add(Calendar.DATE,1)
            strDate = Constants.convertLocalToUTC(clickedDayCalendar.time, formatter)
            val calendar = Calendar.getInstance()
            calendar.time = Date()
            //calendar.add(Calendar.DATE, -1)
            widget.setDate(calendar)
            updateData(strDate)
        }*/
        if (widget.selectedDates.count() == 1) {
            val formatter =
                SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
            val clickedDayCalendar = widget.selectedDates[0]
            val strDate = formatter.format(clickedDayCalendar.time)
            updateData(strDate)
        }
        if (widget.firstSelectedDate != null) {
            getAppointNumberOfMonth(widget.firstSelectedDate)
        }
        /*if(widget.firstSelectedDate != null){
            getAppointNumberOfMonth(widget.firstSelectedDate)
        }*/
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(fetchSubscriptionReceiver)
    }

    private fun initCalendar() {
        val formatter = SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())

        val calendar = Calendar.getInstance()
        calendar.time = Date()
        //calendar.add(Calendar.DATE, -1)
        widget.setDate(calendar)

        widget.setOnDayClickListener { eventDay ->
            /*val clickedDayCalendar = eventDay.calendar
            //val strDate = formatter.format(clickedDayCalendar.time)
            clickedDayCalendar.add(Calendar.DATE, 1)
            strDate = Constants.convertLocalToUTC(clickedDayCalendar.time, formatter)
            updateData(strDate)*/
            val clickedDayCalendar = eventDay.calendar
            val strDate = formatter.format(clickedDayCalendar.time)
            updateData(strDate)
        }
        widget.setOnPreviousPageChangeListener {
            getAppointNumberOfMonth(widget.currentPageDate)
        }
        widget.setOnForwardPageChangeListener {
            getAppointNumberOfMonth(widget.currentPageDate)
        }
        //widget.firstSelectedDate.add(Calendar.DATE,1)
        getAppointNumberOfMonth(widget.firstSelectedDate)

//        widget.setHeaderTextAppearance(R.style.CalendarHeaderStyle)
//        widget.setWeekDayTextAppearance(R.style.CalendarWeekDayStyle)
//        widget.setDateTextAppearance(R.style.CalendarDateStyle)
//        widget.selectionColor = ContextCompat.getColor(activity!!.applicationContext, R.color.SelectedColor)
//
////        widget.showOtherDates = MaterialCalendarView.SHOW_ALL
//        widget.setLeftArrow(R.drawable.ic_arrow_back)
//        widget.setRightArrow(R.drawable.ic_arrow_forward)
//
//        widget.setOnDateChangeListener(this)
//        widget.set(this)
//
//        appBarLayout.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
//            override fun onStateChanged(appBarLayout: AppBarLayout, state: State) {
//                if(state == State.COLLAPSED){
//                    isCompleted = true
//                    preStatus = state
//                    if (widget.calendarMode == CalendarMode.MONTHS) {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                            TransitionManager.beginDelayedTransition(appBarLayout)
//                        }
//                        widget.state().edit().setCalendarDisplayMode(CalendarMode.WEEKS).commit()
//
//                    }
//                }else if(state == State.EXPANDED){
//                    isCompleted = true
//                    preStatus = state
//                    if (widget.calendarMode == CalendarMode.WEEKS) {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                            TransitionManager.beginDelayedTransition(appBarLayout)
//                        }
//                        widget.state().edit().setCalendarDisplayMode(CalendarMode.MONTHS).commit()
//                    }
//                }
//            }
//        })
    }

    private fun subscribeUi(adapter: AppointAdapter, eventPostAdapter: EventPostAdapter) {
        isRequiredBindData = true
        initSubscription()
        val dateFormat = SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
        viewModel.fetchList(activity!!, dateFormat.format(Date()), app.currentUser.id)


        //viewModel.fetchList(activity!!, Constants.convertLocalToUTC(Date(), dateFormat), app.currentUser.id)
        viewModel.appointments.observe(viewLifecycleOwner, Observer { appointments ->
            if (appointments != null) adapter.submitList(appointments)
        })

        viewModel.events.observe(viewLifecycleOwner, Observer { events ->
            if (events != null) {
                items = events as ArrayList<Event>;
                eventPostAdapter.submitList(events)
                eventPostAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun updateData(date: String) {
        isRequiredBindData = false
        initSubscription()
        with(viewModel) {
            fetchList(activity!!, date, app.currentUser.id)
        }
    }

    private fun getAppointNumberOfMonth(calendar: Calendar) {
        val dateFormat = SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
        val params = HashMap<String, String>()
        params["date"] = dateFormat.format(calendar.time)
        //calendar.add(Calendar.DATE,1)
        //params["date"] = Constants.convertLocalToUTC(calendar.time, dateFormat)

        APIHandler(
            context!!,
            Request.Method.GET,
            Constants.API.appointment_by_month,
            params,
            object : APIHandler.NetworkListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResult(result: JSONObject) {

                    val jsonArray = result.getJSONArray("result")
                    val items: ArrayList<EventDay> = ArrayList()
                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray.getJSONObject(i)
                        val cal = Calendar.getInstance()
                        if (json.has("a_day")) {
                            val day = json.getInt("a_day")
                            cal.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), day)
                        }
                        var num = 1
                        if (json.has("num")) {
                            num = json.getInt("num")
                        }
                        try {
                            val drawable = CalendarUtils.getDrawableText(
                                context,
                                num.toString(),
                                Typeface.DEFAULT,
                                R.color.gold,
                                12
                            )
                            items.add(EventDay(cal, drawable))
                        } catch (e: Exception) {

                        }

                    }

                    widget.setEvents(items)
                    widget.requestLayout()
                }

                override fun onFail(error: String?) {
                    shortToast(error)
                }
            })
    }

    private fun openPostingEventView() {
        val dialog = EventDialog(requireActivity())
        dialog.setEventData(event)
        dialog.show()
        dialog.confirmButton.setOnClickListener {
            btnAddEvent.isEnabled = true
            val eventText = dialog.valueEditText.text.toString().trim()
            //val start = dialog.txtStart.text.toString()
            val start = dialog.startDate.time
            //val end = dialog.txtEnd.text.toString()
            val end = dialog.endDate.time
            val startDate = dialog.edtStartDate.text.toString()
            val endDate = dialog.edtEndDate.text.toString()
            if(eventText.isEmpty())
                shortToast(activity!!.resources.getString(R.string.add_event_detail))

            if (eventText.isNotEmpty() && startDate.isNotEmpty() && endDate.isNotEmpty()) {
                viewModel.postEvent(
                    activity!!,
                    event=eventText,
                    start = start,
                    end = end,
                    startDate = startDate,
                    endDate = endDate,
                    id  = if( event!=null )  event!!.id  else 0
                ,
                    pos
                )
                dialog.dismiss()
                //Blurry.delete(mainRootView)
            }
        }

        dialog.btnDone.setOnClickListener {
            btnAddEvent.isEnabled = true
            dialog.dismiss()
            //Blurry.delete(cal_coordinateLayout)
            //Blurry.delete(calendar_app_bar)
           // Blurry.delete(mainRootView)
        }

        val formatter = SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
        val date = formatter.format(Date())
        //dialog.txtStart.text = date
        //dialog.txtEnd.text = date
        dialog.edtStartDate.text = Editable.Factory.getInstance().newEditable(if(event!=null) event!!.getStartDate() else date)
        dialog.edtEndDate.text = Editable.Factory.getInstance().newEditable(if(event!=null) event!!.getEndDate() else date)

       // Blurry.with(context).radius(25).sampling(1).onto(mainRootView)

    }

    private fun initSubscription() {
        //progressHUD.show()
       // billingViewModel = ViewModelProviders.of(this).get(BillingViewModel::class.java)

        if (sessionManager.isSubscribed) {
            cancelSubscription("false", app.currentUser.id.toString());
        } else {
            cancelSubscription("true", app.currentUser.id.toString());
        }


    }

    private inner class FetchSubscriptionReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent

           /* val isSusbcribeAvail = intent.getBooleanExtra("isSusbcribeAvail", false)
            if (isSusbcribeAvail) {
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

       // Log.d("mn13subs2",isCancel.toString())

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
                        Log.d("mn13subscribe", result.toString())
                        //sessionManager.isSubscribed = subsCancelResult
                        // Log.d("mn13subscribe",sessionManager.isSubscribed.toString())
                    }
                    /*val dateFormat = SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
                    if(isRequiredBindData){
                        viewModel.fetchList(activity!!, Constants.convertLocalToUTC(Date(), dateFormat), app.currentUser.id)
                        viewModel.appointments.observe(viewLifecycleOwner, Observer { appointments ->
                            if (appointments != null) adapter.submitList(appointments)
                        })

                        viewModel.events.observe(viewLifecycleOwner, Observer { events->
                            if (events != null) {
                                eventPostAdapter.submitList(events)
                                eventPostAdapter.notifyDataSetChanged()}
                        })
                    } else {
                        with(viewModel) {
                            fetchList(activity!!, strDate, app.currentUser.id)
                        }
                    }*/
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    override fun onDeleteClick(pos: Int) {
        val builder = AlertDialog.Builder(mActivity)
        builder.setTitle("Warning!")
        builder.setMessage("Are you sure you want to delete this event?")
        builder.setPositiveButton("Ok") { _, _ ->
            doReuestForDelete(pos)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    override fun onEditClick(event: Event,pos: Int) {
        this.event= event
        this.pos = pos
        openPostingEventView()
    }

    private fun doReuestForDelete(pos: Int) {
        progressHUD.show()
        val params = HashMap<String, String>()
        APIHandler(
            mActivity,
            Request.Method.DELETE,
            Constants.API.delete_event + "/" + items.get(pos).id,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    shortToast(result.getString("message"))
                    if (!(result.getBoolean("error"))) {
                        items.removeAt(pos)
                        viewModel.events.value = items;
                        eventPostAdapter.submitList(items)
                        eventPostAdapter.notifyDataSetChanged()
                    }

                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }


}