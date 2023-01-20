package com.smox.smoxuser.utils.listeners

import com.google.android.gms.maps.model.LatLng

interface OnGetAddress {
    fun onGetAddress(address:String, latLng: LatLng)
}