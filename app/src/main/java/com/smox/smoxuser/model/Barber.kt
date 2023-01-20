package com.smox.smoxuser.model

import com.smox.smoxuser.HolidayRes
import com.smox.smoxuser.model.type.LoginType
import com.smox.smoxuser.model.type.UserType
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception
import java.util.ArrayList

class Barber : SmoxUser {

    var logo: String = ""
    var twitter: String = ""
    var instagram: String = ""
    var facebook: String = ""
    var youtube: String = ""
    var linkedin: String = ""

    var openDays = ArrayList<OpenDay>()
    var upNext: Int = 0

    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var address: String = ""
    var distance: String = ""
    var bio: String = ""
    var countOfReviews: Int = 0
    var rating: Float = 0.0f
    var cancellationFee: Int = 0

    var services = ArrayList<Service>()
    var products = ArrayList<Product>()
    var isApproved = false
    var connectAccount = false
    var stripeUrl: String = ""
    var isSubscribed: Boolean = false
    var subscriptionEndDate: String = ""
    var stripe_customer_id :String = ""
    var business_name:String = ""
    var holidaysList= arrayListOf<HolidayRes.Holidays>()


    fun getService() = services.map { it.title }.joinToString(", ") { it }

    fun getDetailedService(): ArrayList<Service> {
        return services
    }

    constructor() : super()
    constructor(json: JSONObject) : super(json) {
        try {
            if (json.has("logo")) {
                this.logo = json.getString("logo")
            }
            if(json.has("business_name")){
                this.business_name= json.getString("business_name")
            }
            if (json.has("facebook")) {
                this.facebook = json.getString("facebook")
            }
            if (json.has("twitter")) {
                this.twitter = json.getString("twitter")
            }
            if (json.has("instagram")) {
                this.instagram = json.getString("instagram")
            }
            if (json.has("youtube")) {
                this.youtube = json.getString("youtube")
            }
            if (json.has("linkedin")) {
                this.linkedin = json.getString("linkedin")
            }
            if (json.has("location") && !json.isNull("location")) {
                this.address = json.getString("location")
            }
            if (json.has("location_lat") && !json.isNull("location_lat")) {
                this.latitude = json.getDouble("location_lat")
            }
            if (json.has("location_lng") && !json.isNull("location_lng")) {
                this.longitude = json.getDouble("location_lng")
            }
            if (json.has("distance")) {
                this.distance = json.getString("distance")
            }
            if (json.has("open_hours")) {
                if (!json.isNull("open_hours")) {
                    this.openDays = getOpenDays(json.getJSONObject("open_hours"))

                }
            }
            if (json.has("upnext")) {
                this.upNext = json.getInt("upnext")
            }
            if (json.has("bio")) {
                this.bio = json.getString("bio")
            }
            if (json.has("review_count")) {
                this.countOfReviews = json.getInt("review_count")
            }
            if (json.has("avg_rating")) {
                try {
                    this.rating = json.getDouble("avg_rating").toFloat()
                } catch (e: Exception) {

                }

            }
            if (json.has("cancellation_fee")) {
                this.cancellationFee = json.getDouble("cancellation_fee").toInt()
            }
            if (json.has("services")) {
                val items = json.getJSONArray("services")
                for (i in 0 until items.length()) {
                    val model = Service(items.getJSONObject(i))
                    this.services.add(model)
                }
            }
            if (json.has("is_activated")) {
                this.isApproved = json.getInt("is_activated") == 1
            }
            if (json.has("connect_account")) {
                this.connectAccount = json.getBoolean("connect_account")
            }
            if (json.has("stripe_url")) {
                this.stripeUrl = json.getString("stripe_url")
            }
            if (json.has("is_subscribed")) {
                this.isSubscribed = json.getBoolean("is_subscribed")
            }
            if (json.has("subscription_enddate")) {
                this.subscriptionEndDate = json.getString("subscription_enddate")
            }

            if (json.has("stripe_customer_id")){
                this.stripe_customer_id  = json.getString("stripe_customer_id")
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    constructor(data: String?) {
        try {
            val json = JSONObject(data)
            this.business_name = json.getString("business_name")
            this.id = json.getInt("id")
            this.email = json.getString("email")
            this.phone = json.getString("phone")
            this.firstName = json.getString("firstName")
            this.lastName = json.getString("lastName")
            this.image = json.getString("image")
            this.accountType = UserType.valueOf(json.getString("accountType"))
            this.loginType = LoginType.valueOf(json.getString("loginType"))
            this.logo = json.getString("logo")
            this.twitter = json.getString("twitter")
            this.instagram = json.getString("instagram")
            this.facebook = json.getString("facebook")
            this.youtube = json.getString("youtube")
            this.linkedin = json.getString("linkedin")
            this.latitude = json.getDouble("latitude")
            this.longitude = json.getDouble("longitude")
            this.address = json.getString("address")
            this.bio = json.getString("bio")
            //this.openDay = json.getJSONObject("open_hours")
            this.cancellationFee = json.getDouble("cancellationFee").toInt()
            this.connectAccount = json.getBoolean("connect_account")
            this.stripeUrl = json.getString("stripe_url")
            this.stripe_public_key = json.getString("stripe_public_key")
            this.stripe_secret_key = json.getString("stripe_secret_key")
            this.stripe_client_key = json.getString("stripe_client_key")
            this.isSubscribed = json.getBoolean("is_subscribed")
            this.subscriptionEndDate = json.getString("subscription_enddate")
            this.stripe_customer_id = json.getString("stripe_customer_id")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun getJsonString(): String {
        try {
            val json = JSONObject()
            json.put("id", id)
            json.put("email", email)
            json.put("phone", phone)
            json.put("firstName", firstName)
            json.put("lastName", lastName)
            json.put("image", image)
            json.put("accountType", accountType.name)
            json.put("loginType", loginType.name)
            json.put("logo", logo)
            json.put("twitter", twitter)
            json.put("instagram", instagram)
            json.put("facebook", facebook)
            json.put("youtube", youtube)
            json.put("linkedin", linkedin)
            json.put("latitude", latitude)
            json.put("longitude", latitude)
            json.put("address", address)
            json.put("bio", bio)
            //json.put("openDays", openDay)
            json.put("cancellationFee", cancellationFee)
            json.put("connect_account", connectAccount)
            json.put("stripe_url", stripeUrl)
            json.put("stripe_public_key", stripe_public_key)
            json.put("stripe_secret_key", stripe_secret_key)
            json.put("stripe_client_key", stripe_client_key)
            json.put("is_subscribed", isSubscribed)
            json.put("subscription_enddate", subscriptionEndDate)
            json.put("stripe_customer_id",stripe_customer_id)
            json.put("business_name",business_name)
            return json.toString()
        } catch (e: JSONException) {
            e.printStackTrace()
            return ""
        }
    }

    fun getOpenDays(json: JSONObject): ArrayList<OpenDay> {

        val days = ArrayList<OpenDay>()

        val sanday = OpenDay("Sunday")
        sanday.id = 0
        //{"id":"15","barber_id":"206","mo":"09:00 AM-11:36 PM","tu":"09:23 AM-09:23 PM","we":"10:56 AM-09:42 PM","th":"01:35 PM-09:30 PM","fr":"03:35 PM-09:47 PM","sa":"09:00 AM-05:00 PM","su":"09:00 AM-06:24 PM","created_at":"2020-09-14 09:34:30"}
        if (json.has("su")) {
            sanday.updateTime(json.getString("su"))
        } else {
            sanday.updateTime("")
        }
        days.add(sanday)

        val monday = OpenDay("Monday")
        monday.id = 1
        if (json.has("mo")) {
            monday.updateTime(json.getString("mo"))
        } else {
            monday.updateTime("")
        }
        days.add(monday)

        val tuesday = OpenDay("Tuesday")
        tuesday.id = 2
        if (json.has("tu")) {
            tuesday.updateTime(json.getString("tu"))
        } else {
            tuesday.updateTime("")
        }
        days.add(tuesday)

        val wednesday = OpenDay("Wednesday")
        wednesday.id = 3
        if (json.has("we")) {
            wednesday.updateTime(json.getString("we"))
        } else {
            wednesday.updateTime("09:00 AM-05:00 PM")
        }
        days.add(wednesday)

        val thursday = OpenDay("Thursday")
        thursday.id = 4
        if (json.has("th")) {
            thursday.updateTime(json.getString("th"))
        } else {
            thursday.updateTime("09:00 AM-05:00 PM")
        }
        days.add(thursday)

        val friday = OpenDay("Friday")
        friday.id = 5
        if (json.has("fr")) {
            friday.updateTime(json.getString("fr"))
        } else {
            friday.updateTime("09:00 AM-05:00 PM")
        }
        days.add(friday)

        val saturday = OpenDay("Saturday")
        saturday.id = 6
        if (json.has("sa")) {
            saturday.updateTime(json.getString("sa"))
        } else {
            saturday.updateTime("09:00 AM-05:00 PM")
        }
        days.add(saturday)
        return days
    }

    fun getStringOfOpenDays(): String {
        var res = ""
        var i = 0
        for (item in openDays) {
            res += if (i == 0) {
                String.format("%s: ", item.day)
            } else {
                String.format("\n%s: ", item.day)
            }
            res += if (item.isClosed) {
                "CLOSE"
            } else {
                item.startTime + " to " + item.endTime
            }
            i += 1
        }
        return res
    }

    fun isEmptySocial(): Boolean {
        return facebook.isEmpty() && twitter.isEmpty() && instagram.isEmpty()
    }

    /*override fun toString(): String {
        return "Barber(logo='$logo', twitter='$twitter', instagram='$instagram', facebook='$facebook', youtube='$youtube', linkedin='$linkedin', openDays=$openDays, upNext=$upNext, latitude=$latitude, longitude=$longitude, address='$address', distance='$distance', bio='$bio', countOfReviews=$countOfReviews, rating=$rating, cancellationFee=$cancellationFee, services=$services, products=$products, isApproved=$isApproved, connectAccount=$connectAccount, stripeUrl='$stripeUrl', isSubscribed=$isSubscribed, subscriptionEndDate='$subscriptionEndDate')"
    }*/
}
