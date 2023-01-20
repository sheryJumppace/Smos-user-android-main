package com.smox.smoxuser.ui.fragment.customer

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.smox.smoxuser.HolidayRes
import com.smox.smoxuser.data.BarberRepository
import com.smox.smoxuser.databinding.FragmentAboutBinding
import com.smox.smoxuser.model.Barber
import com.smox.smoxuser.ui.adapter.HolidayAdapter
import com.smox.smoxuser.utils.shortToast
import java.util.ArrayList

class AboutFragment : Fragment() {

    private val TAG = "AboutFragment"

    lateinit var binding: FragmentAboutBinding
    var barberId = 0
    private lateinit var barber: Barber
    private lateinit var barberHoliday: List<HolidayRes.Holidays>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barberId = requireArguments().getInt("barber_id")
        barber = BarberRepository.getInstance().barber.value!!
        barberHoliday = arrayListOf()

        if (BarberRepository.getInstance().barberHolidayList.value != null) {
            barberHoliday = BarberRepository.getInstance().barberHolidayList.value!!
        }

        Log.e(TAG, "onViewCreated: ${barberHoliday.size}" )

        showWorkingHours()
        showHolidayList()

        barber.apply {
            binding.barber = this
        }
    }

    private fun showHolidayList() {
        val adapter = HolidayAdapter(requireContext())
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvHolidayList.layoutManager = layoutManager
        binding.rvHolidayList.adapter = adapter
        adapter.setData(barberHoliday as ArrayList<HolidayRes.Holidays>)
        if (barberHoliday.isNotEmpty()){
            binding.rvHolidayList.visibility=View.VISIBLE
            binding.tvNoDataFound.visibility=View.GONE
        }else{
            binding.rvHolidayList.visibility=View.GONE
            binding.tvNoDataFound.visibility=View.VISIBLE
        }
    }

    private fun showWorkingHours() {
        if (!barber.openDays.isNullOrEmpty()) {
            for (open in barber.openDays) {
                when (open.day) {
                    "Monday" -> {
                        binding.txtMon.text = open.startTime + " - " + open.endTime
                    }
                    "Tuesday" -> {
                        binding.txtTue.text = open.startTime + " - " + open.endTime
                    }
                    "Wednesday" -> {
                        binding.txtWed.text = open.startTime + " - " + open.endTime
                    }
                    "Thursday" -> {
                        binding.txtThurs.text = open.startTime + " - " + open.endTime
                    }
                    "Friday" -> {
                        binding.txtFri.text = open.startTime + " - " + open.endTime
                    }
                    "Saturday" -> {
                        binding.txtSat.text = open.startTime + " - " + open.endTime
                    }
                    "Sunday" -> {
                        binding.txtSun.text = open.startTime + " - " + open.endTime
                    }
                }
            }
        } else
            shortToast("Opening Closing time not available.")

    }
}