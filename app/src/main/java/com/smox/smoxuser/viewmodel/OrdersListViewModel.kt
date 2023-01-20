package com.smox.smoxuser.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smox.smoxuser.data.OrdersRepository
import com.smox.smoxuser.model.Orders

class OrdersListViewModel internal constructor(private val repository: OrdersRepository): ViewModel(){
    var productOrderes: MutableLiveData<ArrayList<Orders>> = repository.productOrders
    var productStatus: MutableLiveData<String> = repository.productStatus

    fun fetchList(context: Context, orderUrl: String){
        repository.fetchList(context, orderUrl)
    }

    fun changeProductStatus(context: Context, mParams: HashMap<String, String>){
        repository.changeProductStatus(context, mParams)
    }
}

class OrdersListViewModelFactory(
    private val repository: OrdersRepository
): ViewModelProvider.NewInstanceFactory(){

    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>) = OrdersListViewModel(repository) as T
}