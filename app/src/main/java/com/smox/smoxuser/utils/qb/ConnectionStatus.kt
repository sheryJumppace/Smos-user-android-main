package com.smox.smoxuser.utils.qb

import android.content.Context
import android.net.ConnectivityManager

fun isInternetConnected(context: Context):Boolean{
    val connectivityManager=context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo=connectivityManager.activeNetworkInfo
    return  networkInfo!=null && networkInfo.isConnected
}