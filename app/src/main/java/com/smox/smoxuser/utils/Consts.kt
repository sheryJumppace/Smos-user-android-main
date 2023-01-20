package com.smox.smoxuser.utils

import com.smox.smoxuser.manager.Constants
import java.net.InetAddress
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.*

const val EXTRA_FCM_MESSAGE: String = "message"
const val ACTION_NEW_FCM_EVENT: String = "new-push-event"
const val ACTION_CHAT_EVENT: String = "chat-event"
const val ACTION_OPEN_CHAT_EVENT: String = "open-chat-event"
const val ACTION_REFRESH_CHAT: String = "refresh-chat-event"
const val ACTION_BILLING_CONNECT: String = "billing-connect-event"
const val ACTION_SUBSCRIPTION_PROCESS: String = "subscription-process-event"
const val ACTION_FETCH_SUBSCRIPTION: String = "subscription-fetch-event"

const val FULL_IMAGE_PATH: String = "https://smoxtrimsetters.com/image/users/"

val PREFERRED_IMAGE_SIZE_FULL = dpToPx(320)


fun currentDate(): String {

    val completedDate = Date()

    val dateFormat = SimpleDateFormat(Constants.KDateFormatter.server, Locale.getDefault())
    return Constants.convertLocalToUTC(completedDate, dateFormat)
}
fun getIPAddress(useIPv4: Boolean): String {
    try {
        val interfaces: List<NetworkInterface> =
            Collections.list(NetworkInterface.getNetworkInterfaces())
        for (intf in interfaces) {
            val addrs: List<InetAddress> = Collections.list(intf.inetAddresses)
            for (addr in addrs) {
                if (!addr.isLoopbackAddress) {
                    val sAddr: String = addr.hostAddress
                    //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                    val isIPv4 = sAddr.indexOf(':') < 0
                    if (useIPv4) {
                        if (isIPv4) return sAddr
                    } else {
                        if (!isIPv4) {
                            val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                            return if (delim < 0) sAddr.uppercase(Locale.getDefault()) else sAddr.substring(
                                0,
                                delim
                            ).uppercase(
                                Locale.getDefault()
                            )
                        }
                    }
                }
            }
        }
    } catch (ex: Exception) {
    } // for now eat exceptions
    return ""
}