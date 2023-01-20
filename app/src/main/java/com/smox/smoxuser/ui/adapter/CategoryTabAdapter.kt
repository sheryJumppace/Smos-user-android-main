package com.app.tialupe.appointment

import android.app.Activity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.smox.smoxuser.model.Category
import com.smox.smoxuser.ui.fragment.barber.ServiceFragment

class CategoryTabAdapter(
    var activity: Activity,
    fm: FragmentManager?,
    var totalTabs: Int,
    var categories: ArrayList<Category>
) :
    FragmentPagerAdapter(fm!!, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    // this is for fragment tabs
    override fun getItem(position: Int): ServiceFragment {
        //initiate upcoming appointment fragment
        return ServiceFragment.newInstance(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // this counts total number of tabs
    override fun getCount(): Int {
        return totalTabs
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return categories.get(position).cat_name
    }
}