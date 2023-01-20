package com.smox.smoxuser.ui.fragment.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.smox.smoxuser.R
import com.smox.smoxuser.ui.fragment.BaseFragment

class BarberHomeFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_barber_home, container, false)
    }
}