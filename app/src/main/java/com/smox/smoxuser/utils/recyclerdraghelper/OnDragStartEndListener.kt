package com.app.imcovery.recyclerdraghelper

interface OnDragStartEndListener {
    fun onDragStartListener(fromPosition: Int)
    fun onDragEndListener(toPosition: Int)
}