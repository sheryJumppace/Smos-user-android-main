package com.smox.smoxuser.ui.activity.customer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.rd.PageIndicatorView
import com.smox.smoxuser.R
import com.smox.smoxuser.ui.adapter.FullImageviewAdapter
import java.util.ArrayList

class FullScreenImageActivity : AppCompatActivity() {
    lateinit var adapter: FullImageviewAdapter
    lateinit var viewpagerImage: ViewPager
    var imageList = ArrayList<String>()
    lateinit var pageIndicatorView: PageIndicatorView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)
        viewpagerImage = findViewById(R.id.viewpagerHallDetails)
        pageIndicatorView = findViewById(R.id.pageIndicatorView)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
        findViewById<View>(R.id.ic_back).setOnClickListener { finish() }

        imageList = intent.getStringArrayListExtra("URI")!!
        val pos = intent.getIntExtra("Pos", 0)
        adapter = FullImageviewAdapter(this@FullScreenImageActivity, imageList)
        viewpagerImage.adapter = adapter
        viewpagerImage.currentItem = pos
        pageIndicatorView.setViewPager(viewpagerImage)
        pageIndicatorView.setSelected(pos)

    }
}