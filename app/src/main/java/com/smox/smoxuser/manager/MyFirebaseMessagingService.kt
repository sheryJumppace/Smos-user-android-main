package com.smox.smoxuser.manager

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioAttributes
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.smox.smoxuser.App
import com.smox.smoxuser.PushNotificationUtils
import com.smox.smoxuser.model.Verify
import com.smox.smoxuser.ui.activity.HomeActivity
import com.smox.smoxuser.ui.activity.SplashActivity
import com.smox.smoxuser.ui.activity.barber.AccountVerificationActivity
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private enum class PushType {
        UnKnown,
        Appointment,
        Review,
        Account,
        Payment,
        Event
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        /*Log.e("Notification data:- ", "Remote data notification: ${remoteMessage.notification}")
        Log.e("Notification data:- ", "Remote data data: data ${remoteMessage.data}")

        if (remoteMessage.data.isNotEmpty()) {
            Log.e("Notification data:- ", "Remote data:- ${remoteMessage.data.toString()}")
            sendNotification(true, remoteMessage)
        } else
            sendNotification(false, remoteMessage)*/

        val title: String?
        val message: String?
        if (remoteMessage.notification != null) {
            title = remoteMessage.notification!!.title
            message = remoteMessage.notification!!.body
            Log.e(TAG, "Notification Body: " + remoteMessage.notification!!.body!!)
        } else {
            title = remoteMessage.data["title"]
            message = remoteMessage.data["message"]
            Log.e(TAG, "QB Notification Body: " + remoteMessage.data)
        }
        if (title == null || message == null) {
            return
        }
        Log.e(
            "Notification data:- ",
            "Title :- $title \t Message:- $message \n Remote data:- ${remoteMessage.data.toString()}"
        )

        handleNotification(title, message, remoteMessage.data)

    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        SessionManager.getInstance(applicationContext).deviceToken = token
    }

  /*  private fun sendNotification(isFromNotification: Boolean, messageBody: RemoteMessage) {

        val intent = Intent(this, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val appointId = messageBody.data["appointment_id"]

        if (!appointId.isNullOrEmpty()) {
            intent.putExtra(Constants.API.PUT_EXTRA_IS_FROM_NOTIFICATION, true)
            intent.putExtra(Constants.API.APPOINT_ID, appointId.toInt())
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0 , intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val NOTIFICATION_COLOR = resources.getColor(R.color.colorPrimary)
        val VIBRATE_PATTERN = longArrayOf(0, 500)

        var bodyMessage: String? = ""
        var titleMessage: String? = ""

        if (isFromNotification) {
            bodyMessage = messageBody.notification?.body
            titleMessage = messageBody.notification?.title
        } else {
            bodyMessage = messageBody.data["message"]
            titleMessage = messageBody.data["title"]
        }

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(bodyMessage)
            .setContentText(titleMessage)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.setSound(defaultSoundUri, createAudioAttributes())
            channel.lightColor = NOTIFICATION_COLOR
            channel.vibrationPattern = VIBRATE_PATTERN
            channel.enableVibration(true)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0 , notificationBuilder.build())
    }
*/

    private fun createAudioAttributes(): AudioAttributes? {
        return AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()
    }

    private fun handleNotification(title: String, message: String, data: Map<String, String>) {

        if (data.isNotEmpty()) {
            if (data["message"] != null) {
                showNotificationMessage("", message, Intent(this, HomeActivity::class.java))
                return
            }
            try {
                val json = JSONObject(data.toString())
                if (json.has("type")) {
                    //Log.e(TAG, "type of notification ${json.getString("type")}")
                    when (json.getString("type")) {
                        PushType.Appointment.name.toLowerCase() -> {
                            val appointmentId = json.getInt("appointment_id")
                            val resultIntent =
                                Intent(applicationContext, SplashActivity::class.java)
                            resultIntent.putExtra(Constants.API.APPOINT_ID, appointmentId)
                            resultIntent.putExtra(Constants.API.PUT_EXTRA_IS_FROM_NOTIFICATION, true)
                           // Log.e(TAG, "Title:- $title , Message :- $message")
                            showNotificationMessage(title, message, resultIntent)
                            return
                        }
                        PushType.Review.name.toLowerCase() -> {
//                            val reviewId = json.getInt("review_id")
                            val resultIntent =
                                Intent(applicationContext, SplashActivity::class.java)
                            resultIntent.putExtra("barber_id", App.instance.currentUser.id)
                            showNotificationMessage(title, message, resultIntent)
                            return
                        }
                        PushType.Account.name.toLowerCase() -> {
                            val isVerified = json.getBoolean("is_verified")
                            if (isVerified) {
                                val builder = AlertDialog.Builder(applicationContext)
                                builder.setTitle("Congratulations!")
                                builder.setMessage("Your account has been approved successfully.")
                                builder.setPositiveButton("OK", null)
                                builder.show()
                            } else {
                                val verify = Verify()
                                verify.requiredFields = json.getString("fields")
                                verify.accountToken = json.getString("account_token")

                                val resultIntent = Intent(
                                    applicationContext,
                                    AccountVerificationActivity::class.java
                                )
                                resultIntent.putExtra("verify", verify)
                                showNotificationMessage(title, message, resultIntent)
                            }

                            return
                        }
                        PushType.Payment.name.toLowerCase() -> {
                            val appointmentId = json.getInt("appointment_id")
                            val resultIntent =
                                Intent(applicationContext, SplashActivity::class.java)
                            resultIntent.putExtra("appoint_id", appointmentId)
                            showNotificationMessage(title, message, resultIntent)
                            return
                        }
                        PushType.Event.name.toLowerCase() -> {
//                            val pushNotification = Intent(Constants.KLocalBroadCast.event)
//                            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification)
                            val unReadCount = App.instance.unreadEvents + 1
                            App.instance.unreadEvents = unReadCount

                            ObservingService.getInstance()
                                .postNotification(Constants.KLocalBroadCast.event, "")

                            val resultIntent =
                                Intent(applicationContext, SplashActivity::class.java)
                            showNotificationMessage(title, message, resultIntent)
                            return
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception: " + e.message)
            }

        }else{
            showNotificationMessage(title, message, Intent(this, HomeActivity::class.java))
        }

        showNotificationMessage(title, message, Intent(this, HomeActivity::class.java))
    }

    @SuppressLint("SimpleDateFormat")
    private fun showNotificationMessage(
        title: String,
        message: String,
        intent: Intent
    ) {
        val c = Calendar.getInstance()
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val formattedDate = df.format(c.time)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        PushNotificationUtils(applicationContext)
            .showNotificationMessage(title, message, formattedDate, intent)
    }

    companion object {
        private val TAG = "MyFirebaseMsgService"
    }
}
