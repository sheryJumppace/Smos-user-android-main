package com.smox.smoxuser.ui.fragment.barber

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.smox.smoxuser.R
import com.smox.smoxuser.ui.adapter.ServiceAdapter
import com.smox.smoxuser.ui.fragment.BaseFragment
import kotlinx.android.synthetic.main.activity_add_services.*

class ServiceFragment : BaseFragment() {

    companion object {
        public fun newInstance(position: Int):ServiceFragment {
            var serviceFragment = ServiceFragment()
            val args = Bundle()
            args.putInt("Pos",position)
            serviceFragment.setArguments(args)
            return serviceFragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view = inflater.inflate(R.layout.fragment_service, container, false)
        setAdapter()
        return view
    }

    private fun setAdapter() {

        val adapter = ServiceAdapter(isSmall = false, isSelect = true)
        service_list.adapter = adapter
//        subscribeUi(adapter)
    }
}