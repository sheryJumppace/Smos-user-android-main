package com.smox.smoxuser.ui.activity.barber

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.smox.smoxuser.R
import com.smox.smoxuser.model.type.SocialLinkType
import com.smox.smoxuser.ui.activity.BaseActivity

import kotlinx.android.synthetic.main.activity_social_media.*

class SocialMediaActivity : BaseActivity(), View.OnClickListener {
    override fun onClick(v: View?) {
        val type = when(v?.id){
            R.id.btnFacebook-> {
                btnFacebook.isEnabled = false
                SocialLinkType.Facebook
            }
            R.id.btnTwitter-> {
                btnTwitter.isEnabled = false
                SocialLinkType.Twitter
            }
            R.id.btnInstagram-> {
                btnInstagram.isEnabled = false
                SocialLinkType.Instagram
            }
            R.id.btnYoutube-> {
                btnYoutube.isEnabled = false
                SocialLinkType.Youtube
            }
            R.id.btnLinkedin -> {
                btnLinkedin.isEnabled = false
                SocialLinkType.Linkedin
            }
            else -> SocialLinkType.Facebook
        }

        openEditPage(type)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_social_media)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@SocialMediaActivity, Constants.backButton))
        }

        btnFacebook.setOnClickListener(this)
        btnInstagram.setOnClickListener(this)
        btnTwitter.setOnClickListener(this)
        btnYoutube.setOnClickListener(this)
        btnLinkedin.setOnClickListener(this)
    }

    private fun openEditPage(type:SocialLinkType) {
        val intent = Intent(this@SocialMediaActivity, SocialLinkEditActivity::class.java)
        intent.putExtra("type", type.name)
        startActivity(intent)
        overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
    }

    override fun onResume() {
        super.onResume()
        btnFacebook.isEnabled = true
        btnTwitter.isEnabled = true
        btnInstagram.isEnabled = true
        btnYoutube.isEnabled = true
        btnLinkedin.isEnabled = true
    }
}
