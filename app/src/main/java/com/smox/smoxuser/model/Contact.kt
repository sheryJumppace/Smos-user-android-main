package com.smox.smoxuser.model

import android.graphics.Bitmap
import java.io.Serializable


open class Contact(val name:String, val phoneNumber:String, val profileImage:Bitmap?) : Serializable {
    var userID: Int = 0
    var customerID: Int = 0
}

