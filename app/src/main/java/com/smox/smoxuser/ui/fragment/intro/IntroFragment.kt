package com.smox.smoxuser.ui.fragment.intro


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.TextView
import com.santalu.aspectratioimageview.AspectRatioImageView

import com.smox.smoxuser.R
import com.smox.smoxuser.ui.activity.HomeActivity

private const val ARG_TITLE = "param1"
private const val ARG_CONTENT = "param2"
private const val ARG_IMG = "param3"
private const val ARG_IS_LAST = "param4"

class IntroFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var title: Int = -1
    private var content: Int = -1
    private var img: Int = -1
    private var isLast:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getInt(ARG_TITLE)
            content = it.getInt(ARG_CONTENT)
            img = it.getInt(ARG_IMG)
            isLast = it.getBoolean(ARG_IS_LAST)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_intro, container, false)
        if(title > 0){
            (v.findViewById<View>(R.id.txtTitle) as TextView).text = getString(title)
        }
        if(content > 0){
            (v.findViewById<View>(R.id.txtContent) as TextView).text = getString(content)
        }
        if(img > 0){
            (v.findViewById<View>(R.id.imgView) as AspectRatioImageView).setImageResource(img)
        }
        val btnStart = v.findViewById<View>(R.id.btnStart)
        if(!isLast){
            btnStart.visibility = View.GONE
        }
        btnStart.setOnClickListener{
            openMainPage()
        }
        return v
    }

    private fun openMainPage(){
        val intent = Intent(activity!!, HomeActivity::class.java)
        startActivity(intent)
    }

    companion object {
        @JvmStatic
        fun newInstance(title: Int, content: Int, img:Int, isLast:Boolean = false) =
            IntroFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TITLE, title)
                    putInt(ARG_CONTENT, content)
                    putInt(ARG_IMG, img)
                    putBoolean(ARG_IS_LAST, isLast)
                }
            }
    }
}
