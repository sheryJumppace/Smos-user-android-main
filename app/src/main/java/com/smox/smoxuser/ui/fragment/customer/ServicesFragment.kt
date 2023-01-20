package com.smox.smoxuser.ui.fragment.customer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.smox.smoxuser.databinding.FragmentServicesBinding
import com.smox.smoxuser.model.Category
import com.smox.smoxuser.model.Service
import com.smox.smoxuser.ui.adapter.CategoryNameAdapter
import com.smox.smoxuser.ui.adapter.NewServiceAdapter
import com.smox.smoxuser.utils.listeners.OnItemClicked

class ServicesFragment : Fragment(), OnItemClicked, NewServiceAdapter.OnServiceClicked {

    private val TAG = "ServicesFragment"
    private lateinit var binding: FragmentServicesBinding
    private var catNameList = arrayListOf<String>()
    private var catItemList = arrayListOf<Category>()
    private var catServiceList = arrayListOf<Service>()
    lateinit var serviceAdapter: NewServiceAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentServicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments?.containsKey("catList") == true) {
            catItemList = requireArguments().getSerializable("catList") as ArrayList<Category>
            Log.e(TAG, "setServiceData: all items ${catItemList.size}")
            catNameList.clear()
            for (item in catItemList) {
                catNameList.add(item.cat_name.toString())
            }
        }
        setAdapter()
    }

    private fun setAdapter() {

        serviceAdapter = NewServiceAdapter(requireContext(), catServiceList, false, this)
        binding.rvServiceList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvServiceList.setHasFixedSize(true)
        binding.rvServiceList.adapter = serviceAdapter

        val categoryAdapter = CategoryNameAdapter(requireContext(), this, catNameList)
        binding.rvCategoryName.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategoryName.setHasFixedSize(true)
        binding.rvCategoryName.adapter = categoryAdapter

    }

    override fun onItemClick(pos: Int) {
        Log.e(TAG, "onItemClick: $pos")
        binding.tvNoDataFound.visibility = View.GONE
        catServiceList.clear()
        if (!catItemList[pos].services.isNullOrEmpty())
            catServiceList.addAll(catItemList[pos].services)
        else
            binding.tvNoDataFound.visibility = View.VISIBLE

        serviceAdapter.notifyDataSetChanged()

    }

    override fun onServiceClicked(pos: Int) {

    }
}
