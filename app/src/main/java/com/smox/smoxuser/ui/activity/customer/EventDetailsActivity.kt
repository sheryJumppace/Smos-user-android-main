package com.smox.smoxuser.ui.activity.customer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ActivityEventDetailsBinding
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Event
import com.smox.smoxuser.utils.FULL_IMAGE_PATH

class EventDetailsActivity : AppCompatActivity() {
    lateinit var binding: ActivityEventDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_details)

        val event: Event = intent.getSerializableExtra(Constants.API.EVENT_SELECTED) as Event

        binding.txtTitle.text = event.name
        binding.startdate.text = event.getStart()
        binding.txtEndDate.text = event.getEnd()
        binding.txtDetails.text=event.event


        val options: RequestOptions =
            RequestOptions().placeholder(R.drawable.small_placeholder)
                .error(R.drawable.small_placeholder)

        val url = if (event.image.startsWith("http"))
            event.image
        else
            FULL_IMAGE_PATH + event.image

        Glide.with(this)
            .load(url)
            .apply(options)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.imgProfile)

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

    }
}