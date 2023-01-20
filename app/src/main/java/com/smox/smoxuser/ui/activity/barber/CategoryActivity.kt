package com.smox.smoxuser.ui.activity.barber

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.smox.smoxuser.R
import com.smox.smoxuser.data.ServiceRepository
import com.smox.smoxuser.model.Category
import com.smox.smoxuser.model.Service
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.adapter.ServiceAdapter
import com.smox.smoxuser.viewmodel.ServiceListViewModel
import com.smox.smoxuser.viewmodel.ServiceListViewModelFactory
import kotlinx.android.synthetic.main.activity_booking_appointment.toolbar
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.activity_category.service_list

class CategoryActivity : BaseActivity() {

    private lateinit var viewModel: ServiceListViewModel
    var items: ArrayList<Category> = ArrayList()
    private lateinit var custSelectedServices: java.util.ArrayList<Service>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Services"

        }

        if (intent != null && intent.hasExtra("category_item")) {
            items = intent.getSerializableExtra("category_item") as ArrayList<Category>
        }

        if (intent.hasExtra("appointment_service")) {
            custSelectedServices = intent.getSerializableExtra("appointment_service") as java.util.ArrayList<Service>
        } else {
            custSelectedServices = java.util.ArrayList()
        }

        val factory = ServiceListViewModelFactory(ServiceRepository.getInstance(app.currentUser.id))
        viewModel = ViewModelProvider(this, factory).get(ServiceListViewModel::class.java)
        items.forEach { catv ->
            for (i in 0..catv.services.size - 1) {
                for (j in 0..custSelectedServices.size - 1) {
                    if (catv.services.get(i).id == custSelectedServices.get(j).id) {
                        //var selectService : Service = services.get(i)
                        //selectService.isSelected = ObservableBoolean(true)
                        catv.services.get(i).isSelected = ObservableBoolean(true)
                    }
                }
            }
        }


        setTabLayout()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {

            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                setAdapter(tabLayout.selectedTabPosition)
            }

        })
    }

    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_save -> {
                selectServices()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        //set the colors for android version < android 8 (because before that the spannable string builder with foregroundcolorspan doesnt work)
        menu.let {
            for (i in 0 until menu.size()) {
                val menuItem = menu.getItem(i)
                menuItem.title = "DONE"
            }
        }
        return true
    }

    private fun setTabLayout() {

        for (category: Category in items) {
            tabLayout.addTab(tabLayout.newTab().setText(category.cat_name))
        }
        setAdapter(tabLayout.selectedTabPosition)

    }

    private fun setAdapter(selectedTabPosition: Int) {
        val adapter = ServiceAdapter(isSmall = false, isSelect = true)
        service_list.adapter = adapter
        subscribeUi(adapter, items.get(selectedTabPosition).services)
    }

    private fun subscribeUi(
        adapter: ServiceAdapter,
        services: ArrayList<Service>
    ) {
        if (services.isNotEmpty()) {
            adapter.submitList(services)
            adapter.notifyDataSetChanged()
            tvNotFoundx.visibility = View.GONE
        } else {
            adapter.submitList(services)
            adapter.notifyDataSetChanged()
            tvNotFoundx.visibility = View.VISIBLE
        }
    }

    private fun selectServices() {
        val selectedServices: ArrayList<Service> = ArrayList()

        for (category in items) {
            //if (category.cat_name!!.equals(items.get(tabLayout.selectedTabPosition).cat_name))
                for (service in category.services) {
                    if (service.isSelected.get()) {
                        selectedServices.add(service)
                    }
                }
        }
        if (selectedServices.isEmpty()) {
            showAlertDialog(
                "",
                "Please select a service to proceed for booking",
                DialogInterface.OnClickListener { _, _ ->
                },
                getString(R.string.ok),
                null,
                null
            )
            return
        }
        viewModel.updateServices(selectedServices)
        val intent = Intent().apply {
            putExtra("selectedService", selectedServices)
            // Put your data here if you want.
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

}


