package com.smox.smoxuser.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.smox.smoxuser.R
import com.smox.smoxuser.utils.CAMERA_REQUEST_CODE
import com.smox.smoxuser.utils.GALLERY_REQUEST_CODE
import com.smox.smoxuser.utils.shortToast
import com.smox.smoxuser.utils.startImagePicker
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.ByteArrayOutputStream


open class BaseImagePickerFragment : BaseFragment(), PhotoPickerDialogFragment.Listener  {

    companion object {
        private val TAG = BaseFragment::class.java.simpleName
        private const val REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101
        private const val REQUEST_CAMERA_ACCESS_PERMISSION = 102
    }

    protected var imageFile: File? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE && data != null) {
                val selectedUri = data.data
                startCropActivity(selectedUri!!)
            }else if (requestCode == CAMERA_REQUEST_CODE) {
                val imageBitmap = data?.extras?.get("data") as Bitmap
                val uri = getImageUri(imageBitmap)
                startCropActivity(uri)
//                startCropActivity(Uri.fromFile(ImageUtils.getLastUsedCameraFile()))
            } else if (requestCode == UCrop.REQUEST_CROP) {
                handleCropResult(data!!)
            }
        }else if (resultCode == UCrop.RESULT_ERROR) {
            handleCropError(data!!)
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        when (requestCode) {
            REQUEST_STORAGE_READ_ACCESS_PERMISSION -> {
                var grant = 0
                grantResults.forEach {
                    grant += it
                }
                if (grant == PackageManager.PERMISSION_GRANTED) {
                    onGalleryClicked()
                }
            }
            REQUEST_CAMERA_ACCESS_PERMISSION -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPhotoClicked()

            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onPhotoClicked() {
        if (ContextCompat.checkSelfPermission(context!!,
                Manifest.permission.CAMERA) +
            ContextCompat.checkSelfPermission(context!!,
                Manifest.permission.READ_EXTERNAL_STORAGE)+
            ContextCompat.checkSelfPermission(context!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                    Manifest.permission.CAMERA)|| ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                    Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                val message = "This app requires to use Camera."
                showAlertDialog("", message, DialogInterface.OnClickListener{_,_->
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(activity!!,
                        arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_CAMERA_ACCESS_PERMISSION
                    )
                }, getString(R.string.ok), null, null)
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity!!,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CAMERA_ACCESS_PERMISSION
                )

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
//            ImageUtils.startCameraForResult(this@BaseImagePickerActivity)

            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(activity!!.packageManager)?.also {
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
                }
            }
        }
    }

    override fun onGalleryClicked() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(context!!,
                Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                val message = "This app requires to access on your photos."
                showAlertDialog("", message, DialogInterface.OnClickListener{_,_->
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(activity!!,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_STORAGE_READ_ACCESS_PERMISSION
                    )
                }, getString(R.string.ok), null, null)

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity!!,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION
                )

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            startImagePicker(this)
        }
    }

    override fun onCancel() {

    }

    open fun didOpenPhotoOption(){
        val photoPickerDialogFragment = PhotoPickerDialogFragment()
        photoPickerDialogFragment.setOnListener(this)
        photoPickerDialogFragment.show(
            fragmentManager!!,
            "add_photo_dialog_fragment"
        )
    }

    protected open fun didSelectPhoto(uri: Uri){

    }
    private fun getImageUri(inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(activity!!.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }
    private fun startCropActivity(uri: Uri) {
        val time = System.currentTimeMillis()
        val destinationFileName = String.format("%d_profile_%d.jpg", app.currentUser.id, time)
        var uCrop = UCrop.of(uri, Uri.fromFile(File(context?.cacheDir, destinationFileName)))
        uCrop = basisConfig(uCrop)
        uCrop = advancedConfig(uCrop)

        uCrop.start(activity!!, this)
    }

    /**
     * In most cases you need only to set crop aspect ration and max size for resulting image.
     *
     * @param uCrop - ucrop builder instance
     * @return - ucrop builder instance
     */
    private fun basisConfig(uCrop: UCrop): UCrop {
        var crop = uCrop
        crop = crop.withAspectRatio(1f, 1f)
        crop = crop.withMaxResultSize(1000, 1000)

        return crop
    }

    /**
     * Sometimes you want to adjust more options, it's done via [com.yalantis.ucrop.UCrop.Options] class.
     *
     * @param uCrop - ucrop builder instance
     * @return - ucrop builder instance
     */
    private fun advancedConfig(uCrop: UCrop): UCrop {
        val options = UCrop.Options()
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG)

        options.setCompressionQuality(100)

        options.setHideBottomControls(true)
        options.setFreeStyleCropEnabled(true)
        options.setCircleDimmedLayer(true)

        return uCrop.withOptions(options)
    }

    private fun handleCropResult(result: Intent) {
        val resultUri = UCrop.getOutput(result)
        if (resultUri != null) {
            imageFile = File(resultUri.path)
            didSelectPhoto(resultUri)
        } else {
            shortToast(R.string.toast_cannot_retrieve_cropped_image)
        }
    }

    /**
     * Requests given permission.
     * If the permission has been denied previously, a Dialog will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private fun requestPermission(permission: String, rationale: String, requestCode: Int) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permission)) {
            showAlertDialog(getString(R.string.permission_title_rationale), rationale,
                DialogInterface.OnClickListener { _, _ ->
                    ActivityCompat.requestPermissions(
                        activity!!,
                        arrayOf(permission), requestCode
                    )
                }, getString(R.string.ok), null, getString(R.string.cancel)
            )
        } else {
            ActivityCompat.requestPermissions(activity!!, arrayOf(permission), requestCode)
        }
    }


    private fun handleCropError(result: Intent) {
        val cropError = UCrop.getError(result)
        if (cropError != null) {
            Log.e(TAG, "handleCropError: ", cropError)
            shortToast(cropError.message)
        } else {
            shortToast(R.string.toast_unexpected_error)
        }
    }

    /**
     * This method shows dialog with given title & message.
     * Also there is an option to pass onClickListener for positive & negative button.
     *
     * @param title                         - dialog title
     * @param message                       - dialog message
     * @param onPositiveButtonClickListener - listener for positive button
     * @param positiveText                  - positive button text
     * @param onNegativeButtonClickListener - listener for negative button
     * @param negativeText                  - negative button text
     */
    open fun showAlertDialog(
        title: String?, message: String?,
        onPositiveButtonClickListener: DialogInterface.OnClickListener?,
        positiveText: String,
        onNegativeButtonClickListener: DialogInterface.OnClickListener?,
        negativeText: String?
    ) {
        val builder = AlertDialog.Builder(activity!!)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(positiveText, onPositiveButtonClickListener)
        if(negativeText != null){
            builder.setNegativeButton(negativeText, onNegativeButtonClickListener)
        }
        builder.show()
    }
}
