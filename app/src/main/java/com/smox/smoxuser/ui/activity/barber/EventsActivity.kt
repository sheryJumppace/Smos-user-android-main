package com.smox.smoxuser.ui.activity.barber

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.smox.smoxuser.R
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.fragment.events.EventsFragment


class EventsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@EventsActivity, Constants.backButton))
        }

        title = "Events"


        val webViewFragment = EventsFragment()

        val fm = supportFragmentManager

        val ft = fm.beginTransaction()
        ft.add(R.id.frameContainer, webViewFragment, "events")
        ft.commit()
        fm.executePendingTransactions()

    }
}
