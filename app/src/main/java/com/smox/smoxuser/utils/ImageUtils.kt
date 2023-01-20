package com.smox.smoxuser.utils

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.smox.smoxuser.App
import com.smox.smoxuser.R
import java.io.File
import java.io.IOException
import java.util.*
import android.app.Activity
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

const val CAMERA_FILE_NAME_PREFIX = "CAMERA_"
const val IMAGE_MIME = "image/*"
const val EXTERNAL_STORAGE = "com.android.externalstorage.documents"
const val DOCUMENTS_URI = "com.android.providers.downloads.documents"
const val MEDIA_URI = "com.android.providers.media.documents"

const val GALLERY_REQUEST_CODE = 183
const val CAMERA_REQUEST_CODE = 212

private val isKitkatSupportDevice = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

@Throws(Exception::class)
fun getImageFilePathFromUri(uri: Uri): String {
    var filePath = ""
    if (isKitkatSupportDevice && DocumentsContract.isDocumentUri(App.instance, uri)) {
        filePath = getFilePathFromKitKatSupport(uri)
    } else if (uri.scheme.equals("content", ignoreCase = true)) { //MediaStore
        filePath = getDataColumn(uri, null, null)
    } else if (uri.scheme.equals("file", ignoreCase = true)) {
        filePath = uri.path ?: ""
    }
    return filePath
}

@RequiresApi(Build.VERSION_CODES.KITKAT)
private fun getFilePathFromKitKatSupport(uri: Uri): String {
    var filePath = ""
    when (uri.authority) {
        EXTERNAL_STORAGE -> {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":")
            val type = split[0]
            if (type.equals("primary", ignoreCase = true)) {
                filePath = Environment.getExternalStorageDirectory().path + "/" + split[1]
            }
        }
        DOCUMENTS_URI -> {
            val id = DocumentsContract.getDocumentId(uri)
            val parsedUri = Uri.parse("\"content://downloads/public_downloads\"")
            val contentUri = ContentUris.withAppendedId(parsedUri, id.toLong())
            filePath = getDataColumn(contentUri, null, null)
        }
        MEDIA_URI -> {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":")
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val selection = "_id=?"
            val selectionArgs = arrayOf(split[1])
            filePath = getDataColumn(contentUri, selection, selectionArgs)
        }
    }
    return filePath
}

private fun getDataColumn(uri: Uri, selection: String?, selectionArgs: Array<String>?): String {
    var result = ""
    val column = "_data"
    val projection = arrayOf(column)
    val cursor = App.instance.contentResolver.query(uri, projection, selection, selectionArgs, null)
    if (cursor != null && cursor.moveToFirst()) {
        val columnIndex = cursor.getColumnIndexOrThrow(column)
        result = cursor.getString(columnIndex)
        cursor.close()
    }
    return result
}

private fun getAppExternalDataDirectoryFile(): File {
    val dataDirectoryFile = File(getAppExternalDataDirectoryPath())
    dataDirectoryFile.mkdirs()
    return dataDirectoryFile
}

private fun getAppExternalDataDirectoryPath(): String {
    val sb = StringBuilder()
    sb.append(Environment.getExternalStorageDirectory())
            .append(File.separator)
            .append("Android")
            .append(File.separator)
            .append("data")
            .append(File.separator)
            .append(App.instance.packageName)
            .append(File.separator)
    return sb.toString()
}

fun startImagePicker(activity: Activity) {
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.type = IMAGE_MIME
    activity.startActivityForResult(
        Intent.createChooser(intent, activity.getString(R.string.dlg_choose_image_from)),
        GALLERY_REQUEST_CODE
    )
}

fun startCameraForResult(activity: Activity) {
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    if (intent.resolveActivity(activity.packageManager) == null) {
        return
    }

    val photoFile = getTemporaryCameraFile()
    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
    activity.startActivityForResult(intent, CAMERA_REQUEST_CODE)
}

fun startImagePicker(fragment: Fragment) {
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.type = IMAGE_MIME
    fragment.startActivityForResult(Intent.createChooser(intent, fragment.getString(R.string.dlg_choose_image_from)), GALLERY_REQUEST_CODE)
}

fun startCameraForResult(fragment: Fragment) {
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    intent.resolveActivity(App.instance.packageManager)?.let {
        val photoFile = getTemporaryCameraFile()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getValidUri(photoFile, fragment.context))
        fragment.startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }
}

fun getTemporaryCameraFile(): File {
    val storageDir = getAppExternalDataDirectoryFile()
    val file = File(storageDir, getTemporaryCameraFileName())
    try {
        file.createNewFile()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return file
}

fun getLastUsedCameraFile(): File? {
    val dataDir = getAppExternalDataDirectoryFile()
    val files = dataDir.listFiles()
    val filteredFiles = ArrayList<File>()
    for (file in files) {
        if (file.name.startsWith(CAMERA_FILE_NAME_PREFIX)) {
            filteredFiles.add(file)
        }
    }
    filteredFiles.sort()
    return if (filteredFiles.isNotEmpty()) {
        filteredFiles[filteredFiles.size - 1]
    } else {
        null
    }
}

private fun getValidUri(file: File, context: Context?): Uri {
    val outputUri: Uri
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val authority = context!!.packageName + ".provider"
        outputUri = FileProvider.getUriForFile(context, authority, file)
    } else {
        outputUri = Uri.fromFile(file)
    }
    return outputUri
}

fun getRequestOptions(): RequestOptions {

    val options: RequestOptions =
        RequestOptions().centerCrop().placeholder(R.drawable.logo_new)
            .error(R.drawable.logo_new).diskCacheStrategy(DiskCacheStrategy.ALL)


    return options
}

private fun getTemporaryCameraFileName(): String {
    return CAMERA_FILE_NAME_PREFIX + System.currentTimeMillis() + ".jpg"
}