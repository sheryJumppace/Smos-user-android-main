package com.smox.smoxuser.ui.activity.customer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.viewpager.widget.ViewPager
import com.android.volley.Request
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayout
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.smox.smoxuser.R
import com.smox.smoxuser.data.BarberRepository
import com.smox.smoxuser.databinding.ActivityDetailsBinding
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.manager.Constants.API.BARBER_ID
import com.smox.smoxuser.manager.Constants.API.CALLED_FROM
import com.smox.smoxuser.manager.Constants.API.DETAILS
import com.smox.smoxuser.model.Barber
import com.smox.smoxuser.model.Category
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.activity.auth.LoginActivity
import com.smox.smoxuser.ui.activity.product.ProductsActivity
import com.smox.smoxuser.ui.adapter.ViewPagerAdapter
import com.smox.smoxuser.ui.fragment.customer.*
import com.smox.smoxuser.utils.FULL_IMAGE_PATH
import com.smox.smoxuser.utils.shortToast
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


class DetailsActivity : BaseActivity() {
    lateinit var viewPager: ViewPager
    lateinit var tabLayout: TabLayout
    private var barberId: Int = 0
    private lateinit var barber: Barber
    var items: ArrayList<Category> = ArrayList();
    var ite = MutableLiveData<ArrayList<Category>>()
    lateinit var binding: ActivityDetailsBinding
    var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_details)
        viewPager = findViewById(R.id.pager)
        tabLayout = findViewById(R.id.tab_layout)

        barberId = intent.getIntExtra("barber_id",
            0) //barber = BarberRepository.getInstance().getBarber(barberId) ?: Barber()
        val barberRepository = BarberRepository.getInstance()
        barberRepository.barber.value=null
        barberRepository.barber.observe(this, androidx.lifecycle.Observer {
            if (it != null) {
                barber = it
                barber.apply {
                    binding.barber = this

                    val options: RequestOptions =
                        RequestOptions().placeholder(R.drawable.big_placeholder)
                            .error(R.drawable.big_placeholder)

                    val url = if (barber.image.startsWith("http")) barber.image
                    else FULL_IMAGE_PATH + barber.image

                    Glide.with(this@DetailsActivity).load(url).apply(options)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.imgProfile)

                    count++
                    setTabAdapters()
                }
            }
        })

        barberRepository.fetchBarberDetail(this, barberId)
        barberRepository.fetchBarberHoiliday(this, barberId)

        doRequestForCategoryList()

        binding.txtBookAppointment.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable("catList", items)

            startActivity(Intent(this, BookAppointmentActivity::class.java).putExtra("barber_id",
                    barberId).putExtra("barber", barber).putExtras(bundle))
        }

        clickListners()
    }

    private fun clickListners() {

        binding.imgFav.setOnClickListener {
            if (sessionManager.apiKey?.isNotEmpty()!!) {
                barber.isFavorite.set(!barber.isFavorite.get())
                updateFavoriteStatus()
            }else{
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
            }
        }
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
        binding.llCallNow.setOnClickListener {
            if (!barber.phone.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:" + barber.phone)
                startActivity(intent)
            }
        }
        binding.llWebsite.setOnClickListener {
            if (!barber.website.isNullOrEmpty()) {
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
                intent.data = Uri.parse(barber.website)
                startActivity(intent)
            }
            else shortToast("No url found.")
        }
        binding.llLocation.setOnClickListener {
            if (barber.latitude.toString().isNotEmpty()) {
                val uri = java.lang.String.format(Locale.ENGLISH,
                    "geo:%f,%f",
                    barber.latitude,
                    barber.longitude)
                val strUri =
                    "http://maps.google.com/maps?q=loc:" + barber.latitude + "," + barber.longitude + " (" + barber.business_name + ")"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(strUri))
                startActivity(intent)
            }
            else shortToast("No location found.")
        }
        binding.llShare.setOnClickListener {
            createDynamicLink()
        }
    }

    private fun updateFavoriteStatus() {
        val params = HashMap<String, String>()
        params["barber_id"] = barber.id.toString()
        params["favorite"] = if (barber.isFavorite.get()) "1" else "0"

        progressHUD.show()

        APIHandler(applicationContext,
            Request.Method.POST,
            Constants.API.favorite_barber,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)

                }
            })
    }

    private fun setTabAdapters() {

        if (count >= 2) {
            val adapter = ViewPagerAdapter(supportFragmentManager)

            val args = Bundle()
            args.putInt("barber_id", barberId)
            val aboutFragment = AboutFragment()
            aboutFragment.arguments = args

            val args1 = Bundle()
            args1.putInt("barber_id", barberId)
            args1.putSerializable("catList", items)
            val serviceFragment = ServicesFragment()
            serviceFragment.arguments = args1

            val galleryFragment = GalleryFragment()
            galleryFragment.arguments = args

            val reviewFragment = ReviewFragment()
            reviewFragment.arguments = args

            val prodBlankFragment = ProductBlankFragment()

            adapter.addFragment(aboutFragment, "About")
            adapter.addFragment(serviceFragment, "Services")
            adapter.addFragment(prodBlankFragment, "Products")
            adapter.addFragment(galleryFragment, "Gallery")
            adapter.addFragment(reviewFragment, "Review")
            viewPager.adapter = adapter //viewPager.offscreenPageLimit = 5
            tabLayout.setupWithViewPager(viewPager)

            viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int,
                                            positionOffset: Float,
                                            positionOffsetPixels: Int) {

                }

                override fun onPageSelected(position: Int) {
                    Log.e("TAG", "onPageSelected: $position")
                    if (position == 2) {
                        if (sessionManager.apiKey?.isNotEmpty()!!){
                            startActivity(Intent(this@DetailsActivity,
                                ProductsActivity::class.java).putExtra(BARBER_ID, barberId)
                                .putExtra(CALLED_FROM, DETAILS))
                            viewPager.currentItem = 0
                        }else{
                            val intent = Intent(this@DetailsActivity, LoginActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
                        }
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {

                }
            })
        }

    }

    private fun doRequestForCategoryList() {
        progressHUD.show()
        val params = HashMap<String, String>()
        APIHandler(this,
            Request.Method.GET,
            Constants.API.get_category + "/" + barberId,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()

                    items.clear()
                    if (result.has("result")) {
                        val jsonArray = result.getJSONArray("result")
                        for (i in 0 until jsonArray.length()) {
                            val json = jsonArray.getJSONObject(i)
                            val category = Category(json)
                            items.add(category)
                        }
                        count++
                        setTabAdapters()
                    }
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)

                }
            })
    }

    private fun createDynamicLink() {
        var dynamicLinkUri = ""
        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse("https://www.smox.page.link/open?barberId=$barberId"))
            .setDomainUriPrefix("https://smox.page.link") // Open links with this app on Android
            .setAndroidParameters(DynamicLink.AndroidParameters.Builder("com.smox.smoxuser")
                .build()) // Open links with com.example.ios on iOS
            .setIosParameters(DynamicLink.IosParameters.Builder("com.smox.smoxuser").build())
            .setSocialMetaTagParameters(DynamicLink.SocialMetaTagParameters.Builder()
                .setTitle("Amazing styler near you... " + barber.business_name)
                .setDescription(barber.business_name + " Amazing styler. Checkout here...")
                .setImageUrl(Uri.parse("https://i.ibb.co/qy04HPC/Smox-Trimsetters-atom-July-15-2021.png"))
                .build()).buildShortDynamicLink().addOnSuccessListener { shortDynamicLink ->
                dynamicLinkUri = shortDynamicLink.shortLink.toString()
                Log.e("AAA", "test1 success $dynamicLinkUri")

                openSharingDialog(dynamicLinkUri)

                //shareIntent(mInvitationUrl)
            }.addOnFailureListener {
                Log.e("AAA", "test1 fail")
                it.printStackTrace()
            }
    }

    private fun openSharingDialog(dynamicLink: String) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        val shareBody =
            "Please checkout the salon " + barber.business_name + " from " + barber.firstName + " " + barber.lastName + ". Address: " + barber.address + ". " + dynamicLink
        val shareSub = ""
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub)
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(sharingIntent, "Share using"))
    }

}