package com.smox.smoxuser.ui.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import com.smox.smoxuser.R
import com.smox.smoxuser.ui.fragment.WebViewFragment


class WebViewActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@WebViewActivity, Constants.backButton))
        }

        //title = "TERMS"

        val url = intent.getStringExtra("url")
        title = intent.getStringExtra("title")

        val bundle = bundleOf("url" to url)

        val webViewFragment = WebViewFragment()
        webViewFragment.arguments = bundle

        val fm = supportFragmentManager

        val ft = fm.beginTransaction()
        ft.add(R.id.frameContainer, webViewFragment, "terms")
        ft.commit()
        fm.executePendingTransactions()

    }
}
