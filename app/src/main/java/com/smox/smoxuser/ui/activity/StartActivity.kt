package com.smox.smoxuser.ui.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.Toast
import com.smox.smoxuser.R
import com.smox.smoxuser.utils.shortToast
import kotlinx.android.synthetic.main.activity_start.*


class StartActivity : BaseActivity() {

    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val w = window // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        btnStyler.setOnClickListener{
            openIntroPage(0)
        }
        btnCustomer.setOnClickListener{
            openIntroPage(1)
        }
    }
    private fun openIntroPage(isBarber:Int){
        sessionManager.userType = isBarber
        val intent = Intent(this@StartActivity, IntroViewActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        shortToast(resources.getString(R.string.back_again_exit))

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }
}
