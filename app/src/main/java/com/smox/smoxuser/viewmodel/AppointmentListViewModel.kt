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
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smox.smoxuser.data.AppointmentRepository
import com.smox.smoxuser.model.Appointment
import com.smox.smoxuser.model.Event
import com.smox.smoxuser.model.type.AppointmentType
import org.json.JSONObject

class AppointmentListViewModel internal constructor(private val repository: AppointmentRepository) :
    ViewModel() {

    var appointments: MutableLiveData<List<Appointment>> = repository.appointments
    var events: MutableLiveData<List<Event>> = repository.events
    var staus: MutableLiveData<String> = repository.staus

    fun fetchList(context: Context, date: String, barberId: Int) {
        Log.e("Appointment api ", "Date:- $date, Barber id:- $barberId")
        //repository.fetchList(context, date, barberId)
    }

    fun fetchAppointmentsOfCustomer(context: Context, page:String) {
        repository.appointments.value= arrayListOf()
        repository.fetchAppointmentsOfCustomer(context, page)
    }

    fun getAppointments(type: AppointmentType): List<Appointment> {
        return repository.getAppointments(type)
    }

    fun completeAppointment(context: Context, appointment: Appointment) {
        repository.completeAppointment(context, appointment)
    }

    fun createAppointmentWithWalkIn(context: Context, services: String, duration: Int) {
        repository.createAppointment(context, services, duration)
    }

    fun sendReorderAppointment(context: Context, data: JSONObject) {
        repository.sendReorderAppointment(context, data)
    }

    fun postEvent(
        context: Context,
        event: String,
        start: Long,
        end: Long,
        startDate: String,
        endDate: String,
        id :Int,
        pos :Int
    ) {
        repository.postEvent(context, event, start, end, startDate, endDate,id ,pos)
    }
}
