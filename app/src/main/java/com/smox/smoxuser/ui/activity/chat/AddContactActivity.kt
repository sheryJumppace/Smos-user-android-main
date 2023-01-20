package com.smox.smoxuser.ui.activity.chat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.databinding.DataBindingUtil
import com.android.volley.Request
import com.kaopiz.kprogresshud.KProgressHUD
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.request.QBPagedRequestBuilder
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import com.smox.smoxuser.R
import com.smox.smoxuser.data.ContactRepository
import com.smox.smoxuser.databinding.ActivityAddContactBinding
import com.smox.smoxuser.manager.APIHandler
import com.smox.smoxuser.manager.Constants
import com.smox.smoxuser.model.Address
import com.smox.smoxuser.model.SmoxUser
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.adapter.GroupAdapter
import com.smox.smoxuser.utils.shortToast
import okhttp3.internal.http2.Http2Reader
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

class AddContactActivity : BaseActivity() {
    val contacts: ArrayList<SmoxUser> = ArrayList()
    val adapter = GroupAdapter()
    private val contactlist = ArrayList<Address>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityAddContactBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_add_contact)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@AddContactActivity, Constants.backButton))
        }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    getUserByName(query)
                    binding.toolbar.requestFocus()
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })
        binding.contactList.adapter = adapter

    }

    override fun onResume() {
        super.onResume()
        hideKeyboard()
    }

    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_save -> {
                hideKeyboard()
                createContacts()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /*private fun hideKeyBoard() {
        val imm = this@AddContactActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        var view: View? = this@AddContactActivity.getCurrentFocus()
        if (view == null) {
            view = View(this@AddContactActivity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }*/

    private fun getUserByName(query: String) {
        val params = HashMap<String, String>()
        params["name"] = query
        val progressHUD = KProgressHUD(this)
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setCancellable(true)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
        progressHUD.show()

        APIHandler(
            applicationContext,
            Request.Method.GET,
            Constants.API.users_by_name,
            params,
            object : APIHandler.NetworkListener {
                override fun onResult(result: JSONObject) {
                    progressHUD.dismiss()
                    val jsonArray = result.getJSONArray("result")
                    contacts.clear()
                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray.getJSONObject(i)
                        val user = SmoxUser(json)
                        contacts.add(user)
                    }
                    adapter.submitList(contacts)
                    this@AddContactActivity.runOnUiThread(Runnable {
                        hideKeyboard()
                    })
                }

                override fun onFail(error: String?) {
                    progressHUD.dismiss()
                    shortToast(error)
                }
            })
    }

    private fun createContacts() {
        val users = contacts.filter { contact -> contact.isSelected.get() }
        if (users.isEmpty()) {
            shortToast(resources.getString(R.string.err_select_contact))
        } else {
            val ids = users.map { it -> it.id }.joinToString(",") { it.toString() }
            val params = HashMap<String, String>()
            params["contacts"] = ids
            val progressHUD = KProgressHUD(this)
            progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
            progressHUD.show()

            printMap(params)
            Log.e("Param is:- ", ids)

            APIHandler(
                applicationContext,
                Request.Method.POST,
                Constants.API.contacts,
                params,
                object : APIHandler.NetworkListener {
                    override fun onResult(result: JSONObject) {
                        progressHUD.dismiss()
                        this@AddContactActivity.runOnUiThread(Runnable {
                            hideKeyboard()
                        })
                        users.forEach {
                            it.isSelected.set(false)
                        }
                        val isAlreadyAdded = ContactRepository.getInstance().updateContacts(users)
                        val emails = users.map { it -> it.email }
                        checkValidUser(emails, isAlreadyAdded)
                        //finish()
                        this@AddContactActivity.runOnUiThread(Runnable {
                            hideKeyboard()
                        })
                    }

                    override fun onFail(error: String?) {
                        progressHUD.dismiss()
                        shortToast(error)
                    }
                })
        }

    }

    fun printMap(mp: Map<*, *>) {
        val it: Iterator<*> = mp.entries.iterator()
        while (it.hasNext()) {
            val pair = it.next() as Map.Entry<*, *>
            Http2Reader.logger.info(pair.key.toString() + " = " + pair.value)
        }
    }

    private fun checkValidUser(contacts: List<String>, isAlreadyAdded: Boolean) {
        progressHUD.show()
        QBUsers.getUsersByEmails(contacts, QBPagedRequestBuilder()).performAsync(object :
            QBEntityCallback<ArrayList<QBUser>> {
            @SuppressLint("ShowToast")
            override fun onError(error: QBResponseException?) {
                progressHUD.dismiss()
                shortToast("You can't create chat with the selected users, who are not registered on chat server")
            }

            override fun onSuccess(users: ArrayList<QBUser>?, p1: Bundle?) {
                progressHUD.dismiss()
                if (users.isNullOrEmpty()) return
                if (isAlreadyAdded) {
                    this@AddContactActivity.runOnUiThread(Runnable {
                        hideKeyboard()
                        val builder = AlertDialog.Builder(
                            ContextThemeWrapper(
                                this@AddContactActivity,
                                R.style.DialogTheme
                            )
                        )
                        builder.setMessage("Oho, its indicating you selected user already there in your list.")
                        builder.setPositiveButton("Ok") { _, _ ->
                            passResultToCallerActivity(users)
                        }
                        builder.show()
                        hideKeyboard()
                    })
                } else passResultToCallerActivity(users)
            }
        })

    }

    private fun passResultToCallerActivity(users: ArrayList<QBUser>) {
        val result = Intent()
        result.putExtra(EXTRA_QB_USERS, users)
        setResult(RESULT_OK, result)
        finish()
    }
}
