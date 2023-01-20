package com.smox.smoxuser.ui.fragment.address

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.smox.smoxuser.databinding.FragmentAddEditAddressBinding
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.manager.Constants.API.ADDRESS_ITEM
import com.smox.smoxuser.manager.Constants.API.AUTOCOMPLETE_REQUEST_CODE
import com.smox.smoxuser.manager.Constants.API.IS_EDIT
import com.smox.smoxuser.manager.Constants.API.address
import com.smox.smoxuser.model.AddressResponse
import com.smox.smoxuser.ui.activity.product.ProductsActivity
import com.smox.smoxuser.viewmodel.AddressViewModel
import java.io.IOException
import java.util.*


class AddEditAddressFragment : Fragment() {

    lateinit var binding: FragmentAddEditAddressBinding
    lateinit var addressViewModel: AddressViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddEditAddressBinding.inflate(inflater, container, false)
        addressViewModel = (activity as ProductsActivity).addressViewModel

        val bundle = arguments
        val value = bundle!!.getBoolean(IS_EDIT)
        val addressItem = bundle.getParcelable<AddressResponse.AddressData>(ADDRESS_ITEM)
        addressViewModel.isEdit.value = value
        (activity as ProductsActivity).txtTitle.text =
            if (value) "Update Address" else "Add Address"

        binding.defaultSwitch.isEnabled = addressViewModel.addressCount.get()!! >= 1
        addressViewModel.isDefault.set(addressViewModel.addressCount.get()!! < 1)
        binding.viewModel = addressViewModel
        if (value)
            addressViewModel.setAddressData(addressItem)
        else
            addressViewModel.clearData()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        validateInput()
        initObservar()
    }

    private fun initObservar() {
        addressViewModel.isAddressAdded.observe(viewLifecycleOwner, Observer {
            if (it) {
                Navigation.findNavController(requireView()).popBackStack()
            }
        })

        binding.etCity.setOnClickListener {
            startPlaceApi()
        }
    }

    private fun startPlaceApi() {
        if (Places.isInitialized()) {
            val fields: List<Place.Field> = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.ADDRESS_COMPONENTS,
                Place.Field.LAT_LNG
            )
            val autoSearchIntent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN,
                fields
            ).build(requireActivity())
            startActivityForResult(autoSearchIntent, AUTOCOMPLETE_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)

                addressViewModel.lat.set(place.latLng?.latitude)
                addressViewModel.lon.set(place.latLng?.longitude)

                val inputMethodManager =
                    requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)

                getAddress()

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = Autocomplete.getStatusFromIntent(data!!)
            }
        }
    }

    private fun getAddress() {
        try {
            val geocoder = Geocoder(requireActivity(), Locale.getDefault())
            val addresses: List<Address> = geocoder.getFromLocation(
                addressViewModel.lat.get()!!,
                addressViewModel.lon.get()!!,
                1
            )
            if (addresses.isNotEmpty()) {
                val address: Address = addresses[0]

                addressViewModel.city.set(address.locality)
                addressViewModel.state.set(address.adminArea)
                addressViewModel.zipCode.set(address.postalCode)
                addressViewModel.country.set(address.countryName)

            }
        } catch (e: IOException) {
            Log.e("tag", e.localizedMessage)
        }
    }

    private fun validateInput() {
        addressViewModel.error.observe(viewLifecycleOwner, Observer {
            when (it.tag) {
                Constants.API.F_NAME -> {
                    binding.etFirstName.error = it.message
                }
                Constants.API.L_NAME -> {
                    binding.etLastName.error = it.message
                }
                Constants.API.ADD1 -> {
                    binding.etAddress1.error = it.message
                }
                Constants.API.ADD2 -> {
                    binding.etAddress2.error = it.message
                }
                Constants.API.CITY -> {
                    binding.etCity.error = it.message
                }
                Constants.API.STATE -> {
                    binding.etState.error = it.message
                }
                Constants.API.COUNTRY -> {
                    binding.etCountry.error = it.message
                }
                Constants.API.ZIP -> {
                    binding.etZipCode.error = it.message
                }
                Constants.API.PHONE -> {
                    binding.etNumber.error = it.message
                }
            }
        })
    }
}