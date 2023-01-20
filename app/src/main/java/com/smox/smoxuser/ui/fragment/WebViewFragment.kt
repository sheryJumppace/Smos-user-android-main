package com.smox.smoxuser.ui.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.smox.smoxuser.R
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.utils.shortToast
import org.json.JSONObject

class WebViewFragment constructor() : BaseFragment() {


    private lateinit var webView: WebView
    //val mAct = mActivity;
    private lateinit var mActivity: Activity

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_web_view, container, false)
        val url = arguments!!.getString("url")
        webView = v.findViewById<View>(R.id.webView) as WebView
        setUpWebView(url!!)
        return v
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView(url:String){
        /*webView.webViewClient = object: WebViewClient(){
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                Log.e("URL", url);
                progressHUD.show()
            }

            override fun onPageFinished(view: WebView, url: String) {
                Log.e("URL", url);
                progressHUD.dismiss()
            }
        }*/


        // Get the web view settings instance
        val settings = webView.settings

        // Enable java script in web view
        settings.javaScriptEnabled = true

        // Enable and setup web view cache
        settings.setAppCacheEnabled(true)
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        // Enable zooming in web view
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = true

        webView.setBackgroundColor(ContextCompat.getColor(context!!, R.color.transparent))

        webView.getSettings().setPluginState(WebSettings.PluginState.ON)
        webView.setWebChromeClient(WebChromeClient())
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                Log.e("URL", url);
                progressHUD.show()
            }

            override fun onPageFinished(view: WebView, url: String) {
                Log.e("URL", url);
                progressHUD.dismiss()
            }
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if(url!!.contains("https://smoxtrimsetters.com/?code")){
                    val stripeCodeUri : Uri = Uri.parse(url)
                    val stripeCode = stripeCodeUri.getQueryParameters("code").get(0)
                    connectStripeAccount(stripeCode)
                } else if(url!!.contains("https://connect.stripe.com/connect/default/oauth/test?code")){
                    val stripeCodeUri : Uri = Uri.parse(url)
                    val stripeCode = stripeCodeUri.getQueryParameters("code").get(0)
                    connectStripeAccount(stripeCode)
                } else if(url.contains("contactus@smoxtrimsetters.com")){
                    val stripeCodeUri : Uri = Uri.parse("mailto:"+url)
                    startActivity(Intent(Intent.ACTION_SENDTO, stripeCodeUri))
                } else {
                    view?.loadUrl(url)
                }

                return true
            }
        }
        webView.loadUrl(url)

    }
    companion object {

        fun newInstance(): WebViewFragment {
            return WebViewFragment()
        }
    }


    private fun connectStripeAccount(code: String){
        val params = HashMap<String, String>()
        params.put("code", code)

        progressHUD.show()

        APIHandler(context!!,
            Request.Method.POST,
            Constants.API.stripe_connect_account,
            params,
            object  : APIHandler.NetworkListener{
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    //{"error":false,"message":"Congrats! You've registered a payment method, Verifying any of your documents will take us 1 - 3 business days."}
                    val message = result.getString("message")

                    app.currentUser.connectAccount = false //Now onwards user change there key of account so
                    sessionManager.userData = app.currentUser.getJsonString()

                    shortToast(message)
                    requireActivity().finish()
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                }
            }
        )
    }
}
