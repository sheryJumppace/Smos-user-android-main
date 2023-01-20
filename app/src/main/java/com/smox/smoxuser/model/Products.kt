package com.smox.smoxuser.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Products(

    @SerializedName("error") val error: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: ArrayList<ProductItem>
) {
    data class ProductItem(
        @SerializedName("id") val id: Int,
        @SerializedName("user_id") val user_id: Int,
        @SerializedName("category_id") val category_id: Int,
        @SerializedName("category_name") val category_name: String,
        @SerializedName("product_name") val product_name: String,
        @SerializedName("main_img") val main_img: String,
        @SerializedName("stock") var stock: Int,
        @SerializedName("description") val description: String,
        @SerializedName("feature") val feature: String,
        @SerializedName("price") val price: Double,
        @SerializedName("discounted_price") val discountedPrice: Double,
        @SerializedName("shipping_price") val shipping_price: Double,
        @SerializedName("total_price") val total_price: Double,
        @SerializedName("model") val model: String,
        @SerializedName("is_active") val is_active: Int,
        @SerializedName("review_count") val review_count: Double,
        @SerializedName("review_user_count") val review_user_count: Int,
        @SerializedName("is_deleted") val is_deleted: Int,
        @SerializedName("is_cart_added") var is_cart_added: Int = 0,
        @SerializedName("quantity") var quantity: Int = 0,
        @SerializedName("created_at") val created_at: String,
        @SerializedName("updated_at") val updated_at: String,
        @SerializedName("deleted_at") val deleted_at: String,
        @SerializedName("qty_discounted_price") val qty_discounted_price: Double,
        @SerializedName("qty_shipping") val qty_shipping: Double,
        @SerializedName("qty_price") val qty_price: Double,
        @SerializedName("stripe_public_key") val stripe_public_stripe_public_key: String,
        @SerializedName("stripe_secret_key") val stripe_secret_stripe_secret_key: String
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readInt(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readString().toString(),
            parcel.readInt(),
            parcel.readDouble(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readString().toString(),
            parcel.readString().toString(),
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
            parcel.writeInt(user_id)
            parcel.writeInt(category_id)
            parcel.writeString(category_name)
            parcel.writeString(product_name)
            parcel.writeString(main_img)
            parcel.writeInt(stock)
            parcel.writeString(description)
            parcel.writeString(feature)
            parcel.writeDouble(price)
            parcel.writeDouble(discountedPrice)
            parcel.writeDouble(shipping_price)
            parcel.writeDouble(total_price)
            parcel.writeString(model)
            parcel.writeInt(is_active)
            parcel.writeDouble(review_count)
            parcel.writeInt(review_user_count)
            parcel.writeInt(is_deleted)
            parcel.writeInt(is_cart_added)
            parcel.writeInt(quantity)
            parcel.writeString(created_at)
            parcel.writeString(updated_at)
            parcel.writeString(deleted_at)
            parcel.writeDouble(qty_discounted_price)
            parcel.writeDouble(qty_shipping)
            parcel.writeDouble(qty_price)
            parcel.writeString(stripe_public_stripe_public_key)
            parcel.writeString(stripe_secret_stripe_secret_key)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<ProductItem> {
            override fun createFromParcel(parcel: Parcel): ProductItem {
                return ProductItem(parcel)
            }

            override fun newArray(size: Int): Array<ProductItem?> {
                return arrayOfNulls(size)
            }
        }
    }
}
