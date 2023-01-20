package com.smox.smoxuser.ui.activity.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.navigation.NavigationView
import com.kaopiz.kprogresshud.KProgressHUD
import com.smox.smoxuser.App
import com.smox.smoxuser.R
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.manager.Constants.API.CALLED_FROM
import com.smox.smoxuser.manager.Constants.API.CART
import com.smox.smoxuser.manager.ObservingService
import com.smox.smoxuser.manager.SessionManager
import com.smox.smoxuser.model.CustomMenu
import com.smox.smoxuser.ui.activity.HomeActivity
import com.smox.smoxuser.ui.activity.auth.LoginActivity
import com.smox.smoxuser.ui.activity.customer.*
import com.smox.smoxuser.ui.activity.orders.OrdersActivity
import com.smox.smoxuser.ui.activity.product.ProductsActivity
import com.smox.smoxuser.ui.adapter.NewSideMenuAdapter
import com.smox.smoxuser.ui.fragment.customer.BarberSearchFragment
import com.smox.smoxuser.utils.listeners.OnGetAddress
import com.smox.smoxuser.utils.listeners.OnItemClicked
import com.smox.smoxuser.utils.listeners.OnLocFound
import com.smox.smoxuser.utils.listeners.OnSearchItem
import com.smox.smoxuser.utils.shortToast
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.app_bar_main.*
import org.json.JSONObject
import java.util.*


class Home2Activity : LocationActivity() {
    private val TAG = "Home2Activity"

    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var navHostFragment: Fragment
    val menuItems = ArrayList<CustomMenu>()
    private lateinit var customMenuAdapter: NewSideMenuAdapter
    private lateinit var eventObserver: Observer
    private var isNavDrawerClose = false
    protected lateinit var progressHUD: KProgressHUD
    lateinit var searchBar: ImageView
    lateinit var imgClose: ImageView
    lateinit var llTitleBar: LinearLayoutCompat
    lateinit var llSearchBar: LinearLayoutCompat
    lateinit var etSearchText: EditText
    lateinit var txtCurrAddress: TextView
    lateinit var llCurrAdd: LinearLayoutCompat
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    lateinit var onSearchItem: OnSearchItem
    lateinit var onLocFound: OnLocFound
    private val RC_ADDRESS = 520
    protected lateinit var sessionManager: SessionManager
    lateinit var darkModeSwitch: SwitchCompat
    var isFirstTime = true
    var mMessageReceiver: BroadcastReceiver? = null

    override fun onRestart() {
        super.onRestart()
        Log.e(TAG, "onRestart: ")
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG, "onPause: ")
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG, "onStop: ")
    }

    override fun onStart() {
        super.onStart()
        Log.e(TAG, "onStart: ")
        registerReceiverr()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy: ", )
        unregisterReceiverr()
    }

    private fun registerReceiverr() {
        mMessageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action.equals(Constants.API.UN_AUTHORISED)){
                    logout()
                }
            }
        }
        val filter = IntentFilter(Constants.API.UN_AUTHORISED)
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver!!, filter)
    }

    private fun unregisterReceiverr() {
        if (mMessageReceiver != null) LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(mMessageReceiver!!)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home2)
        setSupportActionBar(newToolbar)
        progressHUD = KProgressHUD(this)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        startLocationUpdates()
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)!!

        drawerLayout = new_drawer_layout
        navigationView = nav_view
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)

        NavigationUI.setupWithNavController(navigationView, navController)

        Log.v("tag", SessionManager.getInstance(this).apiKey!!)

        sessionManager = SessionManager.getInstance(applicationContext)
        darkModeSwitch = findViewById(R.id.darkModeSwitch)

        Log.e(TAG, "onCreate: ")

        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {
                Log.i("-onDrawerStateChanged-", "onDrawerStateChanged")
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                Log.i("-onDrawerSlide-", "onDrawerSlide")
                Log.i("-slideOffset-", "" + slideOffset)
                if (slideOffset >= 0.0 && isNavDrawerClose) {
                    rvItem.isEnabled = false
                    drawerLayout.closeDrawers()
                }
            }

            override fun onDrawerClosed(drawerView: View) {
                Log.i("-onDrawerClosed-", "onDrawerClosed")
                isNavDrawerClose = false
                rvItem.isEnabled = true
            }

            override fun onDrawerOpened(drawerView: View) {
                Log.i("-onDrawerOpened-", "onDrawerOpened")
            }

        })

        darkModeSwitch.isChecked = sessionManager.isDarkModeOn

        darkModeSwitch.setOnClickListener {
            if (!sessionManager.isDarkModeOn) {
                darkModeSwitch.isChecked = true
                sessionManager.isDarkModeOn = true
                val handler = Handler(Looper.myLooper()!!)
                handler.postDelayed({
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }, 1000)
            } else {
                darkModeSwitch.isChecked = false
                sessionManager.isDarkModeOn = false
                val handler = Handler(Looper.myLooper()!!)
                handler.postDelayed({
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }, 1000)
            }
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key), Locale.US)
        }

        newToolbar.findViewById<ImageView>(R.id.drawerMenu).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        navHeader.findViewById<ImageView>(R.id.imgCancel).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        App.instance.currentActivity = Home2Activity::class.java


        txtCurrAddress = findViewById(R.id.txtCurrAddress)
        llCurrAdd = findViewById(R.id.llCurrInfo)

        locListner(object : OnGetAddress {
            override fun onGetAddress(address: String, latLng: LatLng) {
                txtCurrAddress.text = currAddress
                onLocFound.onLocFound(latLng, this@Home2Activity)
            }
        })

        txtCurrAddress.setOnClickListener {
            launchLocationAutoCompleteActivity()
        }

        findViewById<ImageView>(R.id.imgGps).setOnClickListener {
            val currentFragment = navHostFragment.childFragmentManager.fragments[0]
            if (currentFragment.javaClass == BarberSearchFragment::class.java) {
                //(currentFragment as BarberSearchFragment).updateMap()
                startLocationUpdates()
            }
        }

        searchBar = findViewById(R.id.searchBar)
        imgClose = findViewById(R.id.imgCloseSearch)
        llTitleBar = findViewById(R.id.llTitleBar)
        llSearchBar = findViewById(R.id.llSearchBar)
        etSearchText = findViewById(R.id.etSearchText)
        searchBar.setOnClickListener {
            startActivity(Intent(this@Home2Activity, SearchAnyActivity::class.java))
            /* llSearchBar.visibility = View.VISIBLE
             llTitleBar.visibility = View.GONE
             etSearchText.requestFocus()
             openKeyboard(this)*/
        }

        imgClose.setOnClickListener {
            llSearchBar.visibility = View.GONE
            llTitleBar.visibility = View.VISIBLE
            etSearchText.setText("")
            hideKeyboard(currentFocus ?: View(this))
        }

        initCustomMenu()
        loadCountOfUnreadEvents()

        eventObserver = Observer { _, _ ->
            updateUnreadEvents()
        }
        ObservingService.getInstance().addObserver(Constants.KLocalBroadCast.event, eventObserver)

        MobileAds.initialize(this) {}

        etSearchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                onSearchItem.onSearchText(text.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }


    fun onSearchClicked(onSearchItem: OnSearchItem) {
        this.onSearchItem = onSearchItem
    }

    fun onFoundLocation(onLocFound: OnLocFound) {
        this.onLocFound = onLocFound
    }


    override fun onBackPressed() {
        super.onBackPressed()
        /*if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            val currentFragment = navHostFragment.childFragmentManager.fragments[0]
            if (currentFragment.javaClass == BarberSearchFragment::class.java) {
                drawerLayout.openDrawer(GravityCompat.START)
            } else {
                super.onBackPressed()
            }
        }*/
    }

    private fun launchLocationAutoCompleteActivity() {
        if (Places.isInitialized()) {
            val fields: List<Place.Field> = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.ADDRESS_COMPONENTS,
                Place.Field.LAT_LNG
            )
            val autoSearchIntent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN,
                fields
            ).build(this)
            startActivityForResult(autoSearchIntent, RC_ADDRESS)
        }

    }

    @SuppressLint("RestrictedApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_ADDRESS && resultCode == RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data!!)
            place.latLng
            txtCurrAddress.text = place.name!!
            val currentFragment = navHostFragment.childFragmentManager.fragments[0]
            if (currentFragment.javaClass == BarberSearchFragment::class.java) {
                (currentFragment as BarberSearchFragment).getNewLocBarberList(place)
            }

        }
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
                        (currentFragment as BarberSearchFragment).updateMap()
                    }
                } else {
                    //shortToast("Please provide location service for better experience.")
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

    private fun startLocationUpdatesss() {
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
            object : APIHandler.NetworkListener {
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

    override fun onResume() {
        super.onResume()
        //isFirstTime=true
        Log.e(TAG, "onResume: ")
        if (sessionManager.apiKey?.isEmpty()!!){
            llLogout.visibility=View.GONE
            llSignin.visibility=View.VISIBLE
        }else{
            llLogout.visibility=View.VISIBLE
            llSignin.visibility=View.GONE
        }
    }

    fun logout() {
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
            Intent(this@Home2Activity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun initCustomMenu() {
        menuItems.add(
            CustomMenu(
                R.drawable.ic_edit_account,
                getString(R.string.edit_account),
                "See , edit your profile"
            )
        )
        menuItems.add(
            CustomMenu(
                R.drawable.ic_barber_search,
                getString(R.string.barber_search),
                "Search services , location here"
            )
        )
        menuItems.add(
            CustomMenu(
                R.drawable.ic_calendar,
                getString(R.string.appointments),
                "Check pending, approved, etc"
            )
        )
        menuItems.add(
            CustomMenu(
                R.drawable.ic_unlike,
                getString(R.string.favorites),
                "Your favorites are saved here"
            )
        )
        menuItems.add(
            CustomMenu(
                R.drawable.ic_event,
                getString(R.string.events),
                "Check events here"
            )
        )
        menuItems.add(
            CustomMenu(
                R.drawable.ic_supplies,
                getString(R.string.cart),
                "Check your cart here"
            )
        )
        menuItems.add(
            CustomMenu(
                R.drawable.ic_supplies,
                getString(R.string.my_orders),
                "Check your orders here"
            )
        )
        menuItems.add(
            CustomMenu(
                R.drawable.ic_supplies,
                getString(R.string.smox_talks),
                "Chat with styler"
            )
        )
        menuItems.add(
            CustomMenu(
                R.drawable.ic_about_us,
                getString(R.string.about_us),
                "Wanna know us ?"
            )
        )
        menuItems.add(
            CustomMenu(
                R.drawable.ic_contact_us,
                getString(R.string.contact_us),
                "Contact us"
            )
        )
        //menuItems.add(CustomMenu(R.drawable.ic_logout, getString(R.string.logout)))

        customMenuAdapter = NewSideMenuAdapter(menuItems, object : OnItemClicked {
            override fun onItemClick(pos: Int) {
                when (pos) {
                    0 -> {
                        if (sessionManager.apiKey?.isNotEmpty()!!)
                        startActivity(Intent(this@Home2Activity, CustProfileActivity::class.java))
                        else
                            callLoginPage()
                    }
                    1 ->
                        startActivity(Intent(this@Home2Activity, SearchAnyActivity::class.java))
                    2 -> {
                        if (sessionManager.apiKey?.isNotEmpty()!!)
                            startActivity(
                                Intent(this@Home2Activity, AppointmentListActivity::class.java)
                                    .putExtra("calledFrom", "Home")
                            )
                        else
                            callLoginPage()
                    }
                    3 -> {
                        if (sessionManager.apiKey?.isNotEmpty()!!)
                            startActivity(Intent(this@Home2Activity, FavoritesActivity::class.java))
                        else
                            callLoginPage()
                    }
                    4 -> {
                        if (sessionManager.apiKey?.isNotEmpty()!!)
                            startActivity(Intent(this@Home2Activity, EventsActivity::class.java))
                        else
                            callLoginPage()
                    }
                    5 -> {
                        startActivity(Intent(this@Home2Activity, ProductsActivity::class.java).putExtra(CALLED_FROM,CART))
                    }
                    6 -> {
                        startActivity(Intent(this@Home2Activity, OrdersActivity::class.java).putExtra(CALLED_FROM,CART))
                    }
                    7 -> {
                        navController.navigate(R.id.action_barberSearchFragment_to_smoxTalks)
                    }
                    8 -> {
                        startActivity(Intent(this@Home2Activity, AboutUsActivity::class.java))
                        /*val bundle = bundleOf("url" to Constants.KUrl.about)
                        navController.navigate(
                            R.id.action_barberSearchFragment_to_webViewFragment,
                            bundle
                        )*/
                    }
                    9 -> startActivity(Intent(this@Home2Activity, ContactUsActivity::class.java))
                    else -> showConfirmLogoutDialog()
                }
                drawerLayout.closeDrawers()
            }
        })
        rvItem.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvItem.setHasFixedSize(true)
        rvItem.adapter = customMenuAdapter

        llLogout.setOnClickListener {
            showConfirmLogoutDialog()
        }
        llSignin.setOnClickListener {
            callLoginPage()
        }

    }

    private fun callLoginPage() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
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

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}