package com.smox.smoxuser.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.smox.smoxuser.R
import com.smox.smoxuser.ui.fragment.intro.IntroFragment
import com.smox.smoxuser.utils.shortToast
import kotlinx.android.synthetic.main.activity_intro_view.*


class IntroViewActivity : BaseActivity() {
    private var pageSize = 0
    private lateinit var titles:List<Int>
    private lateinit var contents:List<Int>
    private lateinit var images:List<Int>
    private var doubleBackToExitPressedOnce = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro_view)

        titles = if(sessionManager.userType == 0)
                    listOf(R.string.intro_styler_title1, R.string.intro_styler_title2, R.string.intro_styler_title3, R.string.intro_styler_title4)
                 else
                    listOf(R.string.intro_customer_title1, R.string.intro_customer_title2, R.string.intro_customer_title3, R.string.intro_customer_title4)
        contents = if(sessionManager.userType == 0)
                    listOf(R.string.intro_styler_content1, R.string.intro_styler_content2, R.string.intro_styler_content3, R.string.intro_styler_content4)
                else
                    listOf(R.string.intro_customer_content1, R.string.intro_customer_content2, R.string.intro_customer_content3, R.string.intro_customer_content4)
        images = if(sessionManager.userType == 0)
            listOf(R.drawable.intro_styler1, R.drawable.intro_styler2, R.drawable.intro_styler3, R.drawable.intro_styler4)
        else
            listOf(R.drawable.intro_customer1, R.drawable.intro_customer2, R.drawable.intro_customer3, R.drawable.intro_styler4)

        pageSize = titles.size
        viewPager.adapter = SectionsPagerAdapter(supportFragmentManager)
        viewPager.offscreenPageLimit = pageSize - 1
        viewPager.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                pageIndicatorView.selection = position
            }

        })
        pageIndicatorView.count = pageSize
        pageIndicatorView.selection = 0
    }
    private fun openMainPage(){
        val intent = Intent(this@IntroViewActivity, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
    private inner class SectionsPagerAdapter(fm: androidx.fragment.app.FragmentManager) : androidx.fragment.app.FragmentPagerAdapter(fm){
        override fun getItem(position: Int): androidx.fragment.app.Fragment {
            return if(position == pageSize - 1) {
                IntroFragment.newInstance(titles[position], contents[position], images[position], true)
            } else {
                IntroFragment.newInstance(titles[position], contents[position], images[position])
            }
        }

        override fun getCount(): Int {
            return pageSize
        }

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
