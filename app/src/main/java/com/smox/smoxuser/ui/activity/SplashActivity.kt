package com.smox.smoxuser.ui.activity

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.smox.smoxuser.App
import com.smox.smoxuser.R
import com.smox.smoxuser.data.AppointmentRepository
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.ui.activity.auth.LoginActivity
import com.smox.smoxuser.ui.activity.customer.*
import com.smox.smoxuser.ui.activity.customer.LocationActivity.Companion.REQUEST_CHECK_SETTINGS
import com.smox.smoxuser.utils.shortToast
import com.smox.smoxuser.viewmodel.AppointmentViewModel
import com.smox.smoxuser.viewmodel.AppointmentViewModelFactory
import kotlinx.android.synthetic.main.activity_splash.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class SplashActivity : BaseActivity() {

    private var userID = "0"
    private var fusedLocationClient: FusedLocationProviderClient?=null
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var lastLocation: Location? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val w = window // in Activity's onCreate() for instance
            w.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        FirebaseApp.initializeApp(this)
        progressHUD.show()

        val userData = sessionManager.userData!!

        if (userData.isNotEmpty()) {
            val jobUserData = JSONObject(userData)
            userID = jobUserData.getString("id")
            progressHUD.dismiss()
            openNextScreen()
        } else {
            sessionManager.userId=-1
            progressHUD.dismiss()
            openNextScreen()
        }
    }

    override fun onResume() {
        super.onResume()
        App.instance.checkSubscription = true;
        changeDarkMode()

    }

    private fun changeDarkMode() {
        if (sessionManager.isDarkModeOn) {
            val handler = Handler(Looper.myLooper()!!)
            handler.postDelayed({
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }, 1000)
        } else {
            val handler = Handler(Looper.myLooper()!!)
            handler.postDelayed({
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }, 1000)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.e("TAG", "onNewIntent: splash called $intent")
    }

    private fun openNextScreen() {

        var pushType = ""
        if (intent.hasExtra("type")) {
            pushType = intent.getStringExtra("type").toString()
        }


        if (pushType == "event") {
            val intent = Intent(applicationContext, EventsActivity::class.java)
            startActivity(intent)
            this@SplashActivity.finish()
        } else {
            val isNeedTutorial = sessionManager.isFirstTime
            sessionManager.isDarkModeSet = false
            val isFromNotification =
                intent.getBooleanExtra(Constants.API.PUT_EXTRA_IS_FROM_NOTIFICATION, false)
            //  Fabric.with(this, Crashlytics())
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
            Handler().postDelayed({
                if (intent.dataString?.contains("smox.page") == true) {
                    handleDeepLink()
                } else {
                    val intent1 = Intent(this@SplashActivity, HomeActivity::class.java)
                    if (isFromNotification) {
                        intent1.putExtra(
                            Constants.API.APPOINT_ID,
                            intent.getIntExtra(Constants.API.APPOINT_ID, 0)
                        )
                    } else {
                        val extra = intent.extras
                        if (extra?.get("appointment_id") != null) {
                            intent1.putExtra(
                                Constants.API.APPOINT_ID,
                                extra.get("appointment_id").toString().toInt()
                            )
                        }
                    }
                    startActivity(intent1)
                    this@SplashActivity.finish()
                }

            }, 2000)
        }
    }

    private fun handleDeepLink() {
       checkForLocation()
        llProgress.visibility=View.VISIBLE
    }

    private fun checkForLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locResult: LocationResult) {
                super.onLocationResult(locResult)
                lastLocation = locResult.lastLocation
                /*Log.e("SPLASH", "onLocationResult: "+locResult.locations[0].latitude )
                lastLocation?.apply {
                    App.instance.myLocation = LatLng(this.latitude, this.longitude)
                }
                fusedLocationClient?.removeLocationUpdates(locationCallback)
                callDeepLinkMethod()*/
            }

        }
        createLocationRequest()
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(
                        this,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }
    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LocationActivity.LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()!! /* Looper */
        )

        fusedLocationClient?.lastLocation?.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
            }
            Log.e("SPLASH", "startLocationUpdates: "+lastLocation?.latitude +" "+lastLocation?.longitude )
            lastLocation?.apply {
                App.instance.myLocation = LatLng(latitude, longitude)
            }
            llProgress.visibility=View.GONE
            callDeepLinkMethod()
        }
    }

    private fun callDeepLinkMethod() {
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(
                this
            ) { pendingDynamicLinkData ->
                //Get deep link from result (may be null if no link is found)
                llProgress.visibility=View.GONE
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    if (deepLink.toString().contains("appointmentId")) {
                        val appointId = deepLink?.getQueryParameter("appointmentId")
                        if (sessionManager.apiKey?.isNotEmpty()!!){
                            getAppointmentDetailForPayment(appointId!!.toInt())
                        }else{
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
                        }
                    } else if (deepLink.toString().contains("barberId")) {
                        val barberId = deepLink?.getQueryParameter("barberId")
                        openBarberDetails(barberId!!.toInt())
                    }
                }
            }
            .addOnFailureListener(
                this
            ) { e -> Log.w("TAG", "getDynamicLink:onFailure", e) }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LocationActivity.LOCATION_PERMISSION_REQUEST_CODE) {
            // Request for camera permission.
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                startLocationUpdates()
            }
        }
    }

    private fun openBarberDetails(barberId: Int) {
        Log.e("TAG", "openBarberDetails: changing page ", )
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("barber_id", barberId)
        startActivity(intent)
        overridePendingTransition(
            R.anim.activity_enter,
            R.anim.activity_exit
        )
    }

    override fun onStop() {
        super.onStop()
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }


    private fun getAppointmentDetailForPayment(appointId: Int) {
        val factory = AppointmentViewModelFactory(AppointmentRepository.getInstance(), appointId)
        val viewModel = ViewModelProvider(this, factory).get(AppointmentViewModel::class.java)
        //Log.e("TAG", "getAppointmentDetailForPayment: $userID ${app.currentUser.id}")
        viewModel.appointment.value=null
        viewModel.appointment.observe(this, androidx.lifecycle.Observer {
            if (it != null) {
                if (userID.toInt() == it.customerId) {
                    val inputFormat = SimpleDateFormat(
                        Constants.KDateFormatter.serverDay,
                        Locale.getDefault()
                    )
                    val abc = inputFormat.parse(it.appointmentDate)
                    it.preferredDate = abc
                    it.strOnlyDate = inputFormat.format(abc)
                    //if (sessionManager.apiKey?.isNotEmpty()!!) {
                        if (it.isPaid) {
                            Intent(this, AppointmentDetailsNewActivity::class.java)
                            intent.putExtra(Constants.API.APPOINT_ID, appointId)
                            startActivity(intent)
                            overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
                        } else {
                            startActivity(
                                Intent(this, BookAppointmentPaymentActivity::class.java)
                                    .putExtra("appointment", it)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            )
                        }
                    /*}else{
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
                    }*/
                    finishAffinity()
                } else {
                    startActivity(
                        Intent(this, HomeActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                    finishAffinity()
                }
            } else {
                shortToast("This link expired")
                val intent1 = Intent(this@SplashActivity, HomeActivity::class.java)
                startActivity(intent1)
                this@SplashActivity.finish()
            }
        })
        viewModel.fetchList(this)

    }

}
