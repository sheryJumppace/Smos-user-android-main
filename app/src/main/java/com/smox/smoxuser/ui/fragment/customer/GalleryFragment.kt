package com.smox.smoxuser.ui.fragment.customer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.smox.smoxuser.data.BarberRepository
import com.smox.smoxuser.databinding.FragmentGalleryBinding
import com.smox.smoxuser.model.Barber
import com.smox.smoxuser.ui.activity.customer.FullScreenImageActivity
import com.smox.smoxuser.ui.adapter.GalleryAdapter
import com.smox.smoxuser.utils.FULL_IMAGE_PATH


class GalleryFragment : Fragment(), GalleryAdapter.OnGalleryItemClicked {
    lateinit var binding: FragmentGalleryBinding
    var barberId = 0
    private lateinit var barber: Barber
    lateinit var galleryAdapter: GalleryAdapter
    var urlList = arrayListOf<String>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barberId = requireArguments().getInt("barber_id")

        Log.e(
            "TAG",
            "GALLLERY  onViewCreated: " + BarberRepository.getInstance().barber.value?.firstName
        )

        //val serviceList = barber.getDetailedService()
        val serviceList = BarberRepository.getInstance().barber.value?.services
        urlList.clear()
        if (!serviceList.isNullOrEmpty()) {
            for (items in serviceList) {
                if (items.image.contains("http"))
                    urlList.add(items.image)
                else
                    urlList.add(FULL_IMAGE_PATH + items.image)
            }
        }

        galleryAdapter = GalleryAdapter(requireActivity(), urlList, this)
        binding.rvGallery.setHasFixedSize(true)
        binding.rvGallery.adapter = galleryAdapter

        if (urlList.isEmpty()) {
            binding.rvGallery.visibility = View.GONE
            binding.tvNoDataFound.visibility = View.VISIBLE
        } else {
            binding.rvGallery.visibility = View.VISIBLE
            binding.tvNoDataFound.visibility = View.GONE
        }
    }

    override fun onGalleryItemClicked(pos: Int) {
        val fullImageIntent = Intent(requireContext(), FullScreenImageActivity::class.java)
        fullImageIntent.putStringArrayListExtra("URI", urlList)
        fullImageIntent.putExtra("Pos", pos)
        startActivity(fullImageIntent)
    }
}