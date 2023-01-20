package com.smox.smoxuser.model

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import androidx.room.Ignore
import com.smox.smoxuser.App
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.manager.SessionManager
import com.smox.smoxuser.model.type.AppointmentType
import com.smox.smoxuser.model.type.UserType
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable
import java.lang.Exception
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


open class Appointment : Serializable {

    var id: Int = -1
    var customerId: Int = -1
    var contactId: Int = -1
    var digitsCode = 1
    var barberId: Int = -1
    var barberName: String = ""
    var comment: String = ""
    var duration: Int = 0
    var preferredDate: Date = Date()
    var officialDate: Date? = null
    var utcOfficialDate: Date? = null
    var completedDate: Date? = null
    var strOfficialDate: String = ""
    var strOnlyDate: String = ""
    var strUtcOfficialDate: String = ""
    var strPreferredDate: String = ""
    var appointmentDate: String = ""
    var username: String = ""
    var price: Float = 0f
    var timeslot = arrayListOf<String>()
    var slotTime = ""
    var status: AppointmentType = AppointmentType.Pending
    var services = ArrayList<Service>()
    var user: Barber = Barber()
    var isPaid = false
    var isSection = false
    var cancellationFee: Float = 0.0f
    var createdAt: String = ""
    var updatedAt: String = ""
    var rating:Float?=null
    var cleanRating:Int?=null
    var workRating:Int?=null
    var behaveRating:Int?=null
    val dateFormat = SimpleDateFormat(Constants.KDateFormatter.server, Locale.getDefault())


    @Ignore
    private var isDraggable = false

    @Ignore
    fun isDraggable(): Boolean {
        isDraggable = status == AppointmentType.Approved
        return isDraggable
    }

    @Ignore
    fun setDraggable() {//draggable: Boolean
        isDraggable = if (status == AppointmentType.Approved) true else false
    }

    fun getService(context: Context, isShowService: Boolean): String {
        Log.d(
            "user type:- ",
            "Session manager Service:- " + App.instance.currentUser.accountType +
                    "\n current userid - barber id " + App.instance.currentUser.id + " - " + customerId
        )
        return services.map { it.title }.joinToString(", ") { it }
    }

    fun getServicePrice(context: Context): String {

        var price = 0.0
        for (service in services) {
            price += service.price
        }
        return "$$price"
    }

    fun getDate(): String {
        return if (utcOfficialDate != null) {
            val dateFormat =
                SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
            dateFormat.format(utcOfficialDate!!)
        } else {
            ""
        }
    }

    fun gettAppointmentDate(): String {
        try {
            return if (appointmentDate.isNotEmpty()) {
                val dateFormat =
                    SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault())
                dateFormat.timeZone = TimeZone.getDefault()
                this.dateFormat.format(dateFormat.parse(appointmentDate)!!).toDate()
                    .formatTo(Constants.KDateFormatter.defaultDate)
            } else
                ""
        } catch (e: Exception) {

        }
        return ""
    }


    fun getCreatedDate(): String {
        try {
            return if (createdAt.isNotEmpty()) {
                dateFormat.timeZone = TimeZone.getDefault()
                createdAt.toDate().formatTo(Constants.KDateFormatter.defaultDate)
            } else
                ""
        } catch (e: Exception) {

        }
        return ""
    }

    fun getCreatedTime(): String {
        try {
            return if (createdAt.isNotEmpty()) {
                dateFormat.timeZone = TimeZone.getDefault()
                createdAt.toDate().formatTo(Constants.KDateFormatter.hourAM)
            } else ""
        } catch (e: Exception) {

        }
        return ""
    }

    fun getUpdateDate(): String {
        try {
            return if (!updatedAt.isNullOrEmpty() && updatedAt != "null") {
                dateFormat.timeZone = TimeZone.getDefault()
                updatedAt.toDate().formatTo(Constants.KDateFormatter.defaultDate)
            } else
                Date().formatTo(Constants.KDateFormatter.defaultDate)
        } catch (e: Exception) {

        }
        return ""
    }

    fun getUpdateTime(): String {
        try {
            return if (!updatedAt.isNullOrEmpty() && updatedAt != "null") {
                dateFormat.timeZone = TimeZone.getDefault()
                updatedAt.toDate().formatTo(Constants.KDateFormatter.hourAM)
            } else
                Date().formatTo(Constants.KDateFormatter.hourAM)
        } catch (e: Exception) {

        }
        return ""
    }


    fun getStatusMessage(): String {
        return when (status) {
            AppointmentType.Pending -> {
                "Your appointment is still pending"
            }
            AppointmentType.Approved -> {
                "Your appointment is approved"
            }
            AppointmentType.Completed -> {
                "Your appointment was done"
            }
            AppointmentType.Canceled, AppointmentType.NoShow, AppointmentType.UntimelyCanceled -> {
                "Your appointment was cancelled"
            }
            else -> "Your appointment was deleted"
        }
    }

    fun getDaySms(): String {
        try {
            return if (officialDate != null) {
                val dateFormat =
                    SimpleDateFormat(Constants.KDateFormatter.defaultDate, Locale.getDefault())
                val timeFormat =
                    SimpleDateFormat(Constants.KDateFormatter.hourAM, Locale.getDefault())
                dateFormat.format(officialDate!!) + ", " + timeFormat.format(officialDate!!)
                    .toUpperCase()
            } else {
                val dateFormat =
                    SimpleDateFormat(Constants.KDateFormatter.defaultDate, Locale.getDefault())
                val timeFormat =
                    SimpleDateFormat(Constants.KDateFormatter.hourAM, Locale.getDefault())
                dateFormat.format(preferredDate) + ", " + timeFormat.format(preferredDate)
                    .toUpperCase()
            }
        } catch (e: Exception) {

        }
        return ""
    }

    fun getDayTime(): String {
        try{
            return if (officialDate != null) {
                val dateFormat = SimpleDateFormat(Constants.KDateFormatter.hourAM, Locale.getDefault())
                dateFormat.format(officialDate!!)
            } else {
                val dateFormat = SimpleDateFormat(Constants.KDateFormatter.hourAM, Locale.getDefault())
                dateFormat.format(preferredDate)
            }
        }catch (e:Exception){

        }
       return ""
    }

    lateinit var sessionManager: SessionManager
    private lateinit var pref: SharedPreferences

    fun getUserSign(context: Context): String {
        //sessionManager = SessionManager.getInstance(context.applicationContext)
        //pref = context.applicationContext.getSharedPreferences(SessionManager.PREF_NAME, 0)

        Log.d("user type:- ", "Session manager:- " + App.instance.currentUser.accountType)
        return if (App.instance.currentUser.accountType == UserType.Barber && (status == AppointmentType.Approved || status == AppointmentType.Completed
                    || status == AppointmentType.Pending)
        ) "$ " else ""
    }

    fun getUserSignVisibility() = if (customerId == 0) View.VISIBLE else View.GONE

    fun getUserSignVisibilityCustomer() = if (customerId == 0) View.GONE else View.VISIBLE

    constructor() : super() {
        id = -1
        comment = ""
        preferredDate = Date()
        officialDate = null
        completedDate = null
        services = ArrayList()
        user = Barber()
        isSection = false
    }

    constructor(json: JSONObject) {
        try {
            if (json.has("id")) {
                id = json.getInt("id")
            }
            if (json.has("customer_id")) {
                this.customerId = json.getInt("customer_id")
            }
            if (json.has("contact_id")) {
                this.contactId = json.getInt("contact_id")
            }
            if (json.has("daily_code")) {
                this.digitsCode = json.getInt("daily_code")
            }
            if (json.has("barber_id")) {
                this.barberId = json.getInt("barber_id")
            }
            if (json.has("comment")) {
                if (json.getString("comment")!="null")
                this.comment = json.getString("comment")
            }
            if (json.has("duration")) {
                this.duration = json.getInt("duration")
            }
            if (json.has("paid")) {
                this.isPaid = json.getInt("paid") == 1
            }
            if (json.has("price")) {
                this.price = json.getDouble("price").toFloat() ?: 0f
            }
            if (json.has("appointment_date")) {
                this.appointmentDate = json.getString("appointment_date") ?: ""
            }
            if (json.has("created_at")) {
                createdAt = json.getString("created_at") ?: ""
            }
            if (json.has("status_change_at")) {
                updatedAt = json.getString("status_change_at") ?: ""
            }

            if (json.has("average_rating")) {
                if (json.getString("average_rating")!="null")
                rating = json.getString("average_rating").toFloat()
            }
            if (json.has("clean_rating")) {
                if (json.getString("clean_rating")!="null")
                cleanRating = json.getString("clean_rating").toInt()
            }
            if (json.has("work_rating")) {
                if (json.getString("work_rating")!="null")
                workRating = json.getString("work_rating").toInt()
            }
            if (json.has("behave_rating")) {
                if (json.getString("behave_rating")!="null")
                behaveRating = json.getString("behave_rating").toInt()
            }

            if (json.has("timeslot")) {
                if (json.getJSONArray("timeslot") != null) {

                    val tim = json.getJSONArray("timeslot")
                    for (i in 0 until tim.length()) {
                        timeslot.add(tim.getString(i))
                    }
                }
            }
            val dateFormat = SimpleDateFormat(Constants.KDateFormatter.server, Locale.getDefault())
            dateFormat.timeZone = TimeZone.getDefault()
            if (json.has("preferred_at")) {
                this.strPreferredDate = json.getString("preferred_at")
                try {
                    val convertToLocalDateTime =
                        strPreferredDate.toDate().formatTo(Constants.KDateFormatter.server)
                    this.preferredDate = dateFormat.parse(convertToLocalDateTime)!!
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }

            if (json.has("official_at")) {
                val d = json.getString("official_at")
                if (!(d == "0000-00-00 00:00:00" || d.isEmpty()||d!="null")) {
                    this.strOfficialDate = d
                    try {
                        val convertToLocalDateTime =
                            d.toDate().formatTo(Constants.KDateFormatter.server)
                        this.officialDate = dateFormat.parse(convertToLocalDateTime)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                }
            }
            if (json.has("utc_official_at")) {
                val d = json.getString("utc_official_at")
                if (!(d == "0000-00-00 00:00:00" || d.isEmpty())) {
                    this.strUtcOfficialDate = d
                    try {
                        val convertToLocalDateTime =
                            d.toDate().formatTo(Constants.KDateFormatter.server)
                        this.utcOfficialDate = dateFormat.parse(convertToLocalDateTime)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                }
            }
            if (json.has("completed_at") && !json.getString("completed_at").equals("null")) {
                val strDate = json.getString("completed_at")
                try {
                    this.completedDate = dateFormat.parse(strDate)
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }
            if (json.has("status")) {
                if (!json.getString("status").isNullOrEmpty()) {
                    this.status = AppointmentType.valueOf(json.getString("status").capitalize())
                }
            }
            if (json.has("services")) {
                val items = json.getJSONArray("services")
                for (i in 0 until items.length()) {
                    val model = Service(items.getJSONObject(i))
                    this.services.add(model)
                }
            }
            if (json.has("cancellation_fee")) {
                this.cancellationFee = json.getDouble("cancellation_fee").toFloat()
            }
            if (json.has("barber")) {
                val json = json.getJSONObject("barber")
                this.user = Barber(json)
            }

            if (this.contactId == 0 && this.customerId == 0) {
                this.user.firstName = String.format("A%02d", this.digitsCode)
                this.user.lastName = ""
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    fun String.toDate(
        dateFormat: String = Constants.KDateFormatter.server,
        timeZone: TimeZone = TimeZone.getTimeZone("UTC")
    ): Date {
        val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
        parser.timeZone = timeZone
        return parser.parse(this)!!
    }

    fun Date.formatTo(dateFormat: String, timeZone: TimeZone = TimeZone.getDefault()): String {
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        formatter.timeZone = timeZone
        return formatter.format(this)
    }

    override fun toString(): String {
        return "Appointment(id=$id, customerId=$customerId, contactId=$contactId, digitsCode=$digitsCode, barberId=$barberId, comment='$comment', duration=$duration, preferredDate=$preferredDate, officialDate=$officialDate, completedDate=$completedDate, strOfficialDate='$strOfficialDate', strPreferredDate='$strPreferredDate', username='$username', status=$status, services=$services, user=$user, isPaid=$isPaid, isSection=$isSection, cancellationFee=$cancellationFee, isDraggable=$isDraggable)"
    }
}
