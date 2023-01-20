package com.smox.smoxuser.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage
import com.smox.smoxuser.R
import com.smox.smoxuser.ui.fragment.PhotoPickerDialogFragment
import com.smox.smoxuser.utils.CAMERA_REQUEST_CODE
import com.smox.smoxuser.utils.GALLERY_REQUEST_CODE
import com.smox.smoxuser.utils.shortToast
import com.smox.smoxuser.utils.startImagePicker
import com.yalantis.ucrop.UCrop
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream


open class BaseImagePickerActivity : BaseActivity(), PhotoPickerDialogFragment.Listener {
    var aspectRatio:Float = 1/1f
    companion object {
        private val TAG = BaseImagePickerActivity::class.java.simpleName
        private const val REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101
        private const val REQUEST_CAMERA_ACCESS_PERMISSION = 102
    }

    protected var imageFile:File? = null

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
            } else if (requestCode == UCrop.REQUEST_CROP) {
                handleCropResult(data!!)
            }
        }else if (resultCode == UCrop.RESULT_ERROR) {
            handleCropError(data!!)
        }
    }

    @Throws(IOException::class)
    open fun rotateImageIfRequired(context: Context, img: Bitmap?, selectedImage: Uri): Bitmap? {
        val input: InputStream? = context.getContentResolver().openInputStream(selectedImage)
        val ei: ExifInterface
        if (Build.VERSION.SDK_INT > 23) ei = ExifInterface(input!!) else ei =
            ExifInterface(selectedImage.path!!)
        val orientation: Int =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(
                img!!, 90
            )
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img!!, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img!!, 270)
            else -> img
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray
    ) {
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
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) +
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )+
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
                )|| ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )) {

                val message = "This app requires to use Camera."
                showAlertDialog("", message, DialogInterface.OnClickListener { _, _ ->
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        REQUEST_CAMERA_ACCESS_PERMISSION
                    )
                }, getString(R.string.ok), null, null)
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    REQUEST_CAMERA_ACCESS_PERMISSION
                )

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
//            ImageUtils.startCameraForResult(this@BaseImagePickerActivity)

            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
                }
            }
        }

    }

    override fun onGalleryClicked() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )) {
                val message = "This app requires to access on your photos."
                showAlertDialog("", message, DialogInterface.OnClickListener { _, _ ->
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_STORAGE_READ_ACCESS_PERMISSION
                    )
                }, getString(R.string.ok), null, null)
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
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
        photoPickerDialogFragment.show(
            supportFragmentManager,
            "add_photo_dialog_fragment"
        )
        photoPickerDialogFragment.dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    protected open fun didSelectPhoto(uri: Uri){

    }
    private fun getImageUri(inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }
    private fun startCropActivity(uri: Uri) {
        val time = System.currentTimeMillis()
        val destinationFileName = String.format("%d_profile_%d.jpg", app.currentUser.id, time)
        var uCrop = UCrop.of(uri, Uri.fromFile(File(cacheDir, destinationFileName)))

        uCrop = basisConfig(uCrop)
        uCrop = advancedConfig(uCrop)

        uCrop.start(this)
    }

    /**
     * In most cases you need only to set crop aspect ration and max size for resulting image.
     *
     * @param uCrop - ucrop builder instance
     * @return - ucrop builder instance
     */
    private fun basisConfig(uCrop: UCrop): UCrop {
        var crop = uCrop
        //crop = crop.withAspectRatio(1f, aspectRatio)
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

        options.setHideBottomControls(false)
        options.setFreeStyleCropEnabled(true)
        options.setCircleDimmedLayer(aspectRatio == 1f)
        // Aspect ratio options
//        options.setAspectRatioOptions(
//            1,
//            AspectRatio("WOW", 1f, 2f),
//            AspectRatio("MUCH", 3f, 4f),
//            AspectRatio("RATIO", CropImageView.DEFAULT_ASPECT_RATIO, CropImageView.DEFAULT_ASPECT_RATIO),
//            AspectRatio("SO", 16f, 9f),
//            AspectRatio("ASPECT", 1f, 1f)
//        )

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
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            showAlertDialog(
                getString(R.string.permission_title_rationale), rationale,
                DialogInterface.OnClickListener { _, _ ->
                    ActivityCompat.requestPermissions(
                        this@BaseImagePickerActivity,
                        arrayOf(permission), requestCode
                    )
                }, getString(R.string.ok), null, getString(R.string.cancel)
            )
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
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

}
