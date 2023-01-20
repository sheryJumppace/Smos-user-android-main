package com.smox.smoxuser.ui.fragment.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smox.smoxuser.data.ReviewRepository
import com.smox.smoxuser.databinding.FragmentReviewBinding
import com.smox.smoxuser.model.Review
import com.smox.smoxuser.ui.adapter.NewReviewAdapter
import com.smox.smoxuser.viewmodel.ReviewListViewModel
import com.smox.smoxuser.viewmodel.ReviewListViewModelFactory
import kotlinx.android.synthetic.main.activity_reviews.*

class ReviewFragment : Fragment() {
    private lateinit var viewModel: ReviewListViewModel
    private var barberId: Int = 0
    var reviewList = arrayListOf<Review>()

    lateinit var binding: FragmentReviewBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barberId = requireArguments().getInt("barber_id")

        val factory = ReviewListViewModelFactory(ReviewRepository.getInstance())
        viewModel = ViewModelProviders.of(this, factory).get(ReviewListViewModel::class.java)
        viewModel.setStartPageIndex(0)
        val adapter = NewReviewAdapter(reviewList)
        val layoutManager = binding.rvReview.layoutManager as LinearLayoutManager
        binding.rvReview.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastCompletelyVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                viewModel.getReviewsCount().apply {
                    if (lastCompletelyVisibleItemPosition == this - 1) {
                        updateData()
                    }
                }
            }
        })
        binding.rvReview.adapter = adapter
        subscribeUi(adapter)
    }

    private fun subscribeUi(adapter: NewReviewAdapter) {
        updateData()
        viewModel.reviews.observe(requireActivity(), Observer { reviews ->

            if (reviews.isNotEmpty()) {
                reviewList.addAll(reviews)
                adapter.notifyDataSetChanged()
                binding.tvNoDataFound.visibility = View.GONE
                binding.rvReview.visibility=View.VISIBLE
            } else {
                binding.tvNoDataFound.visibility = View.VISIBLE
                binding.rvReview.visibility=View.GONE
            }
        })
    }

    private fun updateData() {
        with(viewModel) {
            fetchList(requireActivity(), barberId)
        }
    }
}