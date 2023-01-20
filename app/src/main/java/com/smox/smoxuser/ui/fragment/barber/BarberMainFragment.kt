package com.smox.smoxuser.ui.fragment.barber


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.quickblox.chat.model.QBChatDialog
import com.smox.smoxuser.R
import com.smox.smoxuser.ui.activity.barber.BarberMainActivity
import com.smox.smoxuser.ui.activity.chat.ChatActivity
import com.smox.smoxuser.ui.activity.chat.EXTRA_DIALOG_ID
import com.smox.smoxuser.ui.activity.chat.FROM_NOTIFICATION
import com.smox.smoxuser.ui.fragment.BaseFragment
import com.smox.smoxuser.ui.fragment.chat.SmoxTalkFragment
import com.smox.smoxuser.utils.ACTION_CHAT_EVENT

class BarberMainFragment : BaseFragment(), BottomNavigationView.OnNavigationItemSelectedListener,
    BarberMainActivity.OnPageSelectedListener {


    private lateinit var mBottomNavigation: BottomNavigationView
    private lateinit var chatBroadcastReceiver: BroadcastReceiver
    private lateinit var mContext: Context
    private val REQUEST_DIALOG_ID_FOR_UPDATE = 165

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_barber_main, container, false)
        initView(v)
        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        if (context is BarberMainActivity) {
            val parentActivity = context
            parentActivity.setOnPageSelectedListener(this)
        }
    }

    override fun onPageSelected(index: Int) {
        when (index) {
            0 -> mBottomNavigation.selectedItemId = R.id.navigation_bottom_up_next
            1 -> mBottomNavigation.selectedItemId = R.id.navigation_bottom_calendar
            2 -> openWebLink()//mBottomNavigation.selectedItemId = R.id.navigation_bottom_funds
            3 -> mBottomNavigation.selectedItemId = R.id.navigation_bottom_smox_talk
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.navigation_bottom_funds) {
            openWebLink()
            false
        } else {
            refreshView(item.itemId)
            true
        }
    }

    private fun refreshView(itemId: Int) {
        app.currentPage = itemId
        val fragment: Any? = when (itemId) {
            R.id.navigation_bottom_calendar -> CalendarFragment()
            R.id.navigation_bottom_up_next -> UpNextFragment()
            R.id.navigation_bottom_funds -> null
            R.id.navigation_bottom_smox_talk -> SmoxTalkFragment()
            //R.id.navigation_bottom_smox_talk -> if (ChatHelper.getCurrentUser() != null) SmoxTalkFragment() else null
            else -> UpNextFragment()
        }

        if (fragment != null) {
            val ft = fragmentManager?.beginTransaction()
            ft?.replace(R.id.frameContainer, fragment as Fragment)
            ft?.commit()
        } else if (itemId == R.id.navigation_bottom_funds) {
            openWebLink()
        }
    }

    private fun openWebLink() {
        val launchBrowser =
            Intent(Intent.ACTION_VIEW, Uri.parse("https://dashboard.stripe.com/login"))
        startActivity(launchBrowser)
    }

    private fun initView(v: View) {
        mBottomNavigation = v.findViewById(R.id.navigation)
        mBottomNavigation.setOnNavigationItemSelectedListener(this)
        refreshView(app.currentPage)
    }

    /*override fun onResume() {
        super.onResume()
        activity?.apply {
            (this as AppCompatActivity).supportActionBar?.setTitle(R.string.smoxtalk)
            chatBroadcastReceiver = ChatBroadcastReceiver()
            LocalBroadcastManager.getInstance(this).registerReceiver(chatBroadcastReceiver,
                IntentFilter(ACTION_CHAT_EVENT)
            )

        }
    }*/
    /*override fun onPause() {
        super.onPause()
        activity?.apply {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(chatBroadcastReceiver)
        }
    }*/

    private inner class ChatBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            /*val qbChatDialog: QBChatDialog = intent.getSerializableExtra(EXTRA_DIALOG_ID) as QBChatDialog
            val openChatIntent = Intent(ACTION_OPEN_CHAT_EVENT)
            openChatIntent.putExtra(EXTRA_DIALOG_ID, qbChatDialog)
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(openChatIntent)*/
            val qbChatDialog: QBChatDialog =
                intent.getSerializableExtra(EXTRA_DIALOG_ID) as QBChatDialog
            val fromNotification = intent.getSerializableExtra(FROM_NOTIFICATION)
            var chatIntent = Intent(activity, ChatActivity::class.java)
            chatIntent.putExtra(EXTRA_DIALOG_ID, qbChatDialog)
            chatIntent.putExtra(FROM_NOTIFICATION, fromNotification)
            //startActivity(intent)
            startActivityForResult(chatIntent, REQUEST_DIALOG_ID_FOR_UPDATE)
            activity!!.overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)

        }
    }

    override fun onStart() {
        super.onStart()
        activity?.apply {
            (this as AppCompatActivity).supportActionBar?.setTitle(R.string.smoxtalk)
            chatBroadcastReceiver = ChatBroadcastReceiver()
            LocalBroadcastManager.getInstance(this).registerReceiver(
                chatBroadcastReceiver,
                IntentFilter(ACTION_CHAT_EVENT)
            )

        }
    }

    override fun onStop() {
        super.onStop()
        activity?.apply {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(chatBroadcastReceiver)
        }
    }

}// Required empty public constructor
