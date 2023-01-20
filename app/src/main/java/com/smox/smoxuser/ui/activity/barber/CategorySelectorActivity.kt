package com.smox.smoxuser.ui.activity.barber

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.smox.smoxuser.R
import com.smox.smoxuser.model.Category
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.adapter.CategorySelectorAdapter
import kotlinx.android.synthetic.main.activity_booking_appointment.toolbar
import kotlinx.android.synthetic.main.activity_cat_selector.*

class CategorySelectorActivity:BaseActivity(),
    CategorySelectorAdapter.CategorySelectActions {

    private var items: ArrayList<Category> = ArrayList();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cat_selector)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.select_cat)
        }
        if(intent!=null && intent.hasExtra("CatList")){
            items = intent.getSerializableExtra("CatList") as ArrayList<Category>
            setAdapter()
        }
    }

    private fun setAdapter() {
        var categoryAdapter = CategorySelectorAdapter(this, this)
        category_list.layoutManager= LinearLayoutManager(this)
        category_list.setHasFixedSize(true)
        category_list.adapter=categoryAdapter
        categoryAdapter.doRefresh(items)
    }

    override fun onItemClick(pos: Int) {
        var intent = Intent();
        intent.putExtra("category",items.get(pos))
        setResult(Activity.RESULT_OK,intent)
        finish()
    }

}