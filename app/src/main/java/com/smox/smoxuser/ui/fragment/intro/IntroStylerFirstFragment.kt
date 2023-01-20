package com.smox.smoxuser.ui.fragment.intro


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.smox.smoxuser.R

class IntroStylerFirstFragment : androidx.fragment.app.Fragment() {


    fun newInstance(): IntroStylerFirstFragment {
        return IntroStylerFirstFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v =  inflater.inflate(R.layout.fragment_intro_styler_first, container, false)
        return v
    }
}
