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
import com.smox.smoxuser.data.UpNextOptionRepository
import com.smox.smoxuser.model.UpNextOption

class UpNextOptionListViewModel internal constructor(private val repository: UpNextOptionRepository) : ViewModel() {
    var options: MutableLiveData<List<UpNextOption>> = repository.options
    fun fetchList(context: Context){
        repository.fetchList(context = context)
    }
    fun getOption(position:Int): UpNextOption? {
        return repository.getOption(position)
    }
    fun addOption(context: Context, title:String) {
        repository.addOption(context, title)
    }
    fun deleteOption(context: Context, position:Int) {
        repository.deleteOption(context, position)
    }
}

class UpNextOptionListViewModelFactory(
    private val repository: UpNextOptionRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = UpNextOptionListViewModel(repository) as T
}