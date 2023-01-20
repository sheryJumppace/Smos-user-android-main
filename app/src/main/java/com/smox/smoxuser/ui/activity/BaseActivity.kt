package com.smox.smoxuser.ui.activity

import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.kaopiz.kprogresshud.KProgressHUD
import com.smox.smoxuser.R
import com.smox.smoxuser.App
import com.smox.smoxuser.manager.SessionManager
import com.smox.smoxuser.utils.showSnackbar

private const val DUMMY_VALUE = "dummy_value"
private const val RESTART_DELAY = 200

open class BaseActivity : AppCompatActivity() {

    protected lateinit var progressHUD: KProgressHUD
    protected lateinit var sessionManager: SessionManager
    protected lateinit var app: App
    protected var isTablet: Boolean = false

    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        progressHUD = KProgressHUD(this)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        isTablet = resources.getBoolean(R.bool.isTablet)


        sessionManager = SessionManager.getInstance(applicationContext)
        app = App.instance
        app.currentActivity = this::class.java
        /*app.currentUser = Barber(sessionManager.userData)
        Log.e("Base User data:-", app.currentUser.toString())*/

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.activity_left_to_right, R.anim.activity_right_to_left)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onPause() {
        super.onPause()
        hideProgressDialog()
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
        val builder = AlertDialog.Builder(this).setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveText, onPositiveButtonClickListener)
        if(negativeText != null){
            builder.setNegativeButton(negativeText, onNegativeButtonClickListener)
        }
        builder.create().show()
    }

    protected fun showErrorSnackbar(@StringRes resId: Int, e: Exception?, clickListener: View.OnClickListener?) {
        val rootView = window.decorView.findViewById<View>(android.R.id.content)
        rootView?.let {
            showSnackbar(it, resId, e, R.string.dlg_retry, clickListener)
        }
    }

    protected fun showProgressDialog(@StringRes messageId: Int) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this)
            progressDialog!!.isIndeterminate = true
            progressDialog!!.setCancelable(false)
            progressDialog!!.setCanceledOnTouchOutside(false)

            // Disable the back button
            val keyListener = DialogInterface.OnKeyListener { dialog,
                                                              keyCode,
                                                              event ->
                keyCode == KeyEvent.KEYCODE_BACK
            }
            progressDialog!!.setOnKeyListener(keyListener)
        }
        progressDialog!!.setMessage(getString(messageId))
        progressDialog!!.show()
    }

    protected fun hideProgressDialog() {
        progressDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }

    fun restartApp(context: Context) {
        // Application needs to restart when user declined some permissions at runtime
        val restartIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val intent = PendingIntent.getActivity(context, 0, restartIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.set(AlarmManager.RTC, System.currentTimeMillis() + RESTART_DELAY, intent)
        System.exit(0)
    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}