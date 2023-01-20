package com.smox.smoxuser.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.smox.smoxuser.R
import com.smox.smoxuser.manager.SessionManager
import androidx.core.content.ContextCompat.startActivity

import android.net.Uri

import android.content.Intent
import com.smox.smoxuser.databinding.ShowStylerDownloadDialogBinding


class ShowStylerDownloadDialog(
    var context: Context
) {
    private var binding: ShowStylerDownloadDialogBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.show_styler_download_dialog,
        null,
        false
    )
    val dialog = Dialog(context)

    init {
        dialog.setContentView(binding.root)
        dialog.setCanceledOnTouchOutside(true)
        if (dialog.window != null) {
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.setGravity(Gravity.CENTER)
        }

        dialog.findViewById<TextView>(R.id.txtNoDownload).setOnClickListener {
            SessionManager.getInstance(context.applicationContext).isDownloadShow=false
            dialog.dismiss()
        }

        dialog.findViewById<TextView>(R.id.txtDownload).setOnClickListener {
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.smox.smoxtrimsetters"))
            startActivity(context,intent, null)
            dialog.dismiss()
        }

        dialog.show()
    }
}