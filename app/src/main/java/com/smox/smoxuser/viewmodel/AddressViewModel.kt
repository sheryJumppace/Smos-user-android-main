package com.smox.smoxuser.viewmodel

import android.content.Context
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.common.internal.ResourceUtils
import com.google.gson.JsonObject
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.FragmentAddressListBinding
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.AddressResponse
import com.smox.smoxuser.model.InputValidatorMessage
import com.smox.smoxuser.model.SimpleOkResponse2
import com.smox.smoxuser.retrofit.ApiRepository
import com.smox.smoxuser.utils.getProgressBar
import com.smox.smoxuser.utils.shortToast
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class AddressViewModel : ViewModel() {

    val error = MutableLiveData<InputValidatorMessage>()
    var addressId = ObservableField("")
    var firstName = ObservableField("")
    var lastName = ObservableField("")
    var address1 = ObservableField("")
    var address2 = ObservableField("")
    var city = ObservableField("")
    var state = ObservableField("")
    var country = ObservableField("")
    var zipCode = ObservableField("")
    var phoneNumber = ObservableField("")
    var isDefault = ObservableField(false)
    var lat = ObservableField(0.0)
    var lon = ObservableField(0.0)
    var addressCount = ObservableField(0)
    var isEdit = MutableLiveData(false)
    var isAddressAdded = MutableLiveData(false)
    var addressList = MutableLiveData<ArrayList<AddressResponse.AddressData>>()


    fun submit(view: View) {
        if (isInputDataValidate(view.context)) {


            Log.d("++--++","ZipCode in view model : ${zipCode.get().toString()}")

            val progressBar = getProgressBar(view.context)
            progressBar.show()

            val jsonObject = JsonObject()
            if (isEdit.value!!) jsonObject.addProperty("address_id", addressId.get().toString())
            if (addressCount.get() == 0) isDefault.set(true)
            jsonObject.addProperty("first_name", firstName.get().toString())
            jsonObject.addProperty("last_name", lastName.get().toString())
            jsonObject.addProperty("address_one", address1.get().toString())
            jsonObject.addProperty("address_two", address2.get().toString())
            jsonObject.addProperty("city", city.get().toString())
            jsonObject.addProperty("state", state.get().toString())
            jsonObject.addProperty("country", country.get().toString())
            jsonObject.addProperty("zipcode", zipCode.get().toString())
            jsonObject.addProperty("latitude", lat.get().toString())
            jsonObject.addProperty("longitude", lon.get().toString())
            jsonObject.addProperty("phone", phoneNumber.get().toString())
            jsonObject.addProperty("make_default", if (isDefault.get()!!) 1 else 0)

            Log.d("++--++","jsonObject \n : ${jsonObject}")


            ApiRepository(view.context).addNewAddress(jsonObject).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<SimpleOkResponse2> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onNext(res: SimpleOkResponse2) {
                        progressBar.dismiss()
                        Log.d("++--++","deliveryAddress\n" +
                                "\nsubmit res.message : ${res.message}")

                        if (!res.error) {
                            shortToast(res.message)
                            isAddressAdded.postValue(true)
                        }
                        else {
                            isAddressAdded.postValue(false)
                            if (res.message.trim()
                                    .startsWith("Address Not Found", true) || res.message
                                    .trim().startsWith("Invalid city", true)
                            ) shortToast("Address not deliverable, please change your address.")
                            else shortToast(res.message)
                        }
                    }

                    override fun onError(e: Throwable) {
                        progressBar.dismiss()
                        Log.e("TAG", "onError: ${e.message}")
                        isAddressAdded.postValue(false)
                        if ((e as HttpException).code()==401){
                            shortToast(view.context.getString(R.string.authError))
                            APIHandler(view.context).logout()
                        }
                        else
                            shortToast(e.message())
                    }

                    override fun onComplete() {

                    }

                })
        }
    }

    private fun isInputDataValidate(context: Context): Boolean {
        when {
            firstName.get().toString().isEmpty() -> {
                error.value = InputValidatorMessage(Constants.API.F_NAME,
                    context.getString(R.string.error_fname))
                return false
            }
            lastName.get().toString().isEmpty() -> {
                error.value = InputValidatorMessage(Constants.API.L_NAME,
                    context.getString(R.string.error_lname))
                return false
            }
            /*address1.get().toString().isEmpty() -> {
                error.value = InputValidatorMessage(Constants.API.ADD1,
                    context.getString(R.string.error_add1))
                return false
            }*/
            address2.get().toString().isEmpty() -> {
                error.value = InputValidatorMessage(Constants.API.ADD2,
                    context.getString(R.string.error_add2))
                return false
            }
            city.get().toString().isEmpty() -> {
                error.value = InputValidatorMessage(Constants.API.CITY,
                    context.getString(R.string.error_city))
                return false
            }
            state.get().toString().isEmpty() -> {
                error.value = InputValidatorMessage(Constants.API.STATE,
                    context.getString(R.string.error_state))
                return false
            }
            country.get().toString().isEmpty() -> {
                error.value = InputValidatorMessage(Constants.API.COUNTRY,
                    context.getString(R.string.error_country))
                return false
            }
            zipCode.get().toString().isEmpty() -> {
                error.value =
                    InputValidatorMessage(Constants.API.ZIP, context.getString(R.string.error_zip))
                return false
            }
            phoneNumber.get().toString().isEmpty() -> {
                error.value = InputValidatorMessage(Constants.API.PHONE,
                    context.getString(R.string.error_phone))
                return false
            }

            phoneNumber.get().toString().length < 6 -> {
                error.value = InputValidatorMessage(Constants.API.PHONE,
                    context.getString(R.string.error_phoneValid))
                return false
            }
            !Patterns.PHONE.matcher(phoneNumber.get().toString()).matches() -> {
                error.value = InputValidatorMessage(Constants.API.PHONE,
                    context.getString(R.string.error_phoneValid))
                return false
            }

            else -> return true
        }
    }

    fun getAddressList(context: Context) {
        val progressBar = getProgressBar(context)
        progressBar.show()

        ApiRepository(context).getAddressList().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<AddressResponse> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(res: AddressResponse) {
                    progressBar.dismiss()
                    if (!res.error) {
                        addressList.postValue(res.result)
                    }
                    else shortToast(res.message)
                }

                override fun onError(e: Throwable) {
                    progressBar.dismiss()
                    Log.e("TAG", "onError: ${e.message}")
                    if ((e as HttpException).code()==401){
                        shortToast(context.getString(R.string.authError))
                        APIHandler(context).logout()
                    }
                    else
                        shortToast(e.message())
                }

                override fun onComplete() {

                }

            })
    }

    fun deleteAddress(context: Context, addressId: String) {
        val progressBar = getProgressBar(context)
        progressBar.show()

        val jsonObject = JsonObject()
        jsonObject.addProperty("address_id", addressId)

        ApiRepository(context).deleteAddress(jsonObject).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<SimpleOkResponse2> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(res: SimpleOkResponse2) {
                    progressBar.dismiss()
                    if (!res.error) {
                        getAddressList(context)
                    }
                    else shortToast(res.message)
                }

                override fun onError(e: Throwable) {
                    progressBar.dismiss()
                    Log.e("TAG", "onError: ${e.message}")
                    if ((e as HttpException).code()==401){
                        shortToast(context.getString(R.string.authError))
                        APIHandler(context).logout()
                    }
                    else
                        shortToast(e.message())
                }

                override fun onComplete() {

                }

            })
    }

    fun setAddressData(addressItem: AddressResponse.AddressData?) {

        addressId.set(addressItem?.id)
        firstName.set(addressItem?.first_name)
        lastName.set(addressItem?.last_name)
        address1.set(addressItem?.address_one)
        address2.set(addressItem?.address_two)
        city.set(addressItem?.city)
        state.set(addressItem?.state)
        country.set(addressItem?.country)
        zipCode.set(addressItem?.zipcode)
        phoneNumber.set(addressItem?.phone)
        isDefault.set(addressItem?.make_default == "1")
    }

    fun clearData() {
        addressId.set("")
        firstName.set("")
        lastName.set("")
        address1.set("")
        address2.set("")
        city.set("")
        state.set("")
        country.set("")
        zipCode.set("")
        phoneNumber.set("")
        isDefault.set(addressCount.get()!! < 1)
    }

    fun makeDefaultAddress(addressItem: AddressResponse.AddressData, binding: FragmentAddressListBinding) {
        isEdit.value=true
        addressId.set(addressItem.id)
        firstName.set(addressItem.first_name)
        lastName.set(addressItem.last_name)
        address1.set(addressItem.address_one)
        address2.set(addressItem.address_two)
        city.set(addressItem.city)
        state.set(addressItem.state)
        country.set(addressItem.country)
        zipCode.set(addressItem.zipcode)
        phoneNumber.set(addressItem.phone)
        isDefault.set(true)

        submit(binding.root)

    }
}