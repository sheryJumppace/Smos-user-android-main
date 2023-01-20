/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smox.smoxuser.viewmodel

import android.content.Context
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.smox.smoxuser.data.BarberRepository
import com.smox.smoxuser.model.Barber

class BarberListViewModel internal constructor(private val repository: BarberRepository) : ViewModel() {
    var barbers: MutableLiveData<List<Barber>> = repository.barbers
    var favBarbers: MutableLiveData<List<Barber>> = repository.favBarbers
    fun fetchList(context: Context, location: LatLng? = null, query: String? = null, isFavorite:Boolean = false, page:String="1"){
        repository.fetchList(context = context, location = location, query = query, isFavorite = isFavorite, page = page)
    }
    fun getBarber(id:Int): Barber? {
        return repository.getBarber(id)
    }

}

class BarberListViewModelFactory(
    private val repository: BarberRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = BarberListViewModel(repository) as T
}