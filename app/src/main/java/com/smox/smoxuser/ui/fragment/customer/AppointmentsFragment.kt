package com.smox.smoxuser.ui.fragment.customer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.tabs.TabLayout

import com.smox.smoxuser.R
import com.smox.smoxuser.data.AppointmentRepository
import com.smox.smoxuser.databinding.FragmentAppointmentsBinding
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.type.AppointmentType
import com.smox.smoxuser.ui.activity.customer.AppointmentDetailsNewActivity
import com.smox.smoxuser.ui.adapter.AppointAdapter
import com.smox.smoxuser.ui.fragment.BaseFragment
import com.smox.smoxuser.viewmodel.AppointmentListViewModel
import com.smox.smoxuser.viewmodel.AppointmentListViewModelFactory
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_appointments.*

class AppointmentsFragment : BaseFragment() {
    private lateinit var viewModel: AppointmentListViewModel
    private lateinit var adapter: AppointAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentAppointmentsBinding.inflate(inflater, container, false)

        val factory = AppointmentListViewModelFactory(AppointmentRepository.getInstance())
        viewModel = ViewModelProviders.of(this, factory).get(AppointmentListViewModel::class.java)
        adapter = AppointAdapter()
        adapter.setShowService(true)
        adapter.setItemClickListener(object : AppointAdapter.ItemClickListener {
            override fun onItemClick(view: View, appointmentId: Int) {
                //val intent = Intent(activity, AppointmentDetailActivity::class.java)
                val intent = Intent(activity, AppointmentDetailsNewActivity::class.java)
                intent.putExtra(Constants.API.APPOINT_ID, appointmentId)
                startActivity(intent)
                activity!!.overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
            }
        })

        newToolbar.findViewById<ImageView>(R.id.searchBar).visibility=View.GONE
        binding.appointList.adapter = adapter
        //viewModel.fetchAppointmentsOfCustomer(requireActivity())
        viewModel.appointments.observe(viewLifecycleOwner, Observer { appointments ->
            if (appointments != null) {
                updateListView()
            }
        })

        binding.tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                updateListView()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }

        })
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        activity?.apply {
            viewModel.fetchAppointmentsOfCustomer(this,"1")
        }
    }

    private fun updateListView() {
        val type = when (tabLayout.selectedTabPosition) {
            0 -> AppointmentType.Pending
            1 -> AppointmentType.Approved
            else -> AppointmentType.Completed
        }
        val appointments = viewModel.getAppointments(type)
        adapter.submitList(appointments)
    }
}
