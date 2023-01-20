package com.smox.smoxuser.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.smox.smoxuser.R
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Cards
import com.smox.smoxuser.ui.adapter.CardAdapter
import com.smox.smoxuser.utils.shortToast
import kotlinx.android.synthetic.main.activity_booking_appointment.toolbar
import kotlinx.android.synthetic.main.activity_card_list.*
import org.json.JSONObject
import java.io.Serializable

class CardListActivity : BaseActivity(), CardAdapter.CardActions {


    private lateinit var cardAdapter: CardAdapter
    val items: ArrayList<Cards> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_list)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            toolbar.title = "PAYMENT"
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@BookingAppointmentActivity, Constants.backButton))
        }

        btnAddCard.setOnClickListener {
            val intent = Intent(this@CardListActivity, AddCardActivity::class.java)
            startActivity(intent)
        }
        btnCheckOut.setOnClickListener {
            if(items.size>0) {
                val intent = Intent()
                val bundle = Bundle()
                bundle.putSerializable(
                    "card",
                    cardAdapter.getCard() as Serializable?
                )
                intent.putExtras(bundle)
                setResult(RESULT_OK, intent)
                finish()
            }else{
                shortToast(getString(R.string.no_card_selected))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        doRequestForCardDetails()
    }

    private fun doRequestForCardDetails() {
        progressHUD.show()
        val params = HashMap<String, String>()
        APIHandler(
            this,
            Request.Method.GET,
            Constants.API.getlistcard,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()

                    items.clear()
                    if (result.has("result")) {
                        val jsonArray = result.getJSONArray("result")

                        for (i in 0 until jsonArray.length()) {
                            val json = jsonArray.getJSONObject(i)
                            val card = Cards(json)
                            items.add(card)
                        }

                        setAdapter(items)
                    }
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    private fun setAdapter(items: ArrayList<Cards>) {
        cardAdapter = CardAdapter(this, this)
            card_list.layoutManager= LinearLayoutManager(this)
            card_list.setHasFixedSize(true)
            card_list.adapter=cardAdapter
        cardAdapter.doRefresh(items)
    }

    override fun onDeleteClick(pos: Int) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Warning!")
        builder.setMessage("Are you sure you want to delete this card?")
        builder.setPositiveButton("Ok") { _, _ ->
            doReuestForDelete(pos)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun doReuestForDelete(pos: Int) {
        progressHUD.show()
        val params = HashMap<String, String>()
        params["user_card_id"]=items.get(pos).UserCardId.toString()
        APIHandler(
            applicationContext,
            Request.Method.POST,
            Constants.API.deletecard,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    shortToast(result.getString("message"))
                    if (!(result.getBoolean("error"))) {
                        items.removeAt(pos)
                        cardAdapter.doRefresh(items)
                    }

                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }
}