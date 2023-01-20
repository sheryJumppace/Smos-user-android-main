package com.smox.smoxuser.ui.activity.customer

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.android.volley.Request
import com.google.android.gms.ads.MobileAds
import com.google.android.material.navigation.NavigationView
import com.kaopiz.kprogresshud.KProgressHUD
import com.smox.smoxuser.App
import com.smox.smoxuser.R
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.manager.ObservingService
import com.smox.smoxuser.manager.SessionManager
import com.smox.smoxuser.model.CustomMenu
import com.smox.smoxuser.ui.activity.HomeActivity
import com.smox.smoxuser.ui.adapter.CustomMenuAdapter
import com.smox.smoxuser.ui.fragment.customer.BarberSearchFragment
import com.smox.smoxuser.utils.shortToast
import kotlinx.android.synthetic.main.activity_customer_main.*
import kotlinx.android.synthetic.main.app_bar_customer_main.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.isNotEmpty


class CustomerMainActivity : LocationActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var navHostFragment: Fragment
    val menuItems = ArrayList<CustomMenu>()
    private lateinit var customMenuAdapter: CustomMenuAdapter
    private lateinit var eventObserver: Observer
    private var isNavDrawerClose = false
    protected lateinit var progressHUD: KProgressHUD
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_main)
        setSupportActionBar(toolbar)
        progressHUD = KProgressHUD(this)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        startLocationUpdatesss()
        navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragmentCustomer)!!

        drawerLayout = drawer_layout
        navigationView = nav_view

        navController = Navigation.findNavController(this, R.id.navHostFragmentCustomer)
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)

        NavigationUI.setupWithNavController(navigationView, navController)

        Log.v("tag",SessionManager.getInstance(this).apiKey!!)
        navigationView.setNavigationItemSelectedListener(this)

        navController.currentDestination

        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {
                Log.i("-onDrawerStateChanged-", "onDrawerStateChanged")
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                Log.i("-onDrawerSlide-", "onDrawerSlide")
                Log.i("-slideOffset-", "" + slideOffset)
                if (slideOffset >= 0.0 && isNavDrawerClose) {
                    menu_list.isEnabled = false
                    drawerLayout.closeDrawers()
                }
            }

            override fun onDrawerClosed(drawerView: View) {
                Log.i("-onDrawerClosed-", "onDrawerClosed")
                isNavDrawerClose = false
                menu_list.isEnabled = true
            }

            override fun onDrawerOpened(drawerView: View) {
                Log.i("-onDrawerOpened-", "onDrawerOpened")
            }

        })

        App.instance.currentActivity = CustomerMainActivity::class.java

        initCustomMenu()
        loadCountOfUnreadEvents()
//        mRegistrationBroadcastReceiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context, intent: Intent) {
//                if (intent.action == Constants.KLocalBroadCast.event) {
//                    // new push notification is received
//                    val unReadCount = App.instance.unreadEvents + 1
//                    App.instance.unreadEvents = unReadCount
//                    updateUnreadEvents()
//                }
//            }
//        }
        eventObserver = Observer { _, _ ->
            updateUnreadEvents()
        }
        ObservingService.getInstance().addObserver(Constants.KLocalBroadCast.event, eventObserver)

        MobileAds.initialize(this) {}
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            val currentFragment = navHostFragment.childFragmentManager.fragments[0]
            if (currentFragment.javaClass == BarberSearchFragment::class.java) {
                drawerLayout.openDrawer(GravityCompat.START)
            } else {
                super.onBackPressed()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_barber_serach -> {
            }
            R.id.menu_upnext -> navController.navigate(R.id.action_barberSearchFragment_to_customerUpNextFragment)
            R.id.menu_appointments -> navController.navigate(R.id.action_barberSearchFragment_to_appointmentsFragment)
            R.id.menu_services -> navController.navigate(R.id.action_barberSearchFragment_to_servicesFragment)
            R.id.menu_events -> navController.navigate(R.id.action_barberSearchFragment_to_eventsFragment)
            R.id.menu_supplies -> navController.navigate(R.id.action_barberSearchFragment_to_productFragment)
            R.id.menu_edit_account -> navController.navigate(R.id.action_barberSearchFragment_to_accountFragment)
            R.id.menu_about -> {
                val bundle = bundleOf("url" to Constants.KUrl.about)
                navController.navigate(R.id.action_barberSearchFragment_to_webViewFragment, bundle)
            }
            R.id.menu_contact -> sendEmail()
            else -> showConfirmLogoutDialog()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val currentFragment = navHostFragment.childFragmentManager.fragments[0]
                    if (currentFragment.javaClass == BarberSearchFragment::class.java) {
                        ( currentFragment as BarberSearchFragment).updateMap()
                    }
                } else {
                    shortToast("Please provide location service for better experience.")
                }
                return
            }
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, drawerLayout)
    }

    private fun sendEmail() {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:developer@smoxtrimsetters.com")
        try {
            startActivity(emailIntent)
        } catch (e: ActivityNotFoundException) {

        }
    }

    fun startLocationUpdatesss() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

    }

    private fun showConfirmLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Log out")
        builder.setMessage("Are you sure you want to Log out?")
        builder.setPositiveButton("LOG OUT") { _, _ ->
            logoutCall()
        }
        builder.setNegativeButton("CANCEL", null)
        builder.show()
    }

    private fun logoutCall() {
        val params = HashMap<String, String>()
        progressHUD.show()

        APIHandler(
            this,
            Request.Method.GET,
            Constants.API.logout,
            params,
            object: APIHandler.NetworkListener{
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    logout()
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }

            }
        )
    }

    private fun logout() {
        ObservingService.getInstance()
            .removeObserver(Constants.KLocalBroadCast.event, eventObserver)
        val sessionManager = SessionManager.getInstance(applicationContext)
        sessionManager.userData = ""
        sessionManager.apiKey = ""
        sessionManager.isSubscribed = false
        sessionManager.subscription_enddate = ""
        sessionManager.Sp_publishableKey = ""
        sessionManager.Sp_privateKey = ""
        val intent =
            Intent(this@CustomerMainActivity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun initCustomMenu() {
        menuItems.add(CustomMenu(R.drawable.search_icon, getString(R.string.barber_search),"Search services , location here....."))
        menuItems.add(CustomMenu(R.drawable.appointment_icon, getString(R.string.appointments),"Check pending, approved, etc....."))
        menuItems.add(CustomMenu(R.drawable.favorite_icon, getString(R.string.favorites),"Your favorites are saved here....."))
        menuItems.add(CustomMenu(R.drawable.events_icon, getString(R.string.events),"Check events here....."))
        menuItems.add(CustomMenu(R.drawable.products_icon, getString(R.string.products_coming),"Browse our products here...…."))
        menuItems.add(CustomMenu(R.drawable.order_icon, getString(R.string.my_orders),"Know about your orders......"))
        menuItems.add(CustomMenu(R.drawable.profile, getString(R.string.edit_account),"See , edit your profile"))
        menuItems.add(CustomMenu(R.drawable.about_us_icon, getString(R.string.about_us),"Wanna know us ?...…."))
        menuItems.add(CustomMenu(R.drawable.phone_icon, getString(R.string.contact_us),"Contact us"))
        customMenuAdapter = CustomMenuAdapter(menuItems)
        menu_list.adapter = customMenuAdapter
        menu_list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            isNavDrawerClose = true
            drawerLayout.closeDrawers()
            menu_list.isEnabled = false
            when (position) {
                0 ->  navController.navigate(R.id.action_barberSearchFragment_to_servicesFragment)
                1 -> navController.navigate(R.id.action_barberSearchFragment_to_appointmentsFragment)
                2 -> navController.navigate(R.id.action_barberSearchFragment_to_servicesFragment)
                3 -> navController.navigate(R.id.action_barberSearchFragment_to_eventsFragment)
                4 -> navController.navigate(R.id.action_barberSearchFragment_to_productFragment)
                5 -> navController.navigate(R.id.action_barberSearchFragment_to_myOrdersFragment)
                6 -> navController.navigate(R.id.action_barberSearchFragment_to_accountFragment)
                7 -> {
                    val bundle = bundleOf("url" to Constants.KUrl.about)
                    navController.navigate(
                        R.id.action_barberSearchFragment_to_webViewFragment,
                        bundle
                    )
                }
                8 -> navController.navigate(R.id.action_barberSearchFragment_to_contactusfragment)
                else -> showConfirmLogoutDialog()
            }

            //drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun updateUnreadEvents() {

        val unReadEvents = App.instance.unreadEvents

        var badge = ""
        badge = if (unReadEvents == 0) {
            ""
        } else if (unReadEvents > 9) {
            "9+"
        } else {
            unReadEvents.toString()
        }
        val eventMenu = menuItems[3]
        eventMenu.badge = badge

        runOnUiThread {
            customMenuAdapter.notifyDataSetChanged()
        }

    }

    private fun loadCountOfUnreadEvents() {
        val params = HashMap<String, String>()
        APIHandler(
            this,
            Request.Method.GET,
            Constants.API.event_unread,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    val unreadEvents = result.getInt("result")
                    App.instance.unreadEvents = unreadEvents
                    updateUnreadEvents()
                }

                override fun onFail(error: String?) {
                }
            })
    }
}
