package com.smox.smoxuser.utils.managers

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.quickblox.chat.QBChatService
import com.quickblox.chat.QBSystemMessagesManager
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.request.QBPagedRequestBuilder
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import com.smox.smoxuser.App
import com.smox.smoxuser.R
import com.smox.smoxuser.utils.chat.ChatHelper
import com.smox.smoxuser.utils.qb.QbDialogHolder
import com.smox.smoxuser.utils.qb.QbUsersHolder
import com.smox.smoxuser.utils.qb.callback.QbEntityCallbackImpl
import com.smox.smoxuser.utils.qb.callback.QbEntityCallbackWrapper
import com.smox.smoxuser.utils.qb.getOccupantsIdsStringFromList
import com.smox.smoxuser.utils.qb.getOccupantsNamesStringFromList
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smackx.muc.DiscussionHistory
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.collections.ArrayList

const val PROPERTY_OCCUPANTS_IDS = "current_occupant_ids"
const val PROPERTY_DIALOG_TYPE = "type"
const val PROPERTY_DIALOG_NAME = "room_name"
const val PROPERTY_NOTIFICATION_TYPE = "notification_type"
const val CREATING_DIALOG = "1"
const val OCCUPANTS_ADDED = "2"
const val OCCUPANT_LEFT = "3"
const val PROPERTY_NEW_OCCUPANTS_IDS = "new_occupants_ids"

class DialogsManager {
    private val TAG = DialogsManager::class.java.simpleName

    private val managingDialogsCallbackListener = CopyOnWriteArraySet<ManagingDialogsCallbacks>()

    private fun isMessageCreatedDialog(message: QBChatMessage): Boolean {
        return CREATING_DIALOG == message.getProperty(PROPERTY_NOTIFICATION_TYPE)
    }

    private fun isMessageAddedUser(message: QBChatMessage): Boolean {
        return OCCUPANTS_ADDED == message.getProperty(PROPERTY_NOTIFICATION_TYPE)
    }

    private fun isMessageLeftUser(message: QBChatMessage): Boolean {
        return OCCUPANT_LEFT == message.getProperty(PROPERTY_NOTIFICATION_TYPE)
    }

    private fun buildMessageCreatedGroupDialog(
        dialog: QBChatDialog,
        userNames: String
    ): QBChatMessage {
        val qbChatMessage = QBChatMessage()
        qbChatMessage.dialogId = dialog.dialogId
        qbChatMessage.setProperty(
            PROPERTY_OCCUPANTS_IDS,
            getOccupantsIdsStringFromList(dialog.occupants)
        )
        qbChatMessage.setProperty(PROPERTY_DIALOG_TYPE, dialog.type.code.toString())
        qbChatMessage.setProperty(PROPERTY_DIALOG_NAME, dialog.name.toString())
        qbChatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, CREATING_DIALOG)
        qbChatMessage.dateSent = System.currentTimeMillis() / 1000
        //qbChatMessage.body = App.instance.getString(R.string.new_chat_created, getCurrentUserName())
        qbChatMessage.body =
            App.instance.getString(R.string.created_new_dialog, getCurrentUserName(), userNames)
        qbChatMessage.setSaveToHistory(true)
        qbChatMessage.isMarkable = true
        return qbChatMessage
    }

    private fun buildMessageAddedUsers(
        dialog: QBChatDialog,
        userIds: String,
        usersNames: String
    ): QBChatMessage {
        val qbChatMessage = QBChatMessage()
        qbChatMessage.dialogId = dialog.dialogId
        qbChatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, OCCUPANTS_ADDED)
        qbChatMessage.setProperty(PROPERTY_NEW_OCCUPANTS_IDS, userIds)
        qbChatMessage.body =
            App.instance.getString(R.string.occupant_added, getCurrentUserName(), usersNames)
        qbChatMessage.setSaveToHistory(true)
        qbChatMessage.isMarkable = true
        return qbChatMessage
    }

    private fun buildMessageLeftUser(dialog: QBChatDialog): QBChatMessage {
        val qbChatMessage = QBChatMessage()
        qbChatMessage.dialogId = dialog.dialogId
        qbChatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, OCCUPANT_LEFT)
        qbChatMessage.body = App.instance.getString(R.string.occupant_left, getCurrentUserName())
        qbChatMessage.setSaveToHistory(true)
        qbChatMessage.isMarkable = true
        return qbChatMessage
    }

    private fun buildChatDialogFromNotificationMessage(qbChatMessage: QBChatMessage): QBChatDialog {
        val chatDialog = QBChatDialog()
        chatDialog.dialogId = qbChatMessage.dialogId
        chatDialog.unreadMessageCount = 0
        return chatDialog
    }

    private fun getCurrentUserName(): String {
        //val currentUser = QBChatService.getInstance().user
        val currentUser = ChatHelper.getCurrentUser()
        if (currentUser != null) {
            return if (TextUtils.isEmpty(currentUser.fullName)) currentUser.login else currentUser.fullName
        }

        return ""
    }

    ////// Sending Notification Messages

    fun sendMessageCreatedDialog(dialog: QBChatDialog, userNames: String) {
        val messageCreatingDialog = buildMessageCreatedGroupDialog(dialog, userNames)
        try {
            Log.d(TAG, "Sending Notification Message about Creating Group Dialog")
            dialog.sendMessage(messageCreatingDialog)
        } catch (ignored: SmackException.NotConnectedException) {
            Log.i("-GroupChat-", ignored.message!!)
        }
    }

    fun sendMessageAddedUsers(dialog: QBChatDialog, newUsersIds: List<Int>) {
        QBUsers.getUsersByIDs(newUsersIds, null)
            .performAsync(object : QBEntityCallback<ArrayList<QBUser>> {
                override fun onSuccess(qbUsers: ArrayList<QBUser>, bundle: Bundle) {
                    val usersIds = getOccupantsIdsStringFromList(newUsersIds)
                    if (newUsersIds.isNotEmpty()) {
                        val usersNames = getOccupantsNamesStringFromList(qbUsers)
                        val messageUsersAdded = buildMessageAddedUsers(dialog, usersIds, usersNames)

                        try {
                            Log.d(
                                TAG,
                                "Sending Notification Message to Opponents about Adding Occupants"
                            )
                            dialog.sendMessage(messageUsersAdded)
                        } catch (e: SmackException.NotConnectedException) {
                            Log.d(TAG, "Sending Notification Message Error: " + e.message)
                        }
                    }
                }

                override fun onError(ignored: QBResponseException) {

                }
            })
    }

    fun sendMessageLeftUser(dialog: QBChatDialog) {
        val messageLeftUser = buildMessageLeftUser(dialog)
        try {
            Log.d(TAG, "Sending Notification Message to Opponents about User Left")
            dialog.sendMessage(messageLeftUser)
        } catch (ignored: SmackException.NotConnectedException) {

        }
    }

    ////// Sending System Messages

    fun sendSystemMessageAboutCreatingDialog(
        systemMessagesManager: QBSystemMessagesManager?,
        dialog: QBChatDialog
    ) {

        QBUsers.getUsersByIDs(dialog.occupants, null)
            .performAsync(object : QBEntityCallback<ArrayList<QBUser>> {
                override fun onSuccess(qbUsers: ArrayList<QBUser>, bundle: Bundle) {
                    val usersNames = getOccupantsNamesStringFromList(qbUsers)
                    val messageCreatingDialog = buildMessageCreatedGroupDialog(dialog, usersNames)
                    prepareSystemMessage(
                        systemMessagesManager,
                        messageCreatingDialog,
                        dialog.occupants
                    )
                }

                override fun onError(ignored: QBResponseException) {

                }
            })

        /*val messageCreatingDialog = buildMessageCreatedGroupDialog(dialog)
        prepareSystemMessage(systemMessagesManager, messageCreatingDialog, dialog.occupants)*/
    }

    fun sendSystemMessageAddedUser(
        systemMessagesManager: QBSystemMessagesManager,
        dialog: QBChatDialog,
        newUsersIds: List<Int>
    ) {
        QBUsers.getUsersByIDs(newUsersIds, null)
            .performAsync(object : QBEntityCallback<ArrayList<QBUser>> {
                override fun onSuccess(qbUsers: ArrayList<QBUser>, bundle: Bundle) {
                    val usersIds = getOccupantsIdsStringFromList(newUsersIds)
                    if (newUsersIds.isNotEmpty()) {
                        val usersNames = getOccupantsNamesStringFromList(qbUsers)

                        val messageUsersAdded = buildMessageAddedUsers(dialog, usersIds, usersNames)
                        prepareSystemMessage(
                            systemMessagesManager,
                            messageUsersAdded,
                            dialog.occupants
                        )

                        val messageDialogCreated =
                            buildMessageCreatedGroupDialog(dialog, usersNames)
                        prepareSystemMessage(
                            systemMessagesManager,
                            messageDialogCreated,
                            newUsersIds
                        )
                    }
                }

                override fun onError(ignored: QBResponseException) {

                }
            })
    }

    fun sendSystemMessageLeftUser(
        systemMessagesManager: QBSystemMessagesManager,
        dialog: QBChatDialog
    ) {
        val messageLeftUser = buildMessageLeftUser(dialog)
        prepareSystemMessage(systemMessagesManager, messageLeftUser, dialog.occupants)
    }

    private fun prepareSystemMessage(
        systemMessagesManager: QBSystemMessagesManager?,
        message: QBChatMessage,
        occupants: List<Int>
    ) {
        message.setSaveToHistory(false)
        message.isMarkable = false

        try {
            for (opponentID in occupants) {
                //if (opponentID != QBChatService.getInstance().user.id) {
                if (opponentID != ChatHelper.getCurrentUser()?.id) {
                    message.recipientId = opponentID
                    Log.d(TAG, "Sending System Message to $opponentID")
                    systemMessagesManager?.sendSystemMessage(message)
                }
            }
        } catch (e: SmackException.NotConnectedException) {
            Log.d(TAG, "Sending System Message Error: " + e.message)
            e.printStackTrace()
        }
    }

    ////// Message Receivers

    fun onGlobalMessageReceived(dialogId: String, chatMessage: QBChatMessage) {
        Log.d(TAG, "Global Message Received: " + chatMessage.id)
        val sender = QbUsersHolder.getUserById(chatMessage.senderId!!)
        var fullName = ""
        if (!TextUtils.isEmpty(sender?.fullName)) {
            fullName = sender!!.fullName
            notificationData(fullName, chatMessage, dialogId)
        } else {
            val senderIds = ArrayList<Int>()
            senderIds.add(chatMessage.senderId!!)
            //val requestBuilder = QBPagedRequestBuilder(senderIds.size, 1)
            /*QBUsers.getUsersByIDs(senderIds, requestBuilder, object : QBEntityCallback<ArrayList<QBUser>> {

            })*/
            /*QBUsers.getUsersByIDs(senderIds, requestBuilder).performAsync(
                object : QbEntityCallbackWrapper<ArrayList<QBUser>>(callback) {
                    override fun onSuccess(usersList: ArrayList<QBUser>, bundle: Bundle?) {
                        QbUsersHolder.putUsers(usersList)
                        callback.onSuccess(usersList, bundle)
                    }
                })*/

            getUsersFromDialog(senderIds, object : QBEntityCallback<ArrayList<QBUser>> {
                override fun onSuccess(users: ArrayList<QBUser>, bundle: Bundle?) {
                    val senderName = users.get(0).fullName
                    notificationData(senderName, chatMessage, dialogId)
                }

                override fun onError(e: QBResponseException) {
                    Log.i("QBUserError:", e.message!!)
                    //showErrorSnackbar(R.string.chat_load_users_error, e, View.OnClickListener { loadDialogUsers() })
                }
            })
        }
        /*val senderName = fullName
        Log.i("-QBNotify-", "SN:$senderName")
        //val senderName = getSenderName(chatMessage)
        Log.i("-QBNotify-","Started")
        if (isMessageCreatedDialog(chatMessage) && !QbDialogHolder.hasDialogWithId(dialogId)) {
            loadNewDialogByNotificationMessage(chatMessage)
        }

        if (!TextUtils.isEmpty(sender?.fullName)) {
            fullName = sender!!.fullName
        }
        val senderName2 = fullName
        Log.i("-QBNotify-", "SN2:$senderName2")
        if (isMessageAddedUser(chatMessage) || isMessageLeftUser(chatMessage)) {
            if (QbDialogHolder.hasDialogWithId(dialogId)) {
                notifyListenersDialogUpdated(dialogId, senderName2)
            } else {
                loadNewDialogByNotificationMessage(chatMessage)
            }
        }

        if (chatMessage.isMarkable) {
            if (QbDialogHolder.hasDialogWithId(dialogId)) {
                QbDialogHolder.updateDialog(dialogId, chatMessage)
                notifyListenersDialogUpdated(dialogId, senderName2)
            } else {
                ChatHelper.getDialogById(dialogId, object : QBEntityCallback<QBChatDialog> {
                    override fun onSuccess(qbChatDialog: QBChatDialog, bundle: Bundle?) {
                        Log.d(TAG, "Loading Dialog Successful")
                        loadUsersFromDialog(qbChatDialog)
                        QbDialogHolder.addDialog(qbChatDialog)
                        notifyListenersNewDialogLoaded(qbChatDialog, senderName)
                    }

                    override fun onError(e: QBResponseException) {
                        Log.d(TAG, "Loading Dialog Error: " + e.message)
                    }
                })
            }
        }*/
    }

    private fun notificationData(fullName: String, chatMessage: QBChatMessage, dialogId: String) {
        val senderName = fullName

        if (isMessageAddedUser(chatMessage) || isMessageLeftUser(chatMessage)) {
            if (QbDialogHolder.hasDialogWithId(dialogId)) {
                //notifyListenersDialogUpdated(dialogId, senderName)

                //notifyListenersDialogUpdated(chatMessage.body, senderName)
                ChatHelper.getDialogById(dialogId, object : QBEntityCallback<QBChatDialog> {
                    override fun onSuccess(qbChatDialog: QBChatDialog, bundle: Bundle?) {
                        Log.d(TAG, "Loading Dialog Successful")
                        notifyListenersDialogUpdated(
                            chatMessage.body ?: "",
                            senderName,
                            qbChatDialog
                        )
                    }

                    override fun onError(e: QBResponseException) {
                        Log.d(TAG, "Loading Dialog Error: " + e.message)
                    }
                })
            } else {
                loadNewDialogByNotificationMessage(chatMessage)
            }
        }

        if (chatMessage.isMarkable) {
            if (QbDialogHolder.hasDialogWithId(dialogId)) {
                QbDialogHolder.updateDialog(dialogId, chatMessage)
                //notifyListenersDialogUpdated(dialogId, senderName)

                //notifyListenersDialogUpdated(chatMessage.body, senderName)
                ChatHelper.getDialogById(dialogId, object : QBEntityCallback<QBChatDialog> {
                    override fun onSuccess(qbChatDialog: QBChatDialog, bundle: Bundle?) {
                        Log.d(TAG, "Loading Dialog Successful")
                        notifyListenersDialogUpdated(
                            chatMessage.body ?: "",
                            senderName,
                            qbChatDialog
                        )
                    }

                    override fun onError(e: QBResponseException) {
                        Log.d(TAG, "Loading Dialog Error: " + e.message)
                    }
                })

            } else {
                ChatHelper.getDialogById(dialogId, object : QBEntityCallback<QBChatDialog> {
                    override fun onSuccess(qbChatDialog: QBChatDialog, bundle: Bundle?) {
                        Log.d(TAG, "Loading Dialog Successful")
                        loadUsersFromDialog(qbChatDialog)
                        QbDialogHolder.addDialog(qbChatDialog)
                        notifyListenersNewDialogLoaded(qbChatDialog, senderName)
                    }

                    override fun onError(e: QBResponseException) {
                        Log.d(TAG, "Loading Dialog Error: " + e.message)
                    }
                })
            }
        }
    }

    fun getUsersFromDialog(
        senderIds: ArrayList<Int>,
        callback: QBEntityCallback<java.util.ArrayList<QBUser>>
    ) {
        val requestBuilder = QBPagedRequestBuilder(senderIds.size, 1)
        QBUsers.getUsersByIDs(senderIds, requestBuilder).performAsync(
            object : QbEntityCallbackWrapper<java.util.ArrayList<QBUser>>(callback) {
                override fun onSuccess(usersList: java.util.ArrayList<QBUser>, bundle: Bundle?) {
                    //QbUsersHolder.putUsers(usersList)
                    callback.onSuccess(usersList, bundle)
                }
            })
    }

    fun onSystemMessageReceived(systemMessage: QBChatMessage) {
        Log.d(
            TAG,
            "System Message Received: " + systemMessage.body + " Notification Type: " + systemMessage.getProperty(
                PROPERTY_NOTIFICATION_TYPE
            )
        );
        onGlobalMessageReceived(systemMessage.dialogId, systemMessage)
    }

    ////// END

    private fun loadNewDialogByNotificationMessage(chatMessage: QBChatMessage) {
        val sender = QbUsersHolder.getUserById(chatMessage.senderId!!)
        var fullName = ""
        if (!TextUtils.isEmpty(sender?.fullName)) {
            fullName = sender!!.fullName
            createChatDialog(fullName, chatMessage)
        } else {
            val senderIds = ArrayList<Int>()
            senderIds.add(chatMessage.senderId!!)
            //val requestBuilder = QBPagedRequestBuilder(senderIds.size, 1)
            /*QBUsers.getUsersByIDs(senderIds, requestBuilder, object : QBEntityCallback<ArrayList<QBUser>> {

            })*/
            /*QBUsers.getUsersByIDs(senderIds, requestBuilder).performAsync(
                object : QbEntityCallbackWrapper<ArrayList<QBUser>>(callback) {
                    override fun onSuccess(usersList: ArrayList<QBUser>, bundle: Bundle?) {
                        QbUsersHolder.putUsers(usersList)
                        callback.onSuccess(usersList, bundle)
                    }
                })*/

            getUsersFromDialog(senderIds, object : QBEntityCallback<ArrayList<QBUser>> {
                override fun onSuccess(users: ArrayList<QBUser>, bundle: Bundle?) {
                    val senderName = users.get(0).fullName
                    createChatDialog(senderName, chatMessage)
                }

                override fun onError(e: QBResponseException) {
                    Log.i("QBUserError:", e.message!!)
                    //showErrorSnackbar(R.string.chat_load_users_error, e, View.OnClickListener { loadDialogUsers() })
                }
            })
        }
        //val senderName = getSenderName(chatMessage)
        /*val chatDialog = buildChatDialogFromNotificationMessage(chatMessage)
        ChatHelper.getDialogById(chatDialog.dialogId, object : QBEntityCallback<QBChatDialog> {
            override fun onSuccess(qbChatDialog: QBChatDialog, bundle: Bundle) {
                val history = DiscussionHistory()
                history.maxStanzas = 0

                qbChatDialog.initForChat(QBChatService.getInstance())
                qbChatDialog.join(history, object : QbEntityCallbackImpl<QBChatDialog?>() {
                    override fun onSuccess(result: QBChatDialog?, bundle: Bundle?) {
                        QbDialogHolder.addDialog(qbChatDialog)
                        notifyListenersDialogCreated(qbChatDialog, senderName)
                    }
                })
            }

            override fun onError(e: QBResponseException) {
                Log.d(TAG, "Loading Dialog Error: " + e.message)
            }
        })*/
    }

    private fun createChatDialog(senderName: String, chatMessage: QBChatMessage) {
        val chatDialog = buildChatDialogFromNotificationMessage(chatMessage)
        ChatHelper.getDialogById(chatDialog.dialogId, object : QBEntityCallback<QBChatDialog> {
            override fun onSuccess(qbChatDialog: QBChatDialog, bundle: Bundle) {
                val history = DiscussionHistory()
                history.maxStanzas = 0

                QBChatService.setDefaultPacketReplyTimeout(30000)
                QBChatService.setDefaultConnectionTimeout(30000)
                qbChatDialog.initForChat(QBChatService.getInstance())
                qbChatDialog.join(history, object : QbEntityCallbackImpl<QBChatDialog?>() {
                    override fun onSuccess(result: QBChatDialog?, bundle: Bundle?) {
                        QbDialogHolder.addDialog(qbChatDialog)
                        notifyListenersDialogCreated(qbChatDialog, senderName)
                    }
                })
            }

            override fun onError(e: QBResponseException) {
                Log.d(TAG, "Loading Dialog Error: " + e.message)
            }
        })
    }

    private fun loadUsersFromDialog(chatDialog: QBChatDialog) {
        ChatHelper.getUsersFromDialog(chatDialog, QbEntityCallbackImpl())
    }

    private fun notifyListenersDialogCreated(chatDialog: QBChatDialog, senderName: String) {
        for (listener in getManagingDialogsCallbackListeners()) {
            listener.onDialogCreated(chatDialog, senderName)
        }
    }

    private fun notifyListenersDialogUpdated(
        dialogId: String,
        senderName: String,
        chatDialog: QBChatDialog
    ) {
        for (listener in getManagingDialogsCallbackListeners()) {
            listener.onDialogUpdated(dialogId, senderName, chatDialog)
        }
    }

    private fun notifyListenersNewDialogLoaded(chatDialog: QBChatDialog, senderName: String) {
        for (listener in getManagingDialogsCallbackListeners()) {
            listener.onNewDialogLoaded(chatDialog, senderName)
        }
    }

    fun addManagingDialogsCallbackListener(listener: ManagingDialogsCallbacks?) {
        if (listener != null) {
            managingDialogsCallbackListener.add(listener)
        }
    }

    fun removeManagingDialogsCallbackListener(listener: ManagingDialogsCallbacks) {
        managingDialogsCallbackListener.remove(listener)
    }

    fun getManagingDialogsCallbackListeners(): Collection<ManagingDialogsCallbacks> {
        return Collections.unmodifiableCollection<ManagingDialogsCallbacks>(
            managingDialogsCallbackListener
        )
    }

    interface ManagingDialogsCallbacks {

        fun onDialogCreated(chatDialog: QBChatDialog, senderName: String)

        fun onDialogUpdated(chatMessage: String, senderName: String, chatDialog: QBChatDialog)

        fun onNewDialogLoaded(chatDialog: QBChatDialog, senderName: String)
    }

    private fun getSenderName(chatMessage: QBChatMessage): String {
        val sender = QbUsersHolder.getUserById(chatMessage.senderId!!)
        var fullName = ""
        if (!TextUtils.isEmpty(sender?.fullName)) {
            fullName = sender!!.fullName
        }
        return fullName
    }
}