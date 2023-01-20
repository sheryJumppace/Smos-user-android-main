package com.smox.smoxuser.utils

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

object Prefrences {

    var preferences: SharedPreferences?=null

    fun with(application: Application) {
        preferences = application.getSharedPreferences(application.applicationContext.packageName,
            Context.MODE_PRIVATE)
    }

    fun saveBoolean(key: String, value: Boolean) {
        preferences!!.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String): Boolean {
        return preferences!!.getBoolean(key, false)
    }

    fun saveString(key: String, value: String?) {
        preferences!!.edit().putString(key, value).apply()
    }

    fun getString(key: String): String {
        return preferences!!.getString(key, null) ?: ""
    }
}