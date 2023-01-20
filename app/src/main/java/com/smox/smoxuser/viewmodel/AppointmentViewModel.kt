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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smox.smoxuser.data.AppointmentRepository
import com.smox.smoxuser.model.Appointment


class AppointmentViewModel internal constructor(private val repository: AppointmentRepository,
                                                private val appointmentId: Int) : ViewModel() {

    var appointment: MutableLiveData<Appointment> = repository.selectedAppointment
    var isSuccessToSentPaymentRequest: MutableLiveData<Boolean> = repository.isSuccessToSentPaymentRequest
    fun fetchList(context: Context){
        repository.getAppointment(context, appointmentId)
    }
    fun updateAppointment(appointment: Appointment){
        repository.updateAppointment(appointment)
    }
    fun sendPaymentRequest(context: Context, appointment: Appointment){
        //repository.sendPaymentRequest(context, appointment)
    }
}

class AppointmentViewModelFactory(
    private val repository: AppointmentRepository,
    private val appointmentId: Int
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = AppointmentViewModel(repository, appointmentId) as T
}