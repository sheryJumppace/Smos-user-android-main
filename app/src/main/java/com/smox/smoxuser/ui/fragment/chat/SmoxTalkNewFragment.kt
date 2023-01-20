package com.smox.smoxuser.ui.fragment.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.FragmentSmoxTalkNewBinding
import com.smox.smoxuser.ui.adapter.ChatTabLayoutAdapter

class SmoxTalkNewFragment : Fragment() {

    lateinit var binding: FragmentSmoxTalkNewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSmoxTalkNewBinding.inflate(inflater, container, false)
        activity?.findViewById<LinearLayoutCompat>(R.id.llCurrInfo)?.visibility = View.GONE
        setAdapter()
        return binding.root
    }

    private fun setAdapter() {

        val adapter = ChatTabLayoutAdapter(requireActivity().supportFragmentManager)

        adapter.addFragment(RecentGroupFragment().newInstance(false), getString(R.string.recent))
        //adapter.addFragment(RecentGroupFragment().newInstance(true), getString(R.string.group))

        binding.vpViewPager.adapter = adapter
        binding.vpViewPager.offscreenPageLimit = 2
        binding.tabLayout1.setupWithViewPager(binding.vpViewPager)

    }


}