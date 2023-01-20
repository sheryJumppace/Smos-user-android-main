package com.smox.smoxuser.ui.activity.customer

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import com.smox.smoxuser.R
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.fragment.barber.UpNextFragment

class UpNextActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_up_next)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@UpNextActivity, Constants.backButton))
        }
        val barberId = intent.getIntExtra("barber_id", 0)
        val bundle = bundleOf("barber_id" to barberId, "canShowService" to false)

        val fragment = UpNextFragment()
        fragment.arguments = bundle

        val fm = supportFragmentManager

        val ft = fm.beginTransaction()
        ft.add(R.id.frameContainer, fragment, "terms")
        ft.commit()
        fm.executePendingTransactions()
    }

}
