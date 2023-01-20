package com.smox.smoxuser.ui.activity.barber

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import com.smox.smoxuser.R
import com.smox.smoxuser.ui.activity.BaseActivity

import kotlinx.android.synthetic.main.activity_bio.*
import android.text.InputFilter
import android.widget.Toast
import com.android.volley.Request
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.utils.shortToast
import kotlinx.android.synthetic.main.activity_bio.toolbar
import org.json.JSONObject
import java.util.HashMap


class BioActivity : BaseActivity() {
    companion object {
        private const val MAX_NUM = 250
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bio)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@BioActivity, Constants.backButton))
        }

        initUI()
    }

    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_save -> {
                updateBio()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initUI() {
        txtBio.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(MAX_NUM))
        txtBio.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                setCounterLabel()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
        txtBio.setText(app.currentUser.bio)
        setCounterLabel()
    }

    private fun setCounterLabel() {
        txtCounter.text = String.format("%d Left", MAX_NUM - txtBio.text.toString().length)
    }

    private fun updateBio() {
        val bio = txtBio.text.toString()
        if(bio.isEmpty()){
            txtBio.setError(resources.getString(R.string.err_short_bio))
            return
        }

        val params = HashMap<String, String>()
        params["bio"] = bio

        progressHUD.show()
        APIHandler(
            applicationContext,
            Request.Method.PUT,
            Constants.API.user_bio,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    val barber = app.currentUser
                    barber.bio = bio
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
