package com.smox.smoxuser.ui.activity.barber

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.android.volley.Request
import com.smox.smoxuser.R
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Barber
import com.smox.smoxuser.model.OpenDay
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.adapter.OpenHourAdapter
import com.smox.smoxuser.utils.shortToast
import kotlinx.android.synthetic.main.activity_open_hours.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class OpenHoursActivity : BaseActivity(), TimePickerDialog.OnTimeSetListener {
    private lateinit var adapter: OpenHourAdapter
    private var hours = ArrayList<OpenDay>()
    private var selectedIndex:Int = -1
    private var isStartTime:Boolean = false
    private lateinit var barber:Barber
    private lateinit var itemView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_hours)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@OpenHoursActivity, Constants.backButton))
        }

        barber = app.currentUser

        adapter = OpenHourAdapter()
        open_hour_list.adapter = adapter
        adapter.setItemClickListener(object : OpenHourAdapter.ItemClickListener{
            override fun onDayChange(view: View, position: Int, isOpen: Boolean) {
                hours[position].isClosed = !isOpen
            }

            override fun onItemClick(view: View, position: Int, isStart: Boolean) {
                itemView = view
                itemView.isEnabled = false
                selectedIndex = position
                isStartTime = isStart
                openTimePicker()
            }
        })

        getHours()
    }

    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_save -> {
                updateOpenHours()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        itemView.isEnabled = true
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, 0)
        cal.set(Calendar.MONTH, 0)
        cal.set(Calendar.DAY_OF_MONTH, 0)
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val formatter =  SimpleDateFormat(Constants.KDateFormatter.hourAM, Locale.getDefault())
        val time = formatter.format(cal.time)

        val openDay = hours[selectedIndex]
        if(isStartTime){
            openDay.startTime = time
        }else{
            openDay.endTime = time
        }

        adapter.submitList(hours)
        adapter.notifyDataSetChanged()
    }

    private fun openTimePicker() {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR)
        val minute = c.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(this@OpenHoursActivity,R.style.themeOnverlay_timePicker, this@OpenHoursActivity, hour, minute, false)
        timePickerDialog.show()
        timePickerDialog.setOnDismissListener(){
            itemView.isEnabled = true
        }
    }

    private fun getHours(){

        val params = HashMap<String, String>()
        progressHUD.show()
        APIHandler(
            applicationContext,
            Request.Method.GET,
            Constants.API.hours + "/" + barber.id.toString(),
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()

                    if(result.has("result")){
                        val data = result.getJSONObject("result")
                        Log.e("Open days Data:- ", data.toString())
                        hours = barber.getOpenDays(data)

                        sessionManager.userDataOpenDays = data.toString()
                        Log.e("New Open days data:- ", sessionManager.userDataOpenDays!!)
                        adapter.submitList(hours)
                    }
                }
                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    private fun updateOpenHours(){
        val params = HashMap<String, String>()
        val jsonData = JSONObject(sessionManager.userDataOpenDays!!)
        hours.forEach{
            params[it.getFieldNameOfDay()] = it.getHours().toUpperCase()
            it.updateOpenDays(it.getFieldNameOfDay(), it.getHours().toUpperCase(), jsonData)
        }

        sessionManager.userDataOpenDays = jsonData.toString()
        Log.e("New Open days data:- ", sessionManager.userDataOpenDays!!)

        for ((key, value) in params) {
            println("$key = $value")
        }

        progressHUD.show()
        APIHandler(
            applicationContext,
            Request.Method.POST,
            Constants.API.hours,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    barber.openDays = hours
                    Log.e("Open days Data:- ", result.toString())
                    finish()
                }
                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }
}
