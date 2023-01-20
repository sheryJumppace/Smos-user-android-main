package com.smox.smoxuser.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.kaopiz.kprogresshud.KProgressHUD
import com.smox.smoxuser.R
import com.smox.smoxuser.App
import com.smox.smoxuser.manager.SessionManager


open class BaseFragment : Fragment() {
    lateinit var progressHUD: KProgressHUD
    lateinit var sessionManager: SessionManager
    lateinit var app: App
    var isTablet: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isTablet = resources.getBoolean(R.bool.isTablet)
        app = App.instance
        context?.let {
            sessionManager = SessionManager.getInstance(it)
            progressHUD = KProgressHUD(it)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
        }


    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

}
