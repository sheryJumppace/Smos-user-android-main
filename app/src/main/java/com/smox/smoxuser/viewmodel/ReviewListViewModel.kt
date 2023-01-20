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
import com.smox.smoxuser.data.ReviewRepository
import com.smox.smoxuser.model.Review

class ReviewListViewModel internal constructor(private val repository: ReviewRepository) : ViewModel() {
     val isUpdated = repository.isUpdated

    var reviews: LiveData<List<Review>> = Transformations.switchMap(isUpdated) {
        repository.getReviews()
    }
    fun setStartPageIndex(page:Int){
        if(page == 0){
            repository.reviews.clear()
        }
        repository.page = page
    }
    fun getReviewsCount():Int{
        return repository.reviews.count()
    }
    fun addReview(context: Context, comment: String, clean: Int,work: Int,behave: Int, barberId: Int, appointId:Int){
        repository.addReview(context, comment, clean,work,behave, barberId, appointId)
    }
    fun fetchList(context: Context, barberId:Int){
        repository.fetchList(context = context, barberId = barberId)
    }
}

class ReviewListViewModelFactory(
    private val repository: ReviewRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = ReviewListViewModel(repository) as T
}