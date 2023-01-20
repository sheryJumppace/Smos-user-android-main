package com.smox.smoxuser.ui.activity.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager

import com.smox.smoxuser.databinding.FragmentHomeBinding
import com.smox.smoxuser.ui.adapter.HorizontalItemAdapter
import com.smox.smoxuser.ui.adapter.VerticalItemAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
       // val v = inflater.inflate(R.layout.fragment_home, container, false)
        binding.rvHorizontal.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvHorizontal.adapter = HorizontalItemAdapter(requireActivity())

        binding.rvVertical.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvVertical.adapter = VerticalItemAdapter(requireActivity())

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}