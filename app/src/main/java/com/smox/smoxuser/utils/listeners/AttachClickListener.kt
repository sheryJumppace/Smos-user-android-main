package com.smox.smoxuser.utils.listeners

import com.quickblox.chat.model.QBAttachment


interface AttachClickListener {
    fun onLinkClicked(attachment: QBAttachment, positionInAdapter: Int)
}