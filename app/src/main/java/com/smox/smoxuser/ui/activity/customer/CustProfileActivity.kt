package com.smox.smoxuser.ui.activity.customer

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.android.volley.Request
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.kaopiz.kprogresshud.KProgressHUD
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ActivityCustProfileBinding
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.manager.ObservingService
import com.smox.smoxuser.manager.SessionManager
import com.smox.smoxuser.model.SimpleOkResponse2
import com.smox.smoxuser.retrofit.ApiRepository
import com.smox.smoxuser.ui.activity.BaseImagePickerActivity
import com.smox.smoxuser.ui.activity.HomeActivity
import com.smox.smoxuser.ui.activity.home.Home2Activity
import com.smox.smoxuser.ui.activity.orders.OrdersActivity
import com.smox.smoxuser.utils.ImageUploadUtils
import com.smox.smoxuser.utils.listeners.UploadImages
import com.smox.smoxuser.utils.shortToast
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_add_card.*
import kotlinx.android.synthetic.main.activity_signup.*
import org.json.JSONObject
import retrofit2.HttpException
import java.util.*

class CustProfileActivity : BaseImagePickerActivity(), UploadImages {

    private val TAG = "CustProfileActivity"
    lateinit var binding: ActivityCustProfileBinding
    private val RC_SIGN_IN = 123
    private val RC_ADDRESS = 520

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("++--++", "CustProfileActivity is called")


        binding = DataBindingUtil.setContentView(this, R.layout.activity_cust_profile)

        val user = app.currentUser
        Glide.with(this).load(user.image).apply(
            RequestOptions().placeholder(R.drawable.small_placeholder)
                .error(R.drawable.small_placeholder).dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        ).into(binding.profilePic)
        binding.etFirstName.setText(user.firstName)
        binding.etLastName.setText(user.lastName)
        binding.etEmail.isEnabled = false
        binding.etEmail.setText(user.email)
        binding.etPhone.setText(user.phone)
        binding.etAddress.setText(user.address)

        if (sessionManager.isSocialLogin) {
            binding.llChangePwd.visibility = View.GONE
        } else binding.llChangePwd.visibility = View.VISIBLE

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key), Locale.US)
        }

        binding.txtVerifyNum.setOnClickListener {
            doPhoneNumberVerification()
        }

        binding.imgChooseImage.setOnClickListener {
            didOpenPhotoOption()
        }

        binding.etChangePwd.setOnClickListener {
            startActivity(Intent(this, UpdatePasswordActivity::class.java))
        }

        binding.txtUpdateFName.setOnClickListener {
            if (isValid(binding.etFirstName.text.toString())) {
                callUpdateName(user.lastName, binding.etFirstName.text.toString())
            }

        }
        binding.txtUpdateLName.setOnClickListener {
            if (isValid(binding.etLastName.text.toString())) {
                callUpdateName(binding.etLastName.text.toString(), user.firstName)
            }
        }

        binding.editAddress.setOnClickListener {
            launchLocationAutoCompleteActivity()
        }
        binding.etAddress.setOnClickListener {
            launchLocationAutoCompleteActivity()
        }

        binding.btnDeleteAcc.setOnClickListener {
            AlertDialog.Builder(this).setTitle(getString(R.string.deleteAcc)).setCancelable(false)
                .setMessage(getString(R.string.deleteAccMsg))
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    deleteMyAccount()
                }.setNegativeButton(getString(R.string.cancel)) { _, _ ->

                }.create().show()
        }

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun callUpdateName(lastName: String, firstName: String) {
        val params = HashMap<String, String>()
        params["first_name"] = firstName
        params["last_name"] = lastName
        val progressHUD = KProgressHUD(this)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE).setCancellable(true)
            .setAnimationSpeed(2).setDimAmount(0.5f)
        progressHUD.show()

        APIHandler(this,
            Request.Method.PUT,
            Constants.API.updateName,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    val message: String = result.getString("message")
                    shortToast(message)
                    progressHUD.dismiss()

                    if (!result.getBoolean("error")) {
                        app.currentUser.firstName = firstName
                        app.currentUser.lastName = lastName
                        updateSessionUser()
                    }
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    private fun isValid(text: String): Boolean {
        if (text.isEmpty()) {
            shortToast(getString(R.string.err_valid_name))
            return false
        }

        return true
    }

    private fun doPhoneNumberVerification() {
        val providers = Arrays.asList<AuthUI.IdpConfig>(
//            AuthUI.IdpConfig.PhoneBuilder().setDefaultNumber(app.currentUser.phone).build()
            AuthUI.IdpConfig.PhoneBuilder().build()
        )
        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setTheme(R.style.FirebasePhoneAuth)
                .setAvailableProviders(providers).build(), RC_SIGN_IN
        )
    }

    @SuppressLint("RestrictedApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                val response = IdpResponse.fromResultIntent(data)
                if (response != null) {
                    val phone = response.user.phoneNumber.toString()
                    updatePhoneNumber(phone)
                }
            } else {
                val response = IdpResponse.fromResultIntent(data)
                if (response != null) {
                    val error = response.error
                    if (error != null) {
                        shortToast(response.error!!.localizedMessage)
                    }
                    btnVerify.text = resources.getText(R.string.verify)
                }
            }
        } else if (requestCode == RC_ADDRESS && resultCode == RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data!!)
            place.latLng
            updateAddress(place.name!!, place.latLng!!)
        }
    }

    private fun updateAddress(address: String, latLng: LatLng) {
        val params = HashMap<String, String>()
        params["lat"] = latLng.latitude.toString()
        params["lng"] = latLng.longitude.toString()
        params["address"] = address
        val progressHUD = KProgressHUD(this)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE).setCancellable(true)
            .setAnimationSpeed(2).setDimAmount(0.5f)
        progressHUD.show()

        APIHandler(this,
            Request.Method.PUT,
            Constants.API.updateAddress,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    Log.d("++--++","CustProfileActivity updateAddress 227 \n result : $result")


                    val message: String = result.getString("message")
                    shortToast(message)
                    progressHUD.dismiss()
                    if (!result.getBoolean("error")) {
                        binding.etAddress.setText(address)
                        app.currentUser.address = address
                        app.currentUser.latitude = latLng.latitude
                        app.currentUser.longitude = latLng.longitude
                        updateSessionUser()
                    }
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)

                    Log.d("++--++","error : $error")
                }
            })
    }

    private fun updatePhoneNumber(phone: String) {
        binding.etPhone.setText(phone)
        val params = HashMap<String, String>()
        params["phone"] = phone
        val progressHUD = KProgressHUD(this)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE).setCancellable(true)
            .setAnimationSpeed(2).setDimAmount(0.5f)
        progressHUD.show()

        APIHandler(this,
            Request.Method.PUT,
            Constants.API.phone,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    val message: String = result.getString("message")
                    shortToast(message)
                    progressHUD.dismiss()
                    app.currentUser.phone = phone
                    updateSessionUser()
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    override fun didSelectPhoto(uri: Uri) {
        super.didSelectPhoto(uri)
        Glide.with(this).load(uri).into(binding.profilePic)


        Log.e(TAG, "didSelectPhoto: ${uri.path}")
        progressHUD.show()
        val imageUploadUtils = ImageUploadUtils()
        imageUploadUtils.onUpload(
            this, uri.path!!, this
        )

        //updatePhoto()

    }

    private fun updatePhoto(imageUrl: String) {

        val params = HashMap<String, String>()
        params["image"] = imageUrl

        APIHandler(this,
            Request.Method.POST,
            Constants.API.upload_profile,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    val message: String = result.getString("message")
                    val error: Boolean = result.getBoolean("error")
                    shortToast(message)
                    progressHUD.dismiss()
                    if (!error) {
                        app.currentUser.image = imageUrl
                        updateSessionUser()
                    }
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })

    }

    private fun updateSessionUser() {
        sessionManager.userData = app.currentUser.getJsonString()
    }

    override fun upload(imageUrl: String) {

        Log.e(TAG, "upload: $imageUrl")
        updatePhoto(imageUrl)
    }

    override fun uploadError() {
        progressHUD.dismiss()
        shortToast("Image upload error ")

    }

    private fun launchLocationAutoCompleteActivity() {
        Places.initialize(applicationContext, getString(R.string.google_maps_key), Locale.US)


        if (Places.isInitialized()) {
            val fields: List<Place.Field> = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.ADDRESS_COMPONENTS,
                Place.Field.LAT_LNG
            )
            var autoSearchIntent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields
            ).build(this@CustProfileActivity)
            startActivityForResult(autoSearchIntent, RC_ADDRESS)
        }

    }

    private fun deleteMyAccount() {
        progressHUD.show()
        ApiRepository(this).deleteAccount().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<SimpleOkResponse2> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(res: SimpleOkResponse2) {
                    progressHUD.dismiss()
                    if (!res.error) {
                        shortToast(res.message)
                        val sessionManager = SessionManager.getInstance(applicationContext)
                        sessionManager.userData = ""
                        sessionManager.apiKey = ""
                        sessionManager.isSubscribed = false
                        sessionManager.subscription_enddate = ""
                        sessionManager.Sp_publishableKey = ""
                        sessionManager.Sp_privateKey = ""
                        val intent = Intent(this@CustProfileActivity, HomeActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    } else shortToast(res.message)
                }

                override fun onError(e: Throwable) {
                    progressHUD.dismiss()
                    Log.e("TAG", "onError: ${e.message}")
                    if ((e as HttpException).code() == 401) {
                        shortToast(getString(R.string.authError))
                        APIHandler(this@CustProfileActivity).logout()
                    } else shortToast(e.message())
                }

                override fun onComplete() {

                }

            })
    }


}