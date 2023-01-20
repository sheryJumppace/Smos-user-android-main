package com.smox.smoxuser.model

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
@Keep
class Chats {
    var id: String? = null
    var sender_id: String? = null
    var receiver_id: String? = null
    var message: String? = null
    var message_type = 0
    @field:JvmField
    var is_seen: Boolean? = null
    var msg_time: Long? = null
    var sender_name: String? = null
    var sender_profile: String? = null
}