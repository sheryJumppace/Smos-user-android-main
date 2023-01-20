package com.smox.smoxuser.manager

import java.util.*
import kotlin.collections.HashMap

class ObservingService {
    private var observables: HashMap<String, JumpObservable> = HashMap()

    fun addObserver(notification: String, observer: Observer) {
        var observable = observables[notification]
        if (observable == null) {
            observable = JumpObservable()
            observables[notification] = observable
        }
        observable.addObserver(observer)
    }

    fun removeObserver(notification: String, observer: Observer) {
        observables[notification]?.deleteObserver(observer)
    }

    fun postNotification(notification: String, data: Any) {
        val ob = observables[notification]
        ob?.notifyObservers(data)
    }

    companion object {
        @Volatile
        private var instance: ObservingService? = null

        fun getInstance(): ObservingService =
            instance ?: synchronized(this) {
                instance
                    ?: ObservingService().also {
                        instance = it
                    }
            }
    }

    class JumpObservable : Observable() {

        /* To force notifications to be sent */
        override fun notifyObservers(data: Any) {
            setChanged()
            super.notifyObservers(data)
        }
    }
}