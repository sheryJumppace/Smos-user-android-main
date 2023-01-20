package com.smox.smoxuser.ui.fragment.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.smox.smoxuser.data.EventRepository
import com.smox.smoxuser.databinding.FragmentEventsBinding
import com.smox.smoxuser.model.Event
import com.smox.smoxuser.ui.adapter.EventAdapter
import com.smox.smoxuser.ui.fragment.BaseFragment
import com.smox.smoxuser.utils.listeners.OnItemClicked
import com.smox.smoxuser.viewmodel.EventListViewModel
import com.smox.smoxuser.viewmodel.EventListViewModelFactory
import kotlinx.android.synthetic.main.fragment_events.*
import java.util.ArrayList

class EventsFragment : BaseFragment(), OnItemClicked {
    private lateinit var viewModel: EventListViewModel
    var binding: FragmentEventsBinding? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = EventListViewModelFactory(EventRepository.getInstance())
        viewModel = ViewModelProviders.of(this, factory).get(EventListViewModel::class.java)


        val adapter = EventAdapter(requireContext(), this)
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding!!.eventList.layoutManager = layoutManager
        binding!!.eventList.adapter = adapter

        subscribeUi(adapter)
    }

    private fun subscribeUi(adapter: EventAdapter) {
        updateData()
        viewModel.events.observe(this, Observer { events ->
            binding!!.progressBar.visibility = View.GONE

            if (events != null) {
                if (events.isNotEmpty()) {
                    adapter.setData(events as ArrayList<Event>)
                    txtNoEvent.visibility = View.GONE
                    binding!!.eventList.visibility = View.VISIBLE
                } else {
                    txtNoEvent.visibility = View.VISIBLE
                    binding!!.eventList.visibility = View.GONE
                }

            }
        })
    }

    private fun updateData() {
        viewModel.fetchList(activity!!,1)
    }

    override fun onItemClick(pos: Int) {

    }
}
