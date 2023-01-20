package com.smox.smoxuser.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

private const val PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST = 1010
private const val PERMISSIONS_FOR_CAMERA_REQUEST = 1012

class SystemPermissionHelper(private var activity: Activity) {

    fun isSaveImagePermissionGranted(): Boolean {
        return isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissionsForSaveFileImage() {
        checkAndRequestPermissions(PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun checkAndRequestPermissions(requestCode: Int, vararg permissions: String) {
        if (collectDeniedPermissions(*permissions).isNotEmpty()) {
            requestPermissions(requestCode, *collectDeniedPermissions(*permissions))
        }
    }

    private fun collectDeniedPermissions(vararg permissionsList: String): Array<String> {
        val deniedPermissionsList = ArrayList<String>()
        for (permission in permissionsList) {
            if (!isPermissionGranted(permission)) {
                deniedPermissionsList.add(permission)
            }
        }
        return deniedPermissionsList.toTypedArray()
    }

    private fun requestPermissions(requestCode: Int, vararg permissions: String) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }

    fun isCameraPermissionGranted(): Boolean {
        return isCamPermissionGranted(Manifest.permission.CAMERA)
    }

    private fun isCamPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissionsForCamera() {
        checkAndRequestCameraPermissions(PERMISSIONS_FOR_CAMERA_REQUEST, Manifest.permission.CAMERA)
    }

    private fun checkAndRequestCameraPermissions(requestCode: Int, vararg permissions: String) {
        if (collectDeniedPermissions(*permissions).isNotEmpty()) {
            requestPermissions(requestCode, *collectDeniedPermissions(*permissions))
        }
    }
}