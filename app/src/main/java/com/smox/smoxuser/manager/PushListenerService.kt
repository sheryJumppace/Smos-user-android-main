package com.smox.smoxuser.manager

import android.util.Log
import com.quickblox.messages.services.fcm.QBFcmPushListenerService
import com.smox.smoxuser.R
import com.smox.smoxuser.ui.activity.HomeActivity
import com.smox.smoxuser.utils.ActivityLifecycle
import com.smox.smoxuser.utils.NotificationUtils

private const val NOTIFICATION_ID = 1

class PushListenerService : QBFcmPushListenerService() {
    private val TAG = PushListenerService::class.java.simpleName

    override fun sendPushMessage(data: MutableMap<Any?, Any?>?, from: String?, message: String?) {
        super.sendPushMessage(data, from, message)
        Log.e(TAG, "QB From: $from")
        Log.e(TAG, "QB Message: $message")

        if (ActivityLifecycle.isBackground()) {
            showNotification(message ?: " ")
        }
    }

    private fun showNotification(message: String) {
        Log.e(TAG, "Notification show")
        NotificationUtils.showNotification(
            this, HomeActivity::class.java,
            getString(R.string.notification_title), message,
            R.drawable.smox_notification_icon, NOTIFICATION_ID
        )
    }
}