package com.smox.smoxuser.ui.activity.customer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.smox.smoxuser.R
import com.smox.smoxuser.data.AppointmentRepository
import com.smox.smoxuser.databinding.ActivityAppointmentListBinding
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Appointment
import com.smox.smoxuser.model.type.AppointmentType
import com.smox.smoxuser.ui.activity.home.Home2Activity
import com.smox.smoxuser.ui.adapter.NewAppointmentAdapter
import com.smox.smoxuser.utils.PaginationScrollListner
import com.smox.smoxuser.viewmodel.AppointmentListViewModel
import com.smox.smoxuser.viewmodel.AppointmentListViewModelFactory
import kotlinx.android.synthetic.main.activity_book_appointment.*
import kotlinx.android.synthetic.main.fragment_appointments.*

class AppointmentListActivity : AppCompatActivity() {

    private val TAG = "AppointmentListActivity"
    private val EDIT_REQUEST_CODE = 222

    private lateinit var viewModel: AppointmentListViewModel
    private lateinit var adapter: NewAppointmentAdapter
    lateinit var binding: ActivityAppointmentListBinding
    private val totalPage = 100
    private var pageStart = 0
    private var isLastPagee = false
    var isLoadingg = false
    var type = AppointmentType.Pending
    var appointmentsMainList = arrayListOf<Appointment>()
    var isFirstTime = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_appointment_list)

        val factory = AppointmentListViewModelFactory(AppointmentRepository.getInstance())
        viewModel = ViewModelProviders.of(this, factory).get(AppointmentListViewModel::class.java)


        val resultEdit =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                isLastPagee = false
                pageStart = 0
                appointmentsMainList.clear()
                adapter.clearList()
                callAppoinmentListData()
            }
        adapter = NewAppointmentAdapter(this, object : NewAppointmentAdapter.ItemClickListner {
            override fun onItemClickListner(pos: Int, appointId: Int, appointment: Appointment) {
                val intent =
                    Intent(this@AppointmentListActivity, AppointmentDetailsNewActivity::class.java)
                intent.putExtra(Constants.API.APPOINT_ID, appointId)
                intent.putExtra("appointment", appointment)
                startActivity(intent)
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
            }
        }, object : NewAppointmentAdapter.EditClickListner {
            override fun onEditClickListner(appointment: Appointment) {
                val intent =
                    Intent(this@AppointmentListActivity, EditAppointmentActivity::class.java)
                        .putExtra(Constants.API.APPOINTMENT, appointment)
                        .putExtra(Constants.API.BARBER_ID, appointment.barberId)
                resultEdit.launch(intent)
            }

        })


        binding.imgBack.setOnClickListener {
            if (intent.hasExtra("calledFrom")) {
                if (intent.getStringExtra("calledFrom").equals("ThanksPage")) {
                    val intent = Intent(this@AppointmentListActivity, Home2Activity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else
                    onBackPressed()
            } else
                onBackPressed()
        }
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.appointList.layoutManager = layoutManager
        binding.appointList.adapter = adapter
        viewModel.appointments.observe(this, Observer { appointments ->
            if (!isFirstTime) {
                if (!appointments.isNullOrEmpty()) {
                    isLastPagee = false
                    appointmentsMainList.addAll(appointments)
                    addMoreData(appointments, true)
                } else
                    isLastPagee = true
                isLoadingg = false
            }
        })

        binding.appointList.addOnScrollListener(object : PaginationScrollListner(layoutManager) {
            override fun loadMoreItems() {
                if (!isLastPagee) {
                    if (type == AppointmentType.Pending)
                        callAppoinmentListData()
                }
            }

            override fun getTotalPageCount(): Int {
                return totalPage
            }

            override fun isLastPage(): Boolean {
                return isLastPagee
            }

            override fun isLoading(): Boolean {
                return isLoadingg
            }
        })

        binding.tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                type = when (tabLayout.selectedTabPosition) {
                    0 -> AppointmentType.Pending
                    1 -> AppointmentType.Approved
                    else -> AppointmentType.Completed
                }
                addMoreData(appointmentsMainList, false)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            isLastPagee = false
            pageStart = 0
            appointmentsMainList.clear()
            adapter.clearList()
            callAppoinmentListData()
        }


        isLastPagee = false
        pageStart = 0
        appointmentsMainList.clear()
        adapter.clearList()
        isFirstTime = false
        callAppoinmentListData()
    }

    private fun addMoreData(appointments: List<Appointment>, isSameTab: Boolean) {
        val filterList = getFilteredData(appointments, type)
        adapter.addMoreItems(filterList, isSameTab)

        if (!isSameTab && filterList.isEmpty()) {
            binding.tvNoDataFound.visibility = View.VISIBLE
            binding.appointList.visibility = View.GONE
        } else {
            binding.tvNoDataFound.visibility = View.GONE
            binding.appointList.visibility = View.VISIBLE
        }
    }

    private fun getFilteredData(
        items: List<Appointment>,
        type: AppointmentType
    ): List<Appointment> {
        return items?.filter { it.status == type } ?: ArrayList<Appointment>()
    }

    private fun callAppoinmentListData() {
        if (!this.isLastPagee) {
            isLoadingg = true
            pageStart += 1
            Log.e(TAG, "loadMoreItems: called pageStart: $pageStart   isLastPage: $isLastPagee")

            viewModel.fetchAppointmentsOfCustomer(
                this@AppointmentListActivity,
                pageStart.toString()
            )
        }
    }

    override fun onResume() {
        super.onResume()
        /*isLastPagee = false
        pageStart = 0
        appointmentsMainList.clear()
        adapter.clearList()
        isFirstTime=false
        callAppoinmentListData()*/
    }

    override fun onBackPressed() {

        if (intent.hasExtra("calledFrom")) {
            if (intent.getStringExtra("calledFrom").equals("ThanksPage")) {
                val intent = Intent(this@AppointmentListActivity, Home2Activity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            } else
                super.onBackPressed()
        } else
            super.onBackPressed()
    }
}