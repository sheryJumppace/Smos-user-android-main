package com.smox.smoxuser.ui.fragment.barber


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.ContentUris
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.smox.smoxuser.R
import com.smox.smoxuser.model.Appointment
import com.smox.smoxuser.model.Contact
import com.smox.smoxuser.ui.activity.barber.BookingAppointmentActivity
import com.smox.smoxuser.ui.fragment.BaseFragment
import com.smox.smoxuser.utils.shortToast
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
open class ContactPickerFragment : BaseFragment() {

    companion object {
        private const val PICK_CONTACT = 101
        private const val REQUEST_READ_CONTACTS_PERMISSION = 102
    }

    private var selectedDate: Long = 0

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_READ_CONTACTS_PERMISSION -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFromContacts()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    @SuppressLint("Recycle")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_CONTACT && resultCode == RESULT_OK) {
            val contactUri = data?.data
            context?.contentResolver?.apply {
                val contentResolver = this
                val cursor = query(contactUri!!, null, null, null, null)
                cursor?.apply {
                    if (moveToFirst()) {
                        getContact(cursor, contentResolver)
                    }
                }
            }
        }
    }

    @SuppressLint("Recycle")
    private fun getContact(cursor: Cursor, contentResolver: ContentResolver) {


        val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
        val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
        val hasPhone = (cursor.getString(
            cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
        )).toInt()

        if (hasPhone > 0) {
            val cursorPhone = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.Contacts._ID + " = ? ",
                arrayOf(id),
                null
            ) ?: return

            var phoneNumber = ""
            var photo: Bitmap? = null
            while (cursorPhone.moveToNext()) {
                phoneNumber =
                    cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                //Fetch Photo
                val inputStream = getPhoto(id.toLong(), contentResolver)
                if (inputStream != null) {
                    photo = BitmapFactory.decodeStream(inputStream)
                }
            }
            if (phoneNumber.isNotEmpty()) {
                val contact = Contact(name, phoneNumber, photo)
                val intent = Intent(activity, BookingAppointmentActivity::class.java)
                intent.putExtra("barber", app.currentUser)
                val appointment = Appointment()
                //appointment.services = app.currentUser.services
                intent.putExtra("appointment", appointment)
                intent.putExtra("contact", contact)
                intent.putExtra("selectedDate", selectedDate)
                startActivity(intent)
                activity!!.overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
            } else {
                shortToast("Contact has no phone number.")
            }
            cursorPhone.close()
        }
    }

    @SuppressLint("Recycle")
    private fun getPhoto(contactId: Long, contentResolver: ContentResolver): InputStream? {
        val contactUri =
            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        val photoUri =
            Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)
        val cursor = contentResolver.query(
            photoUri,
            arrayOf(ContactsContract.Contacts.Photo.PHOTO), null, null, null
        ) ?: return null
        cursor.use {
            if (it.moveToFirst()) {
                val data = it.getBlob(0)
                if (data != null) {
                    return ByteArrayInputStream(data)
                }
            }
        }

        val displayPhotoUri =
            Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO)
        return try {
            val fd =
                contentResolver.openAssetFileDescriptor(displayPhotoUri, "r")
            fd!!.createInputStream()
        } catch (e: IOException) {
            null
        }
    }

    fun pickFromContacts(selectedDateMillis: Long = 0) {
        selectedDate = selectedDateMillis
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission(
                Manifest.permission.READ_CONTACTS,
                getString(R.string.permission_read_contacts_rationale),
                REQUEST_READ_CONTACTS_PERMISSION
            )
        } else {
            /* val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
             startActivityForResult(intent, PICK_CONTACT)*/

            val intent = Intent(Intent.ACTION_PICK)
            intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE)
            startActivityForResult(intent, PICK_CONTACT)
        }
    }

    /**
     * Requests given permission.
     * If the permission has been denied previously, a Dialog will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private fun requestPermission(permission: String, rationale: String, requestCode: Int) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permission)) {
            showAlertDialog(
                getString(R.string.permission_title_rationale), rationale,
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
    private fun showAlertDialog(
        title: String?, message: String?,
        onPositiveButtonClickListener: DialogInterface.OnClickListener?,
        positiveText: String,
        onNegativeButtonClickListener: DialogInterface.OnClickListener?,
        negativeText: String
    ) {
        val builder = AlertDialog.Builder(activity!!)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(positiveText, onPositiveButtonClickListener)
        builder.setNegativeButton(negativeText, onNegativeButtonClickListener)
        builder.show()
    }

}
