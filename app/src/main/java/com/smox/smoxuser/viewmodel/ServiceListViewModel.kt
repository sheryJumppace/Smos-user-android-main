
package com.smox.smoxuser.viewmodel

import android.content.Context
import androidx.lifecycle.*
import com.smox.smoxuser.data.ServiceRepository
import com.smox.smoxuser.model.Service
import org.json.JSONObject

class ServiceListViewModel internal constructor(private val repository: ServiceRepository) : ViewModel() {
    private val isUpdated = repository.isUpdated

    var services: LiveData<List<Service>> = Transformations.switchMap(isUpdated) {
        repository.getServices()
    }
    fun fetchList(context: Context){
        repository.fetchList(context = context)
    }

    fun updateServices(services:ArrayList<Service>){
        repository.updateServices(services)
    }

    fun sendReorderService(context: Context, data: JSONObject) {
        repository.sendReorderService(context, data)
    }

    fun deleteServiceFromServer(context: Context, serviceId:Int){
        repository.deleteServiceFromServer(context = context, serviceId = serviceId)
    }

    fun deleteService(service: Service){
        repository.deleteService(service)
    }

    fun didDeselectAll(){
        repository.didDeselectAll()
    }

    fun getSelectedContacts():List<Service>{return repository.getSelectedServices()}
}

class ServiceListViewModelFactory(
    private val repository: ServiceRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = ServiceListViewModel(repository) as T
}