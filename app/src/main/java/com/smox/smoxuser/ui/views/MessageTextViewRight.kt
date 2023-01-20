package com.smox.smoxuser.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.smox.smoxuser.R


class MessageTextViewRight(context: Context, attrs: AttributeSet) : MessageTextView(context, attrs) {

    override fun setLinearSide() {
        val layoutParams = frameLinear.layoutParams as FrameLayout.LayoutParams
        layoutParams.gravity = Gravity.RIGHT
        frameLinear.layoutParams = layoutParams
    }

    override fun setTextLayout() {
        viewTextStub.layoutResource = R.layout.widget_text_msg_right
        layoutStub = viewTextStub.inflate() as LinearLayout
    }
}