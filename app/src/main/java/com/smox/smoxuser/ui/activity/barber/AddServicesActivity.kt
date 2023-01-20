package com.smox.smoxuser.ui.activity.barber

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.smox.smoxuser.R
import com.smox.smoxuser.data.ServiceRepository
import com.smox.smoxuser.model.Service
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.adapter.ServiceAdapter
import com.smox.smoxuser.viewmodel.ServiceListViewModel
import com.smox.smoxuser.viewmodel.ServiceListViewModelFactory
import kotlinx.android.synthetic.main.activity_add_services.*

class AddServicesActivity : BaseActivity() {

    private lateinit var viewModel: ServiceListViewModel
    private var barberId = 0
    private lateinit var custSelectedServices: ArrayList<Service>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_services)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@AddServicesActivity, Constants.backButton))
        }

        barberId = intent.getIntExtra("barber_id", 0)

        if (intent.hasExtra("appointment_service")) {
            custSelectedServices =
                intent.getSerializableExtra("appointment_service") as ArrayList<Service>
        } else {
            custSelectedServices = ArrayList()
        }

        val factory = ServiceListViewModelFactory(ServiceRepository.getInstance(barberId))
        viewModel = ViewModelProvider(this, factory).get(ServiceListViewModel::class.java)
        val adapter = ServiceAdapter(isSmall = false, isSelect = true)
        service_list.adapter = adapter
        subscribeUi(adapter)
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

    private fun selectServices() {
        val selectedServices = viewModel.getSelectedContacts() as ArrayList
        if (selectedServices.isEmpty()) {
            return
        }
        viewModel.updateServices(selectedServices)
        val intent = Intent().apply {
            putExtra("isServiceUpdate", true)
            // Put your data here if you want.
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun subscribeUi(adapter: ServiceAdapter) {
        viewModel.fetchList(this)
        viewModel.services.observe(this, Observer { services ->
            if (services != null) {
                if (custSelectedServices != null) {
                    for (i in 0..services.size - 1) {
                        for (j in 0..custSelectedServices.size - 1) {
                            if (services.get(i).id == custSelectedServices.get(j).id) {
                                //var selectService : Service = services.get(i)
                                //selectService.isSelected = ObservableBoolean(true)
                                services.get(i).isSelected = ObservableBoolean(true)
                            }
                        }
                    }
                }

                adapter.submitList(services)
            }
        })
    }

}
