package com.smox.smoxuser.ui.activity.barber

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.smox.smoxuser.R
import com.smox.smoxuser.data.ReviewRepository
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.adapter.ReviewAdapter
import com.smox.smoxuser.viewmodel.ReviewListViewModel
import com.smox.smoxuser.viewmodel.ReviewListViewModelFactory
import kotlinx.android.synthetic.main.activity_reviews.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.smox.smoxuser.model.type.UserType
import com.smox.smoxuser.ui.dialog.RatingDialog

class ReviewsActivity : BaseActivity() {
    private lateinit var viewModel: ReviewListViewModel
    private var barberId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviews)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@ReviewsActivity, Constants.backButton))
        }

        barberId = intent.getIntExtra("barber_id", 0)

        val factory = ReviewListViewModelFactory(ReviewRepository.getInstance())
        viewModel = ViewModelProviders.of(this, factory).get(ReviewListViewModel::class.java)
        viewModel.setStartPageIndex(0)
        val adapter = ReviewAdapter()
        val layoutManager = review_list.layoutManager as LinearLayoutManager
        review_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {

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
        review_list.adapter = adapter
        subscribeUi(adapter)
    }


    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_edit -> {
                openRatingView()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openRatingView() {
        val dialog = RatingDialog(this)
        dialog.show()

        /*dialog.confirmButton.setOnClickListener {
            val comment = dialog.valueEditText.text.toString()
            val rating = dialog.ratingBar.rating
            if (comment.isNotEmpty()) {
                viewModel.addReview(this, comment, rating = rating.toInt(), barberId = barberId)
            }
            dialog.dismiss()
        }*/
    }

    private fun subscribeUi(adapter: ReviewAdapter) {
        updateData()
        viewModel.reviews.observe(this, Observer { reviews ->

            if (reviews.isNotEmpty()) {
                adapter.submitList(reviews)
                adapter.notifyDataSetChanged()
                tvNotFound.visibility = View.GONE
            } else {
                tvNotFound.visibility = View.VISIBLE
            }

        })
    }

    private fun updateData() {
        with(viewModel) {
            fetchList(this@ReviewsActivity, barberId)
        }
    }
}
