package com.smox.smoxuser.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import com.kaopiz.kprogresshud.KProgressHUD
import com.smox.smoxuser.App

@IntDef(Toast.LENGTH_LONG, Toast.LENGTH_SHORT)
private annotation class ToastLength

fun shortToast(@StringRes text: Int) {
    shortToast(App.instance.getString(text))
}

fun shortToast(text: String?) {
    show(text, Toast.LENGTH_SHORT)
}

fun getProgressBar(context: Context): KProgressHUD{
    return getProgressBar1(context)
}

fun longToast(@StringRes text: Int) {
    longToast(App.instance.getString(text))
}

fun longToast(text: String) {
    show(text, Toast.LENGTH_LONG)
}

private fun show(text: String?, @ToastLength length: Int) {
    text?.let {
        makeToast(it, length).show()
    }
}

private fun makeToast(text: String, @ToastLength length: Int): Toast {
    return Toast.makeText(App.instance, text, length)
}

private fun getProgressBar1(context: Context): KProgressHUD {
    val progressHUD = KProgressHUD(context)
    progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
        .setCancellable(true)
        .setAnimationSpeed(2)
        .setDimAmount(0.5f)
    return progressHUD
}