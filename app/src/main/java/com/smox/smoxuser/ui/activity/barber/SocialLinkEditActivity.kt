package com.smox.smoxuser.ui.activity.barber

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.URLUtil
import android.widget.Toast
import com.android.volley.Request
import com.smox.smoxuser.R
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.type.SocialLinkType
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.utils.shortToast

import kotlinx.android.synthetic.main.activity_social_link_edit.*
import org.json.JSONObject
import java.util.HashMap

class SocialLinkEditActivity : BaseActivity() {
    private lateinit var type:SocialLinkType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_social_link_edit)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@SocialLinkEditActivity, Constants.backButton))
        }

        updateUI()

    }

    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_save -> {
                updateLink()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateUI() {
        type = SocialLinkType.valueOf(intent.getStringExtra("type")!!)
        val icon:Int
        val link:String
        val hint:String
        val title:String
        val barber = app.currentUser
        when(type){
            SocialLinkType.Facebook -> {
                icon = R.drawable.ic_facebook
                link = barber.facebook
                hint = "https://facebook.com/"
                title = "Facebook"
            }
            SocialLinkType.Twitter -> {
                icon = R.drawable.ic_twitter_logo
                link = barber.twitter
                hint = "https://twitter.com/"
                title = "Twitter"
            }
            SocialLinkType.Instagram -> {
                icon = R.drawable.ic_instagram
                link = barber.instagram
                hint = "https://instagram.com/"
                title = "Instagram"
            }
            SocialLinkType.Youtube -> {
                icon = R.drawable.youtube
                link = barber.youtube
                hint = "https://www.youtube.com/"
                title = getString(R.string.youtube)
            }
            SocialLinkType.Linkedin -> {
                icon = R.drawable.linkedin
                link = barber.linkedin
                hint = "https://www.linkedin.com/"
                title = getString(R.string.linkedin)
            }
        }

        imgSocial.setImageResource(icon)
        txtLink.hint = hint
        txtLink.setText(link)
        toolbar_layout.title = title
    }

    private fun updateLink() {
        val link = txtLink.text.toString()
        if (!URLUtil.isValidUrl(link)){
            txtLink.error = "Please type your profile link"
            return
        }
        val params = HashMap<String, String>()
        params["link"] = link
        params["type"] = type.name

        progressHUD.show()
        APIHandler(
            applicationContext,
            Request.Method.PUT,
            Constants.API.social_link,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    val barber = app.currentUser
                    when(type){
                        SocialLinkType.Facebook -> barber.facebook = link
                        SocialLinkType.Twitter -> barber.twitter = link
                        SocialLinkType.Instagram -> barber.instagram = link
                        SocialLinkType.Youtube -> barber.youtube = link
                        SocialLinkType.Linkedin -> barber.linkedin = link
                    }

                    sessionManager.userData = barber.getJsonString()
                    finish()
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }
}
