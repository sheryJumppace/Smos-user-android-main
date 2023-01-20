package com.smox.smoxuser.ui.activity.barber

import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.android.volley.Request
import com.google.android.material.navigation.NavigationView
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.messages.services.QBPushManager
import com.quickblox.messages.services.SubscribeService
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import com.smox.smoxuser.App
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ActivityBarberMainBinding
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.manager.SessionManager
import com.smox.smoxuser.ui.activity.HomeActivity
import com.smox.smoxuser.ui.activity.chat.EXTRA_DIALOG_ID
import com.smox.smoxuser.ui.activity.chat.FROM_NOTIFICATION
import com.smox.smoxuser.ui.activity.chat.QBDialogManageActivity
import com.smox.smoxuser.ui.fragment.barber.BarberMainFragment
import com.smox.smoxuser.utils.ACTION_CHAT_EVENT
import com.smox.smoxuser.utils.SharedPrefsHelper
import com.smox.smoxuser.utils.chat.ChatHelper
import com.smox.smoxuser.utils.qb.QbDialogHolder
import com.smox.smoxuser.utils.qb.callback.QBPushSubscribeListenerImpl
import com.smox.smoxuser.utils.qb.isInternetConnected
import com.smox.smoxuser.utils.shortToast
import kotlinx.android.synthetic.main.activity_barber_main.*
import org.json.JSONObject

class BarberMainActivity : QBDialogManageActivity(),
    NavigationView.OnNavigationItemSelectedListener {
    interface OnPageSelectedListener {
        fun onPageSelected(index: Int)
    }

    protected lateinit var app: App
    private val UNAUTHORIZED = 401

    private lateinit var pushBroadcastReceiver: BroadcastReceiver

    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var onPageSelectedListener: OnPageSelectedListener

    private lateinit var navHostFragment: Fragment
    private var isNavDrawerClose = false
    private lateinit var qbChatDialog: QBChatDialog
    private lateinit var chatIntent: Intent
    //private lateinit var billingViewModel: BillingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityBarberMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_barber_main)
        setSupportActionBar(binding.toolbar)

        //fetchSubscriptionProduct()

        app = App.instance
        navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragmentBarber)!!

        qbLogin() // Quickblox login in background
        drawerLayout = binding.drawerLayout
        navigationView = binding.navView

        navController = Navigation.findNavController(this, R.id.navHostFragmentBarber)
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)

        NavigationUI.setupWithNavController(navigationView, navController)

        navigationView.setNavigationItemSelectedListener(this)

        navController.currentDestination


        //App.instance.currentActivity = BarberMainActivity::class.java

        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {
                //Log.i("-onDrawerStateChanged-","onDrawerStateChanged")
                /*if (newState == DrawerLayout.STATE_SETTLING && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    // Drawer started opening
                    navItemEnableDisable(false)
                }
                else {
                    navItemEnableDisable(true)
                }*/
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                //Log.i("-onDrawerSlide-","onDrawerSlide")
                //Log.i("-slideOffset-",""+slideOffset)
                if (slideOffset >= 0.0 && isNavDrawerClose) {
                    navItemEnableDisable(false)
                    drawerLayout.closeDrawers()
                }

            }

            override fun onDrawerClosed(drawerView: View) {
                //Log.i("-onDrawerClosed-","onDrawerClosed")
                isNavDrawerClose = false
                navItemEnableDisable(true)
            }

            override fun onDrawerOpened(drawerView: View) {
                //Log.i("-onDrawerOpened-","onDrawerOpened")
                //navItemEnableDisable(true)
            }
        })
        //val sessionManager = SessionManager.getInstance(applicationContext)

        //Log.e("BarberMainActivity", "Token: "+sessionManager.deviceToken, )

    }

    fun navItemEnableDisable(isEnable: Boolean) {
        navigationView.menu.findItem(R.id.menu_upnext).isEnabled = isEnable
        navigationView.menu.findItem(R.id.menu_calendar).isEnabled = isEnable
        navigationView.menu.findItem(R.id.menu_smox_talk).isEnabled = isEnable
        navigationView.menu.findItem(R.id.menu_my_barber).isEnabled = isEnable
        navigationView.menu.findItem(R.id.menu_supplies).isEnabled = isEnable
        navigationView.menu.findItem(R.id.menu_orders).isEnabled = isEnable
        navigationView.menu.findItem(R.id.menu_edit_account).isEnabled = isEnable
        navigationView.menu.findItem(R.id.menu_about).isEnabled = isEnable
        navigationView.menu.findItem(R.id.menu_contact).isEnabled = isEnable
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            val currentFragment = navHostFragment.childFragmentManager.fragments[0]
            if (currentFragment.javaClass == BarberMainFragment::class.java) {
                when (App.instance.currentPage) {
                    R.id.navigation_bottom_up_next -> drawerLayout.openDrawer(GravityCompat.START)
                    R.id.navigation_bottom_calendar -> onPageSelectedListener.onPageSelected(0)
                    R.id.navigation_bottom_funds -> onPageSelectedListener.onPageSelected(1)
                    R.id.navigation_bottom_smox_talk -> onPageSelectedListener.onPageSelected(1)
                    else -> drawerLayout.openDrawer(GravityCompat.START)
                }
            } else {
                super.onBackPressed()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        isNavDrawerClose = true
        drawerLayout.closeDrawers()
        toolbar.menu.clear()
        //drawerLayout.closeDrawer(GravityCompat.START)
        when (item.itemId) {

            R.id.menu_upnext ->  if (navController.currentDestination?.id == R.id.barberMainFragment) onPageSelectedListener.onPageSelected(0)
            R.id.menu_calendar ->  if (navController.currentDestination?.id == R.id.barberMainFragment) onPageSelectedListener.onPageSelected(1)
            R.id.menu_smox_talk ->  if (navController.currentDestination?.id == R.id.barberMainFragment) onPageSelectedListener.onPageSelected(3)
            R.id.menu_my_barber -> if (navController.currentDestination?.id == R.id.barberMainFragment) navController.navigate(R.id.action_barberMainFragment_to_myBarberFragment)
            R.id.menu_supplies -> if (navController.currentDestination?.id == R.id.barberMainFragment) navController.navigate(R.id.action_barberMainFragment_to_barberProductFragment)
            R.id.menu_orders -> if (navController.currentDestination?.id == R.id.barberMainFragment) navController.navigate(R.id.action_barberMainFragment_to_orderFragment)
            R.id.menu_edit_account -> if (navController.currentDestination?.id == R.id.barberMainFragment) navController.navigate(R.id.action_barber_main_fragment_to_account_fragment)
            R.id.menu_about -> {
                val bundle = bundleOf("url" to Constants.KUrl.about)
                if (navController.currentDestination?.id == R.id.barberMainFragment) navController.navigate(R.id.action_barberMainFragment_to_webViewFragment, bundle)
            }
            R.id.menu_contact -> sendEmail()
            else -> showConfirmLogoutDialog()
        }

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, drawerLayout)
    }

    fun setOnPageSelectedListener(pageSelectedListener: OnPageSelectedListener) {
        onPageSelectedListener = pageSelectedListener
    }

    private fun sendEmail() {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:developer@smoxtrimsetters.com")
        try {
            startActivity(emailIntent)
        } catch (e: ActivityNotFoundException) {

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

    private fun logout() {
        App.instance.currentPage = 0
        logoutQB()
        val sessionManager = SessionManager.getInstance(applicationContext)
        sessionManager.userData = ""
        sessionManager.apiKey = ""
        sessionManager.isSubscribed = false
        sessionManager.subscription_enddate = ""
        sessionManager.Sp_publishableKey = ""
        sessionManager.Sp_privateKey = ""
        val intent =
            Intent(this@BarberMainActivity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun logoutQB() {
        ChatHelper.destroy()
        if (QBPushManager.getInstance().isSubscribedToPushes) {
            QBPushManager.getInstance().addListener(object : QBPushSubscribeListenerImpl() {
                override fun onSubscriptionDeleted(b: Boolean) {
                    QBUsers.signOut().performAsync(null)
                    QBPushManager.getInstance().removeListener(this)
                }
            })
            SubscribeService.unSubscribeFromPushes(this@BarberMainActivity)
        } else {
            QBUsers.signOut().performAsync(null)
        }

        SharedPrefsHelper.removeQbUser()
        QbDialogHolder.clear()
    }

    override fun onResume() {
        super.onResume()

        App.instance.currentActivity = BarberMainActivity::class.java
        chatIntent = intent
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (chatIntent!!.hasExtra(FROM_NOTIFICATION)) {
            if (chatIntent.getBooleanExtra(FROM_NOTIFICATION, false)) {
                //App.instance.currentPage = 2
                qbChatDialog = chatIntent.getSerializableExtra(EXTRA_DIALOG_ID) as QBChatDialog
                val fromNotifification = chatIntent.getSerializableExtra(FROM_NOTIFICATION)

                chatIntent.removeExtra(FROM_NOTIFICATION)
                chatIntent.removeExtra(EXTRA_DIALOG_ID)

                //navigationView.menu.getItem(2).isChecked = true
                onPageSelectedListener.onPageSelected(3)

                val chatIntent = Intent(ACTION_CHAT_EVENT)
                chatIntent.putExtra(EXTRA_DIALOG_ID, qbChatDialog)
                chatIntent.putExtra(FROM_NOTIFICATION, fromNotifification)
                LocalBroadcastManager.getInstance(this).sendBroadcast(chatIntent)
            }
        }
    }

    private fun qbLogin() {
        val qbUser = QBUser()
        val email = app.currentUser.email
        val fullName = app.currentUser.name
        qbUser.email = email
        qbUser.login = email
        qbUser.password = Constants.KQBUUserPassword
        qbUser.fullName = fullName
        signIn(qbUser, object : BarberMainActivity.QBListener {
            override fun onResult(error: String) {
                initQB()
            }
        })
    }

    private fun signIn(user: QBUser, listener: BarberMainActivity.QBListener) {
        ChatHelper.login(user, object : QBEntityCallback<QBUser> {
            override fun onSuccess(userFromRest: QBUser, bundle: Bundle?) {
                if (userFromRest.fullName == user.fullName) {
                    loginToChat(user, listener)
                } else {
                    //Need to set password NULL, because server will update user only with NULL password
                    user.password = null
                    if (isInternetConnected(this@BarberMainActivity)) {
                        updateUser(user, listener)
                    }/* else {
                        Toast.makeText(
                            applicationContext,
                            R.string.error_connection,
                            Toast.LENGTH_LONG
                        ).show()
                    }*/

                }
            }

            override fun onError(e: QBResponseException) {
                if (e.httpStatusCode == UNAUTHORIZED) {
                    if (isInternetConnected(this@BarberMainActivity)) {
                        signUp(user, listener)
                    } /*else {
                        Toast.makeText(applicationContext, R.string.error_connection, Toast.LENGTH_LONG).show()
                    }*/
                } else {
                    listener.onResult(getString(R.string.login_chat_login_error))
                }
            }
        })
    }

    private fun updateUser(user: QBUser, listener: BarberMainActivity.QBListener) {
        ChatHelper.updateUser(user, object : QBEntityCallback<QBUser> {
            override fun onSuccess(qbUser: QBUser, bundle: Bundle?) {
                if (isInternetConnected(this@BarberMainActivity)) {
                    loginToChat(user, listener)
                } /*else {
                    Toast.makeText(applicationContext, R.string.error_connection, Toast.LENGTH_LONG).show()
                }*/
            }

            override fun onError(e: QBResponseException) {
                listener.onResult(getString(R.string.login_chat_login_error))
            }
        })
    }

    private fun loginToChat(user: QBUser, listener: BarberMainActivity.QBListener) {
        //Need to set password, because the server will not register to chat without password
        user.password = Constants.KQBUUserPassword
        ChatHelper.loginToChat(user, object : QBEntityCallback<Void> {
            override fun onSuccess(void: Void?, bundle: Bundle?) {
                SharedPrefsHelper.saveQbUser(user)
                listener.onResult("")
            }

            override fun onError(e: QBResponseException) {
                listener.onResult(getString(R.string.login_chat_login_error))
            }
        })
    }

    private fun signUp(user: QBUser, listener: BarberMainActivity.QBListener) {
        SharedPrefsHelper.removeQbUser()
        QBUsers.signUp(user).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(p0: QBUser?, p1: Bundle?) {
                if (isInternetConnected(this@BarberMainActivity)) {
                    signIn(user, listener)
                }/* else {
                    Toast.makeText(applicationContext, R.string.error_connection, Toast.LENGTH_LONG).show()
                }*/
            }

            override fun onError(exception: QBResponseException?) {
                listener.onResult(getString(R.string.login_chat_login_error))
            }
        })
    }

    interface QBListener {
        fun onResult(error: String)
    }

    /*override fun onPause() {
        super.onPause()
        intent.removeExtra(FROM_NOTIFICATION)
        intent.removeExtra(EXTRA_DIALOG_ID)
    }*/

    /*private fun fetchSubscriptionProduct() {
        billingViewModel = ViewModelProviders.of(this).get(BillingViewModel::class.java)

    }*/

}