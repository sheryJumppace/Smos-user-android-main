package com.smox.smoxuser.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class AddressResponse(
    @SerializedName("error") val error: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: ArrayList<AddressData>
){
    class AddressData(
        @SerializedName("id") val id: String?="",
        @SerializedName("user_id") val user_id: String? ="",
        @SerializedName("first_name") val first_name: String?="",
        @SerializedName("last_name") val last_name: String?="",
        @SerializedName("address_one") val address_one: String?="",
        @SerializedName("address_two") val address_two: String?="",
        @SerializedName("city") val city: String?="",
        @SerializedName("state") val state: String?="",
        @SerializedName("country") val country: String?="",
        @SerializedName("zipcode") val zipcode: String?="",
        @SerializedName("latitude") val latitude: String?="",
        @SerializedName("longitude") val longitude: String?="",
        @SerializedName("phone") val phone: String?="",
        @SerializedName("make_default") val make_default: String?="",
        @SerializedName("is_deleted") val is_deleted: String?="",
        @SerializedName("created_at") val created_at: String?="",
        @SerializedName("updated_at") val updated_at: String?=""

    ):Parcelable{
        constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeString(user_id)
            parcel.writeString(first_name)
            parcel.writeString(last_name)
            parcel.writeString(address_one)
            parcel.writeString(address_two)
            parcel.writeString(city)
            parcel.writeString(state)
            parcel.writeString(country)
            parcel.writeString(zipcode)
            parcel.writeString(latitude)
            parcel.writeString(longitude)
            parcel.writeString(phone)
            parcel.writeString(make_default)
            parcel.writeString(is_deleted)
            parcel.writeString(created_at)
            parcel.writeString(updated_at)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<AddressData> {
            override fun createFromParcel(parcel: Parcel): AddressData {
                return AddressData(parcel)
            }

            override fun newArray(size: Int): Array<AddressData?> {
                return arrayOfNulls(size)
            }
        }

        fun defaultAddress():String{
            return "$first_name $last_name\n$address_one,$address_two, $city, $state, $zipcode\n$phone"
        }
    }
}



