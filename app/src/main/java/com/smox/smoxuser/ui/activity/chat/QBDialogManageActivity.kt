package com.smox.smoxuser.ui.activity.chat

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.kaopiz.kprogresshud.KProgressHUD
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
import com.smox.smoxuser.App
import com.smox.smoxuser.PushNotificationUtils
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.ui.activity.barber.BarberMainActivity
import com.smox.smoxuser.utils.ACTION_NEW_FCM_EVENT
import com.smox.smoxuser.utils.EXTRA_FCM_MESSAGE
import com.smox.smoxuser.utils.chat.ChatHelper
import com.smox.smoxuser.utils.managers.DialogsManager
import com.smox.smoxuser.utils.qb.QbChatDialogMessageListenerImpl
import com.smox.smoxuser.utils.qb.QbDialogHolder
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

open class QBDialogManageActivity : AppCompatActivity(), DialogsManager.ManagingDialogsCallbacks {

    private lateinit var requestBuilder: QBRequestGetBuilder
    private var skipRecords = 0
    private var isProcessingResultInProgress: Boolean = false
    private lateinit var pushBroadcastReceiver: BroadcastReceiver
    private var incomingMessagesManager: QBIncomingMessagesManager? = null
    private lateinit var allDialogsMessagesListener: QBChatDialogMessageListener
    private lateinit var dialogsManager: DialogsManager
    private lateinit var systemMessagesListener: SystemMessagesListener
    private var systemMessagesManager: QBSystemMessagesManager? = null

    //private lateinit var systemMessagesManager: QBSystemMessagesManager
    private val TAG = QBDialogManageActivity::class.java.simpleName
    protected lateinit var progressHUD: KProgressHUD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressHUD = KProgressHUD(this)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        initQB()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterQbChatListeners()
    }

    override fun onDialogCreated(chatDialog: QBChatDialog, senderName: String) {
        chatDialog.lastMessage?.apply {
            didReceivedMessage(this, senderName, chatDialog)
        }
    }

    override fun onDialogUpdated(
        chatMessage: String,
        senderName: String,
        chatDialog: QBChatDialog
    ) {
        didReceivedMessage(chatMessage, senderName, chatDialog)
    }

    override fun onNewDialogLoaded(chatDialog: QBChatDialog, senderName: String) {
        chatDialog.lastMessage?.apply {
            didReceivedMessage(this, senderName, chatDialog)
        }
    }

    fun initQB() {
        requestBuilder = QBRequestGetBuilder()
        pushBroadcastReceiver = PushBroadcastReceiver()
        allDialogsMessagesListener = AllDialogsMessageListener()
        systemMessagesListener = SystemMessagesListener()
        dialogsManager = DialogsManager()

        registerQbChatListeners()
        if (QbDialogHolder.dialogsMap.isNotEmpty()) {
            loadDialogsFromQb(silentUpdate = true, clearDialogHolder = true)
        } else {
            loadDialogsFromQb(silentUpdate = false, clearDialogHolder = true)
        }
    }

    private fun loadDialogsFromQb(silentUpdate: Boolean, clearDialogHolder: Boolean) {
        isProcessingResultInProgress = true
        if (!silentUpdate) {
            if (!progressHUD.isShowing) {
                //progressHUD.show()
            }
        }

        ChatHelper.getDialogs(requestBuilder, object : QBEntityCallback<ArrayList<QBChatDialog>> {
            override fun onSuccess(dialogs: ArrayList<QBChatDialog>, bundle: Bundle) {
                val dialogJoinerAsyncTask =
                    DialogAsyncTask(
                        this@QBDialogManageActivity,
                        dialogs,
                        clearDialogHolder
                    )
                dialogJoinerAsyncTask.execute()
            }

            override fun onError(e: QBResponseException) {
                if (progressHUD.isShowing) {
                    progressHUD.dismiss()
                }
                //Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun registerQbChatListeners() {
        dialogsManager.addManagingDialogsCallbackListener(this)
        incomingMessagesManager = QBChatService.getInstance().incomingMessagesManager
        systemMessagesManager = QBChatService.getInstance().systemMessagesManager
        incomingMessagesManager?.addDialogMessageListener(
            allDialogsMessagesListener
        )
        systemMessagesManager?.addSystemMessageListener(
            systemMessagesListener
        )
    }

    private fun unregisterQbChatListeners() {
        incomingMessagesManager?.removeDialogMessageListrener(allDialogsMessagesListener)
        systemMessagesManager?.removeSystemMessageListener(systemMessagesListener)
        dialogsManager.removeManagingDialogsCallbackListener(this)
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
            dialogID: String,
            qbChatMessage: QBChatMessage,
            senderID: Int?
        ) {
            Log.d("QB", "Processing received Message: " + qbChatMessage.body)
            if (senderID != ChatHelper.getCurrentUser()?.id) {
                dialogsManager.onGlobalMessageReceived(dialogID, qbChatMessage)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun didReceivedMessage(message: String, senderName: String, chatDialog: QBChatDialog) {
        if (App.instance.currentActivity != ChatActivity::class.java) {
            val c = Calendar.getInstance()
            val df = SimpleDateFormat(Constants.KDateFormatter.display)
            val formattedDate = df.format(c.time)
            intent.putExtra(EXTRA_DIALOG_ID, chatDialog)
            intent.putExtra(FROM_NOTIFICATION, true)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            PushNotificationUtils(applicationContext)
                .showNotificationMessage(
                    senderName,
                    message,
                    formattedDate,
                    Intent(this, BarberMainActivity::class.java)
                )
            //intent.putExtra(EXTRA_DIALOG_ID, chatDialog)
            /*var chatIntent = Intent(this,BarberMainActivity::class.java)
            chatIntent.putExtra(EXTRA_DIALOG_ID, chatDialog)
            chatIntent.putExtra(FROM_NOTIFICATION, true)
            PushNotificationUtils(applicationContext)
                .showNotificationMessage(senderName, message, formattedDate, chatIntent)*/
        }
    }

    inner class PushBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            val message = intent.getStringExtra(EXTRA_FCM_MESSAGE)
            Log.v(TAG, "Received broadcast " + intent.action + " with data: " + message)
            skipRecords = 0
            requestBuilder.skip = skipRecords
            loadDialogsFromQb(true, true)
        }
    }

    private class DialogAsyncTask internal constructor(
        dialogsActivity: QBDialogManageActivity,
        private val dialogs: ArrayList<QBChatDialog>,
        private val clearDialogHolder: Boolean
    ) : AsyncTask<Void, Void, Void>() {
        private val activityRef: WeakReference<QBDialogManageActivity> =
            WeakReference<QBDialogManageActivity>(dialogsActivity)

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
            }
        }
    }

    override fun onResume() {
        super.onResume()
        pushBroadcastReceiver = PushBroadcastReceiver()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            pushBroadcastReceiver,
            IntentFilter(ACTION_NEW_FCM_EVENT)
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pushBroadcastReceiver)
    }
}