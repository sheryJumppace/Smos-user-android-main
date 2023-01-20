package com.smox.smoxuser.ui.activity.customer

import android.os.Bundle
import android.text.Html
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.volley.Request
import com.kaopiz.kprogresshud.KProgressHUD
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ActivityAboutUsBinding
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.utils.shortToast
import org.json.JSONObject
import java.util.*

class AboutUsActivity : AppCompatActivity() {
    lateinit var binding: ActivityAboutUsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_about_us)

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
        callAboutUsData()
    }

    private fun callAboutUsData() {
        val progressHUD = KProgressHUD(this)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        progressHUD.show()

        val params = HashMap<String, String>()

        APIHandler(
            this,
            Request.Method.GET,
            Constants.API.aboutUs,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    val aboutUsData = result.getString("result")
                    Log.e("TAG", "about us onResult: $aboutUsData")
                    if (!result.getBoolean("error")) {
                        binding.txtAboutUs.text =
                            Html.fromHtml(aboutUsData, Html.FROM_HTML_MODE_COMPACT);
                    } else {
                        binding.txtAboutUs.text = Html.fromHtml(aboutUsData);
                    }

                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }
}