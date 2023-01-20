package com.smox.smoxuser.ui.fragment.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.smox.smoxuser.R

import com.smox.smoxuser.databinding.FragmentCustomerUpNextBinding
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.UpNext
import com.smox.smoxuser.ui.adapter.UpNextAdapter
import com.smox.smoxuser.ui.fragment.BaseFragment
import com.smox.smoxuser.utils.shortToast
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class CustomerUpNextFragment : BaseFragment(){
    private lateinit var txtDate:TextView
    private lateinit var txtTime:TextView
    private  var timer = Timer()

    private lateinit var adapter: UpNextAdapter
    private var upNexts:ArrayList<UpNext> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentCustomerUpNextBinding.inflate(inflater, container, false)


        adapter = UpNextAdapter(upNexts)
        binding.appointList.adapter = adapter

        val refreshLayout = binding.swipeRefreshLayout
        refreshLayout.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            refreshLayout.isRefreshing = false
            updateData()
        })
        refreshLayout.setColorSchemeResources(
            R.color.gold,
            android.R.color.holo_green_dark,
            android.R.color.holo_orange_dark,
            android.R.color.holo_blue_dark
        )

        txtDate = binding.txtDate
        txtTime = binding.txtTime
        updateTime()
        updateData()
        return binding.root
    }

    private fun updateData() {
        val dateFormat = SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
        //val date = dateFormat.format(Date())
        val date = Constants.convertLocalToUTC(Date(), dateFormat)
        val params = HashMap<String, String>()
        params["date"] = date

        progressHUD.show()
        APIHandler(
            context!!,
            Request.Method.GET,
            Constants.API.appointment_customer_up_next,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    val jsonArray = result.getJSONArray("result")
                    upNexts.clear()
                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray.getJSONObject(i)
                        val appointment = UpNext(json, context!!.applicationContext)
                        upNexts.add(appointment)
                    }
                    adapter.notifyDataSetChanged()
                }
                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    private fun updateTime(){
        val dateFormat = SimpleDateFormat(Constants.KDateFormatter.hourAM, Locale.getDefault())
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if(activity != null){
                    activity!!.runOnUiThread(Runnable {
                        txtTime.text = dateFormat.format(Date())
                    })
                }
            }
        }, 0, 1000)
    }
}
