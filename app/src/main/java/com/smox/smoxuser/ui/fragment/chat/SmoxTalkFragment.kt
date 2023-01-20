package com.smox.smoxuser.ui.fragment.chat


import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.quickblox.chat.QBChatService
import com.quickblox.chat.QBIncomingMessagesManager
import com.quickblox.chat.QBSystemMessagesManager
import com.quickblox.chat.exception.QBChatException
import com.quickblox.chat.listeners.QBChatDialogMessageListener
import com.quickblox.chat.listeners.QBSystemMessageListener
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.request.QBRequestGetBuilder
import com.quickblox.users.model.QBUser
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.FragmentSmoxTalkBinding
import com.smox.smoxuser.ui.activity.chat.*
import com.smox.smoxuser.ui.fragment.BaseFragment
import com.smox.smoxuser.utils.ACTION_NEW_FCM_EVENT
import com.smox.smoxuser.utils.ACTION_REFRESH_CHAT
import com.smox.smoxuser.utils.EXTRA_FCM_MESSAGE
import com.smox.smoxuser.utils.chat.ChatHelper
import com.smox.smoxuser.utils.managers.DialogsManager
import com.smox.smoxuser.utils.qb.QbChatDialogMessageListenerImpl
import com.smox.smoxuser.utils.qb.QbDialogHolder
import com.smox.smoxuser.utils.qb.callback.QbEntityCallbackImpl
import com.smox.smoxuser.utils.shortToast
import java.lang.ref.WeakReference
import java.util.*


class SmoxTalkFragment : BaseFragment(), DialogsManager.ManagingDialogsCallbacks {

    private val TAG = SmoxTalkFragment::class.java.simpleName
    private val REQUEST_SELECT_PEOPLE = 174
    private val REQUEST_DIALOG_ID_FOR_UPDATE = 165
    val EXTRA_QB_USERS = "qb_users"

    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var currentFragment: Fragment

    private lateinit var requestBuilder: QBRequestGetBuilder
    private var skipRecords = 0
    private var isProcessingResultInProgress: Boolean = false
    private lateinit var pushBroadcastReceiver: BroadcastReceiver

    //////////private lateinit var openChatBroadcastReceiver: BroadcastReceiver
    private lateinit var refreshChatBroadcastReceiver: BroadcastReceiver
    private lateinit var allDialogsMessagesListener: QBChatDialogMessageListener
    private lateinit var systemMessagesListener: SystemMessagesListener
    private var systemMessagesManager: QBSystemMessagesManager? = null
    private var incomingMessagesManager: QBIncomingMessagesManager? = null
    private lateinit var dialogsManager: DialogsManager
    private var currentUser: QBUser? = null

    private var recentFragment: DialogsFragment? = null
    private var groupFragment: DialogsFragment? = null
    private var isNewDialog: Boolean = false
    private var isListRefresh: Boolean = false
    private var chatDialogId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSmoxTalkBinding.inflate(inflater, container, false)
        viewPager = binding.container
        viewPager.adapter = SectionsPagerAdapter(childFragmentManager)
        viewPager.offscreenPageLimit = 4
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {

            }

        })

        tabLayout = binding.tabLayout
        tabLayout.setupWithViewPager(viewPager)

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.apply {
            (this as AppCompatActivity).supportActionBar?.setTitle(R.string.smoxtalk)
            refreshChatBroadcastReceiver = RefreshChatBroadcastReceiver()
            LocalBroadcastManager.getInstance(this).registerReceiver(
                refreshChatBroadcastReceiver,
                IntentFilter(ACTION_REFRESH_CHAT)
            )
        }
    }

    /*override fun onStart() {
        super.onStart()
        activity?.apply {
            (this as AppCompatActivity).supportActionBar?.setTitle(R.string.smoxtalk)

            openChatBroadcastReceiver = OpenChatBroadcastReceiver()
            LocalBroadcastManager.getInstance(this).registerReceiver(openChatBroadcastReceiver,
                IntentFilter(ACTION_OPEN_CHAT_EVENT)
            )

        }
    }*/

    override fun onResume() {
        super.onResume()
        activity?.apply {
            (this as AppCompatActivity).supportActionBar?.setTitle(R.string.smoxtalk)

            pushBroadcastReceiver = PushBroadcastReceiver()
            LocalBroadcastManager.getInstance(this).registerReceiver(
                pushBroadcastReceiver,
                IntentFilter(ACTION_NEW_FCM_EVENT)
            )
            /*openChatBroadcastReceiver = OpenChatBroadcastReceiver()
            LocalBroadcastManager.getInstance(this).registerReceiver(openChatBroadcastReceiver,
                IntentFilter(ACTION_OPEN_CHAT_EVENT)////////////
            )*/


        }
    }

    override fun onPause() {
        super.onPause()
        activity?.apply {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(pushBroadcastReceiver)
            //LocalBroadcastManager.getInstance(this).unregisterReceiver(openChatBroadcastReceiver)

        }
    }

    /*override fun onStop() {
        super.onStop()
        activity?.apply {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshChatBroadcastReceiver)
        }
    }*/

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.apply {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshChatBroadcastReceiver)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_smox_talk, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_create_group -> {
                if (tabLayout.selectedTabPosition == 3) {
                    val intent = Intent(activity, AddContactActivity::class.java)
                    startActivity(intent)
                    activity!!.overridePendingTransition(
                        R.anim.activity_enter,
                        R.anim.activity_exit
                    )
                } else {
                    val intent = Intent(activity, GroupActivity::class.java)
                    startActivityForResult(intent, REQUEST_SELECT_PEOPLE)
                    activity!!.overridePendingTransition(
                        R.anim.activity_enter,
                        R.anim.activity_exit
                    )
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initQB()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterQbChatListeners()
    }

    override fun onDialogCreated(chatDialog: QBChatDialog, senderName: String) {
        updateDialogsAdapter()
    }

    override fun onDialogUpdated(
        chatMessage: String,
        senderName: String,
        chatDialog: QBChatDialog
    ) {
        updateDialogsAdapter()
    }

    override fun onNewDialogLoaded(chatDialog: QBChatDialog, senderName: String) {
        updateDialogsAdapter()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            isNewDialog = false
            isProcessingResultInProgress = true
            if (requestCode == REQUEST_SELECT_PEOPLE) {
                val selectedUsers = data!!
                    .getSerializableExtra(EXTRA_QB_USERS) as ArrayList<QBUser>
                var chatName = data.getStringExtra(EXTRA_CHAT_NAME)

                if (isPrivateDialogExist(selectedUsers)) {
                    selectedUsers.remove(ChatHelper.getCurrentUser())
                    val existingPrivateDialog =
                        QbDialogHolder.getPrivateDialogWithUser(selectedUsers[0])
                    isProcessingResultInProgress = false
                    if (existingPrivateDialog != null) {
                        openChatPage(existingPrivateDialog)
                    }
                } else {
                    isNewDialog = true
                    progressHUD.show().setLabel(getString(R.string.create_chat))
                    if (TextUtils.isEmpty(chatName)) {
                        chatName = ""
                    }
                    createDialog(selectedUsers, chatName!!)
                }
            } else if (requestCode == REQUEST_DIALOG_ID_FOR_UPDATE) {
                if (data != null) {
                    val dialogId = data.getStringExtra(EXTRA_DIALOG_ID)
                    loadUpdatedDialog(dialogId!!)
                } else {
                    isProcessingResultInProgress = false
                    updateDialogsList()
                }
            }
        } else {
            updateDialogsAdapter()
        }
    }

    private fun initQB() {

        requestBuilder = QBRequestGetBuilder()
        pushBroadcastReceiver = PushBroadcastReceiver()
        allDialogsMessagesListener = AllDialogsMessageListener()
        systemMessagesListener = SystemMessagesListener()
        dialogsManager = DialogsManager()
        currentUser = ChatHelper.getCurrentUser()

        registerQbChatListeners()
        if (QbDialogHolder.dialogsMap.isNotEmpty()) {
            updateDialogsAdapter()
            loadDialogsFromQb(silentUpdate = true, clearDialogHolder = true)
        } else {
            loadDialogsFromQb(silentUpdate = false, clearDialogHolder = true)
        }
    }

    private fun loadDialogsFromQb(silentUpdate: Boolean, clearDialogHolder: Boolean) {
        isProcessingResultInProgress = true
        if (!silentUpdate) {
            progressHUD.show()
        }

        ChatHelper.getDialogs(requestBuilder, object : QBEntityCallback<ArrayList<QBChatDialog>> {
            override fun onSuccess(dialogs: ArrayList<QBChatDialog>, bundle: Bundle) {
                val dialogJoinerAsyncTask =
                    DialogAsyncTask(
                        this@SmoxTalkFragment,
                        dialogs,
                        clearDialogHolder
                    )
                dialogJoinerAsyncTask.execute()
            }

            override fun onError(e: QBResponseException) {
                progressHUD.dismiss()
                if (!e.message?.contains("base Forbidden. Need user.")!!)
                    shortToast(e.message)
            }
        })
    }

    private fun registerQbChatListeners() {
        incomingMessagesManager = QBChatService.getInstance().incomingMessagesManager
        systemMessagesManager = QBChatService.getInstance().systemMessagesManager

        incomingMessagesManager?.addDialogMessageListener(
            allDialogsMessagesListener
        )

        systemMessagesManager?.addSystemMessageListener(
            systemMessagesListener
        )

        dialogsManager.addManagingDialogsCallbackListener(this)
    }

    private fun unregisterQbChatListeners() {
        incomingMessagesManager?.removeDialogMessageListrener(allDialogsMessagesListener)

        systemMessagesManager?.removeSystemMessageListener(systemMessagesListener)

        dialogsManager.removeManagingDialogsCallbackListener(this)
    }

    private fun updateDialogsAdapter() {
        recentFragment?.updateDialogsAdapter()
        groupFragment?.updateDialogsAdapter()
    }

    private fun loadUpdatedDialog(dialogId: String) {
        ChatHelper.getDialogById(dialogId, object : QbEntityCallbackImpl<QBChatDialog>() {
            override fun onSuccess(result: QBChatDialog, bundle: Bundle?) {
                QbDialogHolder.addDialog(result)
                updateDialogsAdapter()
                isProcessingResultInProgress = false
            }

            override fun onError(e: QBResponseException) {
                isProcessingResultInProgress = false
            }
        })
    }

    private fun updateDialogsList() {
        requestBuilder.skip = 0
        loadDialogsFromQb(true, true)
    }

    private fun isPrivateDialogExist(allSelectedUsers: ArrayList<QBUser>): Boolean {
        val selectedUsers = ArrayList<QBUser>()
        selectedUsers.addAll(allSelectedUsers)
        selectedUsers.remove(ChatHelper.getCurrentUser())
        return selectedUsers.size == 1 && QbDialogHolder.hasPrivateDialogWithUser(selectedUsers[0])
    }

    private fun createDialog(selectedUsers: ArrayList<QBUser>, chatName: String) {
        ChatHelper.createDialogWithSelectedUsers(selectedUsers, chatName,
            object : QBEntityCallback<QBChatDialog> {
                override fun onSuccess(dialog: QBChatDialog, args: Bundle?) {
                    isProcessingResultInProgress = false
                    val newUserIds = ArrayList<Int>()

                    for (user in selectedUsers) {
                        //if (!existingOccupants.contains(user.id)) {
                        newUserIds.add(user.id)
                        //}
                    }
                    //dialogsManager.sendMessageAddedUsers(dialog, newUserIds, true)
                    dialogsManager.sendSystemMessageAboutCreatingDialog(
                        systemMessagesManager,
                        dialog
                    )
                    openChatPage(dialog)
                    progressHUD.dismiss()
                }

                override fun onError(error: QBResponseException) {
                    isProcessingResultInProgress = false
                    progressHUD.dismiss()
                    shortToast(R.string.dialogs_creation_error)
                }
            }
        )
    }

    fun openChatPage(dialog: QBChatDialog) {
        val intent = Intent(activity, ChatActivity::class.java)
        intent.putExtra(EXTRA_DIALOG_ID, dialog)
        intent.putExtra(EXTRA_IS_NEW_DIALOG, isNewDialog)
        startActivityForResult(intent, REQUEST_DIALOG_ID_FOR_UPDATE)
        //ChatActivity.startForResult(activity!!, REQUEST_DIALOG_ID_FOR_UPDATE, dialog, true)
        activity!!.overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
    }

    private inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm),
        DialogsFragment.DialogClickListener {
        override fun onDialogClick(dialog: QBChatDialog) {
            openChatPage(dialog)
        }

        internal var titles = arrayOf("RECENT", "FAVORITES", "GROUP", "CONTACTS")
        override fun getItem(position: Int): Fragment {
            val fragment: Fragment
            when (position) {
                0 -> {
                    fragment = DialogsFragment().newInstance(false)
                    recentFragment = fragment
                    recentFragment?.setDialogClickListener(this)
                }
                2 -> {
                    fragment = DialogsFragment().newInstance(true)
                    groupFragment = fragment
                    groupFragment?.setDialogClickListener(this)
                }
                1 -> {
                    fragment = ContactsFragment().newInstance(true)
                }
                else -> fragment = ContactsFragment().newInstance(false)
            }
            return fragment
        }

        override fun getCount(): Int {
            return titles.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }

    }

    private inner class SystemMessagesListener : QBSystemMessageListener {
        override fun processMessage(qbChatMessage: QBChatMessage) {
            dialogsManager.onSystemMessageReceived(qbChatMessage)
        }

        override fun processError(e: QBChatException, qbChatMessage: QBChatMessage) {

        }
    }

    private inner class AllDialogsMessageListener : QbChatDialogMessageListenerImpl() {
        override fun processMessage(
            dialogId: String,
            qbChatMessage: QBChatMessage,
            senderId: Int?
        ) {
            if (senderId != ChatHelper.getCurrentUser()?.id) {
                //dialogsManager.onGlobalMessageReceived(dialogId, qbChatMessage)
                updateDialogsAdapter()
            }

        }
    }


    private inner class PushBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            val message = intent.getStringExtra(EXTRA_FCM_MESSAGE)
            Log.v(TAG, "Received broadcast " + intent.action + " with data: " + message)
            skipRecords = 0
            requestBuilder.skip = skipRecords
            loadDialogsFromQb(true, true)
        }
    }

    /* private inner class OpenChatBroadcastReceiver : BroadcastReceiver() {
         override fun onReceive(context: Context, intent: Intent) {
             // Get extra data included in the Intent
             activity?.apply {
                 val qbChatDialog: QBChatDialog = intent.getSerializableExtra(EXTRA_DIALOG_ID) as QBChatDialog
                 var intent = Intent(this, ChatActivity::class.java)
                 intent.putExtra(EXTRA_DIALOG_ID, qbChatDialog)
                 startActivity(intent)
                 activity!!.overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
             }


         }
     }
 */
    private inner class RefreshChatBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent

            chatDialogId = intent.getStringExtra(EXTRA_DIALOG_ID)!!
            loadUpdatedDialog(chatDialogId)

        }
    }

    private class DialogAsyncTask internal constructor(
        dialogsActivity: SmoxTalkFragment,
        private val dialogs: ArrayList<QBChatDialog>,
        private val clearDialogHolder: Boolean
    ) : AsyncTask<Void, Void, Void>() {
        private val activityRef: WeakReference<SmoxTalkFragment> =
            WeakReference<SmoxTalkFragment>(dialogsActivity)

        override fun doInBackground(vararg params: Void?): Void? {
            ChatHelper.join(dialogs)
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            if (activityRef.get() != null) {
                activityRef.get()!!.progressHUD.dismiss()
                if (clearDialogHolder) {
                    QbDialogHolder.clear()
                }
                QbDialogHolder.addDialogs(dialogs)
                if (!TextUtils.isEmpty(activityRef.get()!!.chatDialogId)) {
                    activityRef.get()!!.loadUpdatedDialog(activityRef.get()!!.chatDialogId)
                    activityRef.get()!!.chatDialogId = ""
                } else {
                    activityRef.get()!!.updateDialogsAdapter()
                }
            }
        }
    }

}
