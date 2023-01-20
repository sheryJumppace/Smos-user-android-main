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
import com.smox.smoxuser.data.ContactRepository
import com.smox.smoxuser.model.SmoxUser

class ContactListViewModel internal constructor(private val repository: ContactRepository, isFavorite:Boolean) : ViewModel() {
    private val isUpdated = repository.isUpdated

    var contacts: LiveData<List<SmoxUser>> = Transformations.switchMap(isUpdated) {
        repository.getContacts(isFavorite)
    }
    fun fetchList(context: Context){
        repository.fetchList(context = context)
    }
    fun didDeselectAll(){
        repository.didDeselectAll()
    }

    fun getSelectedContacts():List<SmoxUser>{return repository.getSelectedContacts()}
}

class ContactListViewModelFactory(
    private val repository: ContactRepository,
    private val favorite:Boolean
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = ContactListViewModel(repository, favorite) as T
}