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
import com.smox.smoxuser.data.ProductRepository
import com.smox.smoxuser.model.Product

class ProductListViewModel internal constructor(private val repository: ProductRepository) : ViewModel() {
    fun fetchList(context: Context){
        repository.fetchList(context = context)
    }
    var products: MutableLiveData<List<Product>> = repository.products
    fun getProduct(id:Int): Product? {
        return repository.getProduct(id)
    }
    fun deleteProduct(context: Context,id:Int){
        repository.deleteProduct(context,id)
    }
}

class ProductListViewModelFactory(
    private val repository: ProductRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = ProductListViewModel(repository) as T
}