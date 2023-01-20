package com.smox.smoxuser.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smox.smoxuser.data.AddressRepository
import com.smox.smoxuser.model.Address
import org.json.JSONObject

class AddressListViewModel internal constructor(private val repository: AddressRepository): ViewModel(){
    var addresses: MutableLiveData<List<Address>> = repository.addresses
    var deleteAddress: MutableLiveData<JSONObject> = repository.deleteAddress

    fun fetchList(context: Context){
        repository.fetchList(context = context)
    }

    fun deleteAddress(context: Context, addressId: String){
        repository.deleteAddress(context = context, addressId = addressId)
    }
}


class AddressListViewModelFactory(
    private val repository: AddressRepository
): ViewModelProvider.NewInstanceFactory(){

    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelclass: Class<T>) = AddressListViewModel(repository) as T
}