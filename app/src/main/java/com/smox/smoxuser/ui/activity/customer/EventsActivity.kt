package com.smox.smoxuser.ui.activity.customer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.smox.smoxuser.R
import com.smox.smoxuser.data.EventRepository
import com.smox.smoxuser.databinding.ActivityEventsBinding
import com.smox.smoxuser.manager.Constants.API.EVENT_SELECTED
import com.smox.smoxuser.model.Event
import com.smox.smoxuser.ui.adapter.EventAdapter
import com.smox.smoxuser.utils.PaginationScrollListner
import com.smox.smoxuser.utils.listeners.OnItemClicked
import com.smox.smoxuser.viewmodel.EventListViewModel
import com.smox.smoxuser.viewmodel.EventListViewModelFactory
import java.util.*

class EventsActivity : AppCompatActivity(), OnItemClicked {

    private val TAG = "EventsActivity"
    lateinit var binding: ActivityEventsBinding
    private lateinit var viewModel: EventListViewModel
    private val totalPage = 500
    private var pageStart = 0
    private var isLastPagee = false
    var isLoadingg = false
    var eventListMain = arrayListOf<Event>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_events)

        val factory = EventListViewModelFactory(EventRepository.getInstance())
        viewModel = ViewModelProviders.of(this, factory).get(EventListViewModel::class.java)

        val adapter = EventAdapter(this, this)
        val layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvEventList.layoutManager = layoutManager
        binding.rvEventList.adapter = adapter

        subscribeUi(adapter)

        callEventListData()

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            isLastPagee = false
            pageStart = 0
            eventListMain.clear()
            adapter.clearList()
            viewModel.clearData()
            callEventListData()
        }

        binding.rvEventList.addOnScrollListener(object : PaginationScrollListner(layoutManager) {
            override fun loadMoreItems() {
                if (!isLastPagee) {
                    callEventListData()
                }
            }

            override fun getTotalPageCount(): Int {
                return totalPage
            }

            override fun isLastPage(): Boolean {
                return isLastPagee
            }

            override fun isLoading(): Boolean {
                return isLoadingg
            }
        })

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun callEventListData() {
        if (!this.isLastPagee) {
            isLoadingg = true
            pageStart += 1
            if (pageStart != 1) {
                binding.bottomProgressBar.visibility = View.VISIBLE
            }
            Log.e(TAG, "loadMoreItems: called pageStart: $pageStart   isLastPage: $isLastPagee")

            viewModel.fetchList(this, pageStart)
        }
    }

    private fun subscribeUi(adapter: EventAdapter) {

        viewModel.events.observe(this, Observer { events ->
            if (events != null) {
                binding.bottomProgressBar.visibility = View.GONE
                if (events.isNotEmpty()) {
                    isLastPagee = false
                    eventListMain.addAll(events)
                } else isLastPagee = true
                isLoadingg = false

                if (pageStart == 1 && events.isEmpty()) {
                    binding.rvEventList.visibility = View.GONE
                    binding.txtNoEvent.visibility = View.VISIBLE
                } else {
                    binding.rvEventList.visibility = View.VISIBLE
                    binding.txtNoEvent.visibility = View.GONE
                }

                adapter.setData(events as ArrayList<Event>)
            }
        })
    }

    override fun onItemClick(pos: Int) {

        val intent = Intent(this@EventsActivity, EventDetailsActivity::class.java)
        intent.putExtra(EVENT_SELECTED, eventListMain[pos])
        startActivity(intent)

    }
}