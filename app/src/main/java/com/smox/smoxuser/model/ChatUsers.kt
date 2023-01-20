package com.ibcemobile.smoxstyler.model

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
@Keep
class ChatUsers {
    var id: String? = null
    var last_message_time: String? = null
    var last_message: String? = null
    var message_count: String? = null
    var chat_room_id: String? = null
    var user_name: String? = null
    var barber_name: String? = null
    var user_id: String? = null
    var sender_id: String? = null
    var barber_id: String? = null
    var sender_profile: String? = null
    var receiver_profile: String? = null
}