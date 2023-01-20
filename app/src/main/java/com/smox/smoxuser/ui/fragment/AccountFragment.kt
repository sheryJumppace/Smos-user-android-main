package com.smox.smoxuser.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.smox.smoxuser.R
import de.hdodenhof.circleimageview.CircleImageView

import java.io.File

import android.app.Activity.RESULT_OK
import android.net.Uri
import android.widget.EditText
import com.android.volley.Request
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.kaopiz.kprogresshud.KProgressHUD
import com.quickblox.users.QBUsers
import com.smox.smoxuser.databinding.FragmentAccountBinding
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.type.UserType
import com.smox.smoxuser.utils.SharedPrefsHelper
import com.smox.smoxuser.utils.shortToast
import kotlinx.android.synthetic.main.activity_signup.btnVerify
import org.json.JSONObject
import java.util.*


class AccountFragment : BaseImagePickerFragment() {

    private lateinit var imgPhoto: CircleImageView
    private lateinit var txtFirstName: EditText
    private lateinit var txtLastName: EditText
    private lateinit var txtPhone: EditText


    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        /*(activity as BarberMainActivity).supportActionBar?.apply {
            setHomeAsUpIndicator(ContextCompat.getDrawable(requireContext(), Constants.backButton))
        }*/

        val binding = FragmentAccountBinding.inflate(inflater, container, false)
       /* txtFirstName = binding.etName
        txtLastName = binding.txtLastName
        txtPhone = binding.etNumber
        imgPhoto = binding.imgProfile

        binding.btnSignUp.setOnClickListener {
            updatePhoto()
        }
        binding.btnChange.setOnClickListener {
            it.findNavController().navigate(R.id.action_accountFragment_to_passwordFragment)
        }
        binding.btnVerify.setOnClickListener {
            doPhoneNumberVerification()
        }
        binding.imgProfile.setOnClickListener {
            didOpenPhotoOption()
        }*/

        val user = app.currentUser
        Glide.with(requireActivity())
            .load(Constants.downloadUrl(user.image))
            .apply(RequestOptions().centerCrop().circleCrop().placeholder(R.drawable.user))
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imgPhoto)
        txtFirstName.setText(user.firstName)
        txtLastName.setText(user.lastName)
        binding.txtEmail.isEnabled = false
        binding.txtEmail.setText(user.email)
        binding.etNumber.setText(user.phone)
        //binding.txtPassword.setText("Password")

        activity?.title = ""
        return binding.root
    }

    @SuppressLint("RestrictedApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == RC_SIGN_IN) {
                val response = IdpResponse.fromResultIntent(data)
                if (response != null) {
                    btnVerify.text = resources.getText(R.string.verified)
                    val phone = response.user.phoneNumber.toString()
                    updatePhoneNumber(phone)
                }
            }
        } else {
            if (requestCode == RC_SIGN_IN) {
                val response = IdpResponse.fromResultIntent(data)
                if (response != null) {
                    val error = response.error
                    if (error != null) {
                        shortToast(response.error!!.localizedMessage)
                    }
                    btnVerify.text = resources.getText(R.string.verify)
                }
            }
        }
    }

    override fun didSelectPhoto(uri: Uri) {
        super.didSelectPhoto(uri)
//        imgPhoto.setImageURI(uri)
        Glide.with(requireActivity())
            .load(uri)
            .into(imgPhoto)
    }

    private fun updatePhoto() {
        if (context == null) return

        if (!validation()) {
            return
        }

        var imageName = app.currentUser.image
        var oldImage = ""

        val files = ArrayList<File>()
        val names = ArrayList<String>()

        val firstName = txtFirstName.text.toString()
        val lastName = txtLastName.text.toString()
        val phone = txtPhone.text.toString()

        if (imageFile != null) {
            oldImage = imageName
            val time = System.currentTimeMillis()
            imageName = String.format("%d_profile_%d.jpg", app.currentUser.id, time)

            files.add(imageFile!!)
            names.add(imageName)
        }

        val params = HashMap<String, String>()
        params["old_image"] = oldImage
        params["image"] = imageName
        params["first_name"] = firstName
        params["last_name"] = lastName
        params["type"] = "profile"
        params["phone_number"] = phone


        progressHUD.show()

       /* APIHandler(
            context!!,
            Constants.API.upload_image,
            params,
            files,
            names,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    val message: String = result.getString("message")
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    progressHUD.dismiss()
                    app.currentUser.image = imageName
                    app.currentUser.firstName = txtFirstName.text.toString()
                    app.currentUser.lastName = txtLastName.text.toString()
                    app.currentUser.phone = txtPhone.text.toString()
                    sessionManager.userData = app.currentUser.getJsonString()
                    updateQBUser()

                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    Toast.makeText(
                        context,
                        error, Toast.LENGTH_LONG
                    ).show()
                }
            })*/

    }

    private fun validation()
            : Boolean {
        //var validate = true
        if (txtFirstName.text.toString().isEmpty()) {
            txtFirstName.error = "Please type your first name"
            return false
        }
        if (txtLastName.text.toString().isEmpty()) {
            txtLastName.error = "Please type your last name"
            return false
        }
        return true
    }

    private fun updatePhoneNumber(phone: String) {
        txtPhone.setText(phone)
        val params = HashMap<String, String>()
        params["phone"] = phone
        val progressHUD = KProgressHUD(activity)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        progressHUD.show()

        APIHandler(
            requireContext(),
            Request.Method.PUT,
            Constants.API.phone,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    val message: String = result.getString("message")
                    shortToast(message)
                    progressHUD.dismiss()
                    app.currentUser.phone = phone
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    private fun updateQBUser() {
        if (app.currentUser.accountType != UserType.Barber) {
            return
        }
        var isChanged = false
        val user = SharedPrefsHelper.getQbUser()

        if (user?.fullName == app.currentUser.name) {
            user.fullName = app.currentUser.name
            isChanged = true
        }
        val url = Constants.downloadUrlll(app.currentUser.image)
        if (url != user?.website) {
            user?.website = url
        }
        if (!isChanged) return

        QBUsers.updateUser(user)

    }

    private fun doPhoneNumberVerification() {
        val providers = Arrays.asList<AuthUI.IdpConfig>(
//            AuthUI.IdpConfig.PhoneBuilder().setDefaultNumber(app.currentUser.phone).build()
            AuthUI.IdpConfig.PhoneBuilder().build()
        )
        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setTheme(R.style.FirebasePhoneAuth)
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN
        )
    }

    companion object {
        private const val RC_SIGN_IN = 123
    }

    override fun onResume() {
        super.onResume()
        //(activity as BarberMainActivity).supportActionBar?.title = ""
    }
}
