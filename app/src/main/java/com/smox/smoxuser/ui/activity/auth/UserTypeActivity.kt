package com.smox.smoxuser.ui.activity.auth

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import androidx.databinding.DataBindingUtil
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ActivityUserTypeBinding
import com.smox.smoxuser.model.type.UserType

class UserTypeActivity : BaseLoginActivity(), View.OnClickListener {

    private var type = UserType.Customer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding:ActivityUserTypeBinding = DataBindingUtil.setContentView(this, R.layout.activity_user_type)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@UserTypeActivity, Constants.backButton))
        }
        this.title = getString(R.string.are_you_a_atyler)


        binding.btnSave.setOnClickListener(this)

        val radioGroup = binding.radioGroup
        radioGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { _, checkedId ->
            type = if (checkedId == R.id.radioCustomer) {
                UserType.Customer
            } else {
                UserType.Barber
            }
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnSave -> updateAccountType(type)
        }
    }

}
