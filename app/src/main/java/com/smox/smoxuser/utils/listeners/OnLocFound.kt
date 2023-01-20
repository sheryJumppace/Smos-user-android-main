package com.smox.smoxuser.utils.listeners

import android.content.Context
import com.google.android.gms.maps.model.LatLng

interface OnLocFound {
    fun onLocFound(latLng: LatLng, context: Context)
}