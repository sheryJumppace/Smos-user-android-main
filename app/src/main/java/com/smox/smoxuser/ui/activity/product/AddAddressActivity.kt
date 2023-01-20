package com.smox.smoxuser.ui.activity.product

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.android.volley.Request
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ActivityAddAddressBinding
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Address
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.utils.shortToast
import org.json.JSONObject
import java.util.*

class AddAddressActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityAddAddressBinding
    private val AUTOCOMPLETE_REQUEST_CODE: Int = 1
    private var addressLatitude: Double? = 0.0
    private var addressLongitude: Double? = 0.0
    private lateinit var addressDetail: Address
    private var isNewAddress: Boolean = true
    private var addressId: String = ""

    companion object {
        private const val ADDRESS_EDIT = 1011
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_address)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@ProductDetailActivity, Constants.backButton))
        }

        isNewAddress = intent.getBooleanExtra("IsNewAddress", true)

        when {
            isNewAddress -> {
                binding.toolbar.title = resources.getString(R.string.add_address)
            }
            else -> {
                binding.toolbar.title = resources.getString(R.string.update_address)
            }
        }

        if (intent.hasExtra("AddressDetail")) {
            addressDetail = intent.getSerializableExtra("AddressDetail") as Address
            binding.edtCustomerName.text =
                Editable.Factory.getInstance().newEditable(addressDetail.customer_name)
            binding.edtAddress.text =
                Editable.Factory.getInstance().newEditable(addressDetail.customer_address)

/*
            binding.edtPostalCode.text =
                Editable.Factory.getInstance().newEditable(addressDetail.postal_code)
*/

            addressId = addressDetail.id.toString()

            binding.btnSaveAddress.text = resources.getString(R.string.update_address)
        } else {
            addressDetail = Address()
        }

        binding.btnSaveAddress.setOnClickListener(this)
//        binding.edtPostalCode.isEnabled = false

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key), Locale.US)
        }

        binding.edtAddress.setOnTouchListener(object : View.OnTouchListener {
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (Places.isInitialized()) {
                            val fields: List<Place.Field> = Arrays.asList(
                                Place.Field.ID,
                                Place.Field.NAME,
                                Place.Field.ADDRESS,
                                Place.Field.ADDRESS_COMPONENTS,
                                Place.Field.LAT_LNG
                            )
                            var autoSearchIntent = Autocomplete.IntentBuilder(
                                AutocompleteActivityMode.FULLSCREEN,
                                fields
                            ).build(this@AddAddressActivity)
                            startActivityForResult(autoSearchIntent, AUTOCOMPLETE_REQUEST_CODE)
                        }
                    }
                }
                return v?.onTouchEvent(event) ?: true
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSaveAddress -> {
                if (!validation())
                    return

                saveAddress()
            }
        }
    }

    private fun validation(): Boolean {
        if (binding.edtCustomerName.text.toString().isEmpty()) {
            binding.edtCustomerName.error = resources.getString(R.string.err_customer_name)
            return false
        }
        if (binding.edtAddress.text.toString().isEmpty()) {
            binding.edtAddress.error = resources.getString(R.string.err_address)
            return false
        }

/*  commented as per client requirment   if (binding.edtPostalCode.text.toString().isEmpty()) {
            binding.edtPostalCode.error = resources.getString(R.string.err_postal_code)
            return false
        }*/

        return true
    }

    private fun saveAddress() {
        binding.btnSaveAddress.isEnabled = false
        val customer_name = binding.edtCustomerName.text
        val address = binding.edtAddress.text
//        val postal_code = binding.edtPostalCode.text

        val params = HashMap<String, String>()
        params["name"] = customer_name.toString()
        params["address"] = address.toString()
        params["latitude"] = addressLatitude.toString()
        params["longitude"] = addressLongitude.toString()
        params["postal_code"] = ""
        if (!isNewAddress) {
            params["id"] = addressId
        }

        progressHUD.show()

        APIHandler(
            applicationContext,
            Request.Method.POST,
            Constants.API.addedit_address,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    binding.btnSaveAddress.isEnabled = true
                    Log.i("-AddAddress-", result.toString())
                    progressHUD.dismiss()
                    val error: Boolean = result.getBoolean("error")
                    if (result.has("message")) {
                        shortToast(result.getString("message"))
                    }

                    if (!error) {
                        val intent = Intent()
                        intent.putExtra("IsAddressUpdate", true)
                        setResult(ADDRESS_EDIT, intent);
                        finish()
                    }

                }

                override fun onFail(error: String?) {
                    binding.btnSaveAddress.isEnabled = true
                    progressHUD.dismiss()
                    shortToast(error)
                }

            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!);

                addressLatitude = place.latLng?.latitude
                addressLongitude = place.latLng?.longitude

                binding.edtAddress.text = Editable.Factory.getInstance().newEditable(place.address)

                for (addressComponent in place.addressComponents!!.asList()) {
                    var typeList: List<String> = addressComponent.types
                    for (postalcode in typeList) {
                        if (postalcode.equals("postal_code")) {
//                            binding.edtPostalCode.text = Editable.Factory.getInstance().newEditable(addressComponent.name)
                        }
                    }
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                var status = Autocomplete.getStatusFromIntent(data!!)
                Log.i("address", status.getStatusMessage()!!);
            }
        }
    }

}
