package com.smox.smoxuser.manager

import android.webkit.URLUtil
import com.smox.smoxuser.utils.FULL_IMAGE_PATH
import java.text.SimpleDateFormat
import java.util.*

object Constants {
    val imgFullPath = "https://smoxtrimsetters.com/image/users/"

    object KUrl {
        //const val hosting = "http://smoxtrimsetters.com/"
        //const val hosting = "http://192.168.1.134/smox/"
        //const val hosting = "http://192.168.1.84:8000/smox/"
        //const val hosting = "https://smoxtrimsetters.com/smoxdiff/"
        //const val hosting = "http://52.35.193.52/diffsmox/"
        //const val hosting = "http://192.168.1.106/smox/"
        //const val hosting = "http://3.132.177.220/" // Client's Main Server API
        //const val hosting = "https://smoxtrimsetters.com/smox_uat/" // UAT Main Server API
        // const val hosting = "https://smoxtrimsetters.com/" // Client's Main Server API
        //const val server = hosting + "api/"
        //const val hosting = "http://3.135.192.185/"

        //const val hosting = "https://smoxtrimsetters.com/api/v2/" //live
        //const val hosting = "http://13.232.168.249/"   //testing

        //const val hosting = "https://smoxtrimsetters.com/api/v3/" //old live new

        //cart product new live also change live strip payment key, also product page link in detailsActivity
        //const val hosting = "https://smoxtrimsetters.com/api/v4/"

        //const val hosting = "https://smoxtrimsetters.com/api/v4.1/"

        //test server url
        const val hosting = "https://smox.jumppace.com/api/v6/"
//        const val hosting = "https://smoxtrimsetters.com/api/v6/"
//                    const val hosting = "https://smox.jumppace.com/api/v4.2/"

//        const val newHosting = "https://smoxtrimsetters.com/"
//        const val newHosting = "https://smox.jumppace.com/api/v4.2/"
        const val newHosting = "https://smox.jumppace.com/api/v6/"

        const val server = hosting

        const val image = hosting + "image/users/"
        const val product = newHosting + "image/products/"

        //const val terms = hosting + "api/content/terms.html"
        const val terms = hosting + "terms.html"
        const val privacy = hosting + "api/content/privacy.html"
        const val report = hosting + "api/content/reports.html"

        //const val about = hosting + "terms"
        const val about = hosting + "aboutus.html"
        const val faq = hosting + "api/content/faqs.html"
        const val contact = hosting + "api/content/contact.html"
        const val stripe = "https://stripe.com/us/connect-account/legal"
        const val stripeV1API = "https://api.stripe.com/v1/customers"

    }

    object KStripe {
        /*const val publishableKey: String = "pk_test_5asA0dnWs7gY6uxE0BuDwJFB"  // Company's Stripe Ac. Test Key
        const val publishableKey: String = "pk_test_9wyRe5j6MuA1kwWFsMJ08CEI"  // Company's Stripe Ac. Test Key*/

        //var publishableKey: String = "pk_live_mMJiIOrh3B0TB4jOHIvnbs6o00YgTkisAO" // sk_live_HCBemrxRjDNm4IZX8McC3ikg00JimN8sHJ  // Client's Stripe Ac. Test Key
        //const val secretKey: String = "sk_live_HCBemrxRjDNm4IZX8McC3ikg00JimN8sHJ" //Client's account key

        const val publishableKey: String = "pk_test_R8FVqnYwrx1Ytp7NRn5mjCvp00V9AQp9HS"  // Company's Stripe Ac. Test Key
        const val secretKey: String = "sk_test_mJkKySZpMj4zqYo7wPcGw4qD00tuCf6sae"  // Company's Stripe Ac. Test Key
    }

    fun getImagePath(): String {
        return imgFullPath
    }


    fun downloadUrl(fileName: String): String {
        return if (URLUtil.isValidUrl(fileName)) {
            fileName
        } else KUrl.image + fileName
    }

    fun downloadUrlll(fileName: String): String {
        return FULL_IMAGE_PATH + fileName
    }

    fun downloadUrlOfProduct(fileName: String): String {
        return if (URLUtil.isValidUrl(fileName)) {
            fileName
        } else KUrl.product + fileName
    }

    object API {
        const val signUp = "register"
        const val loginWithSocial = "login_with_social_account"
        const val login = "login"
        const val forgot = "forgot"
        const val user_type = "user_type"
        const val appointment = "appointment"
        const val appointment_walkin = "appointment_walkin"
        const val appointment_by_date = "appointment_by_date"
        const val appointment_by_month = "appointment_by_month"
        const val appointment_status = "appointment_status"
        const val appointment_time = "appointment_time"
        const val appointment_timeslots = "appointment_timeslots"
        const val appointment_customer_up_next = "appointment_customer_up_next"
        const val rearrange_appointment = "rearrange_appointment"
        const val smox_talk = "smox_talk"
        const val favorite = "favorite"
        const val users_by_name = "users_by_name"
        const val user_by_phone = "user_by_phone"
        const val contacts = "contacts"
        const val upload_image = "upload_image"
        const val upload_profile = "upload_profile"
        const val phone = "phone"
        const val updateAddress = "user_address"
        const val updateName = "update_name"
        const val password = "password"
        const val contactUs = "contact_us"
        const val addcard = "AddCard"
        const val aboutUs = "about_us"
        const val getlistcard = "getlistcard"
        const val deletecard = "deletecard"
        const val delete_event = "event_delete"
        const val get_category = "get_category"
        const val PUT_EXTRA_IS_FROM_NOTIFICATION = "isFromNoti"
        const val APPOINT_ID = "appoint_id"
        const val CALLED_FROM = "calledFrom"
        const val EVENT_SELECTED = "eventSelected"
        const val IS_DARK_MODE_ON = "isDarkModeOn"
        const val DARK_MODE_SET = "darkModeSet"
        const val IS_DOWNLOAD_POPUP_SHOW = "isDownloadPopupShow"
        const val APPOINTMENT = "appointmentt"
        const val BARBER_ID = "barberId"
        const val PAYMENT_INTENT = "paymentIntent"
        const val APPOINTMENT_ID = "appointmentId"
        const val CART = "Cart"
        const val DETAILS = "details"
        const val QUANTITY = "quantity"
        const val PAY_STATUS = "payStatus"
        const val PAY_MESSAGE = "payMessage"
        const val IS_EDIT = "isEdit"
        const val ADDRESS_ITEM = "addressItem"
        const val IS_BACK = "isBack"


        const val connect_account = "connect_account"
        const val save_key = "update_stripe_detail"
        const val service = "service"
        const val rearrange_service = "rearrange_service"
        const val barber_opening_time = "barber_opening_time"
        const val social_link = "social_link"
        const val user_bio = "user_bio"
        const val address = "address"
        const val review = "review"
        const val register_card = "register_card"
        const val update_card = "update_card"
        const val logout = "logout"

        const val event = "newevent"
        const val event_unread = "event_unread"

        const val barberById = "barber_details"
        const val barberHolidayList = "hours_list"
        const val barbers = "barbers"
        const val barber_address = "barber_address"
        const val user_device = "user_device"
        const val charge = "charge"
        const val update_appointment = "update_appointment"
        const val product = "product"
        const val hours = "hours"
        const val hours_list = "hours_list"

        const val favorite_barber = "favorite_barber"
        const val upnext_option = "upnext_option"
        const val upnext_time = "upnext_time"
        const val send_payment_request = "send_payment_request"
        const val cancellation_fee = "cancellation_fee"
        const val stripe_connect_account = "stripe_connect_account"
        const val addedit_address = "addedit_address"
        const val myaddress = "myaddress"
        const val delete_address = "delete_address"
        const val buy_product = "buy_product"
        const val myOrders = "myOrders"
        const val orders = "orders"
        const val mark_delivered = "mark_delivered"
        const val add_subscription = "add_subscription"
        const val cancelSubscription = "cancelSubscription"

        const val BUCKET_NAME = "smox"
        const val AWS_URL = "https://smox.s3.us-east-2.amazonaws.com/"

        const val F_NAME = "fName"
        const val L_NAME = "lName"
        const val ADD1 = "add1"
        const val ADD2 = "add2"
        const val CITY = "city"
        const val STATE = "state"
        const val COUNTRY = "country"
        const val ZIP = "zip"
        const val PHONE = "phone"

        const val PRODUCT = "MyProduct"
        const val ORDER_ITEM = "orderItem"
        const val MESSAGE_TYPE_IMAGE = 2
        const val MESSAGE_TYPE_TEXT = 1
        const val USER_NAME = "userName"
        const val BARBER_NAME = "barberName"
        const val CHAT_ROOM = "chatRoom"


        //chat
        const val USER_STATUS_TABLE = "User_status"
        const val CHAT_ROOM_TABLE = "chatRoom"
        const val CHATS_TABLE = "chats"
        const val CHAT_ROOM_ID = "chatRoomId"
        const val COL_LAST_MESSAGE = "last_message"
        const val COL_LAST_MESSAGE_TIME = "last_message_time"
        const val COL_SENDER_ID = "sender_id"
        const val MESSAGE_COUNT = "message_count"

        const val ORDER_CANCELLED = "OrderCancelled"
        const val UN_AUTHORISED = "unAuthorised"

        const val PLACE_KEY_1 = "AIzaSyAc"
        const val PLACE_KEY_2 = "T5skfyEGHN3Mn"
        const val PLACE_KEY_3 = "5h3yubSzqx"
        const val PLACE_KEY_4 = "TYO41lUc"
        const val AUTOCOMPLETE_REQUEST_CODE = 1255


    }

    object KDateFormatter {
        const val local: String = "yyyy-MM-dd hh:mm a"
        const val local_full: String = "yyyy-MM-dd HH:mm:ss a"
        const val local_full_time: String = "dd-MM-yyyy hh:mm a"
        const val server: String = "yyyy-MM-dd HH:mm:ss"
        const val serverDay: String = "yyyy-MM-dd"
        const val defaultDate: String = "E, MMM d yyyy"
        const val full: String = "E, MMMM d, yyyy"
        const val displayFull: String = "MMM dd, yyyy"
        const val displayFullTime: String = "MMM dd, yyyy hh:mm a"
        const val display: String = "MMM d"
        const val hourAM: String = "hh:mm a"
        const val hourFull: String = "HH:mm"
        const val hourOnly: String = "hha"
        const val hourOnlySpace: String = "hh a"
        const val hourDetail: String = "MMM d, hh:mm a"
        const val second: String = "HH:mm:ss"
        const val event: String = "E. MM-dd-yyyy"
    }

    object KLocalBroadCast {
        const val event: String = "event"
    }

//QuickBlox

    const val kQBApplicationID = "78215"
    const val kQBAuthKey = "tsk64yaAJREtCJd"
    const val kQBAuthSecret = "CxQekc6XUsqrzJZ"
    const val kQBAccountKey = "AoQdCoDMp8CfaxxEMfPp"
    const val KQBUUserPassword = "SmoxTalk123!"

    val KUSAStates = arrayOf(
        "AK - Alaska",
        "AL - Alabama",
        "AR - Arkansas",
        "AS - American Samoa",
        "AZ - Arizona",
        "CA - California",
        "CO - Colorado",
        "CT - Connecticut",
        "DC - District of Columbia",
        "DE - Delaware",
        "FL - Florida",
        "GA - Georgia",
        "GU - Guam",
        "HI - Hawaii",
        "IA - Iowa",
        "ID - Idaho",
        "IL - Illinois",
        "IN - Indiana",
        "KS - Kansas",
        "KY - Kentucky",
        "LA - Louisiana",
        "MA - Massachusetts",
        "MD - Maryland",
        "ME - Maine",
        "MI - Michigan",
        "MN - Minnesota",
        "MO - Missouri",
        "MS - Mississippi",
        "MT - Montana",
        "NC - North Carolina",
        "ND - North Dakota",
        "NE - Nebraska",
        "NH - New Hampshire",
        "NJ - New Jersey",
        "NM - New Mexico",
        "NV - Nevada",
        "NY - New York",
        "OH - Ohio",
        "OK - Oklahoma",
        "OR - Oregon",
        "PA - Pennsylvania",
        "PR - Puerto Rico",
        "RI - Rhode Island",
        "SC - South Carolina",
        "SD - South Dakota",
        "TN - Tennessee",
        "TX - Texas",
        "UT - Utah",
        "VA - Virginia",
        "VI - Virgin Islands",
        "VT - Vermont",
        "WA - Washington",
        "WI - Wisconsin",
        "WV - West Virginia",
        "WY - Wyoming"
    )

    //var backButton : Int =  R.drawable.ic_back

    fun convertLocalToUTC(localDate: String, formatter: SimpleDateFormat): String {

        //val formatter =  SimpleDateFormat(Constants.KDateFormatter.server, Locale.getDefault())
        var conOfficialDate: Date = formatter.parse(localDate)

        //val formatter =  SimpleDateFormat(KDateFormatter.serverDay, Locale.getDefault())

        //formatter.applyPattern(Constants.KDateFormatter.server)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        val utcDate = formatter.format(conOfficialDate)

        return utcDate
    }

    fun convertLocalToUTC(localDate: Date, formatter: SimpleDateFormat): String {
        //formatter.timeZone = TimeZone.getTimeZone("UTC")
        val utcDate = formatter.format(localDate)

        return utcDate
    }
}


