package com.smox.smoxuser.model

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
@Keep
class TrackOrderStatus(
    var statusTitle: String = "",
    var statusDate: String = "",
    var statusMsg: String = "",
    var isCompleted: Boolean = false
)