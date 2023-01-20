package com.smox.smoxuser.ui.activity.chat

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.request.QBPagedRequestBuilder
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import com.smox.smoxuser.R
import com.smox.smoxuser.data.ContactRepository
import com.smox.smoxuser.databinding.ActivityGroupBinding
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.adapter.GroupAdapter
import com.smox.smoxuser.utils.shortToast
import com.smox.smoxuser.viewmodel.ContactListViewModel
import com.smox.smoxuser.viewmodel.ContactListViewModelFactory

private const val EXTRA_QB_DIALOG = "qb_dialog"
const val EXTRA_QB_USERS = "qb_users"

class GroupActivity : BaseActivity() {
    val EXTRA_QB_USERS = "qb_users"
    lateinit var txtGroupName:EditText
    private lateinit var viewModel: ContactListViewModel
    private var qbChatDialog: QBChatDialog? = null

    companion object {
        fun startForResult(activity: Activity, code: Int, dialog: QBChatDialog?) {
            val intent = Intent(activity, AddContactActivity::class.java)
            intent.putExtra(EXTRA_QB_DIALOG, dialog)
            activity.startActivityForResult(intent, code)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding:ActivityGroupBinding = DataBindingUtil.setContentView(this, R.layout.activity_group)
        setSupportActionBar(binding.toolbar)
        with(supportActionBar){
            if(this != null){
                setDisplayShowHomeEnabled(true)
                setDisplayHomeAsUpEnabled(true)
                //setHomeAsUpIndicator(ContextCompat.getDrawable(this@GroupActivity, Constants.backButton))
            }
        }

        intent.getSerializableExtra(EXTRA_QB_DIALOG)?.let {
            qbChatDialog = it as QBChatDialog
            txtGroupName.setText(qbChatDialog!!.name)
        }

        txtGroupName = binding.txtGroupName

        val factory = ContactListViewModelFactory(ContactRepository.getInstance(), false)
        viewModel = ViewModelProvider(this, factory).get(ContactListViewModel::class.java)

        val adapter = GroupAdapter()
        binding.contactList.adapter = adapter
        subscribeUi(adapter)
    }


    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_add_contact -> {
                addContact()
                return true
            }
            R.id.menu_save -> {
                createGroup()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addContact() {
        val intent = Intent(this@GroupActivity, AddContactActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
    }

    private fun subscribeUi(adapter: GroupAdapter) {
        viewModel.contacts.observe(this, androidx.lifecycle.Observer { contacts ->
            if(contacts != null) adapter.submitList(contacts)
        })
    }

    @SuppressLint("ShowToast")
    private fun createGroup(){
        if(txtGroupName.text.isEmpty()){
            shortToast(resources.getString(R.string.err_group_name))
            return
        }
        val contacts = viewModel.getSelectedContacts()
        if(contacts.isEmpty()){
            shortToast(resources.getString(R.string.err_select_contact))
            return
        }
        val emails = contacts.map {it -> it.email}
        checkValidUser(emails)
    }

    private fun checkValidUser(contacts:List<String>){
        progressHUD.show()
        QBUsers.getUsersByEmails(contacts, QBPagedRequestBuilder()).performAsync(object : QBEntityCallback<ArrayList<QBUser>> {
            @SuppressLint("ShowToast")
            override fun onError(error: QBResponseException?) {
                progressHUD.dismiss()
                shortToast("You can't create chat with the selected users, who are not registered on chat server")
            }

            override fun onSuccess(users: ArrayList<QBUser>?, p1: Bundle?) {
                progressHUD.dismiss()
                if(users.isNullOrEmpty()) return
                passResultToCallerActivity(users)
            }
        })
    }

    private fun passResultToCallerActivity(users: ArrayList<QBUser>) {
        val result = Intent()
        result.putExtra(EXTRA_QB_USERS, users)
        result.putExtra(EXTRA_CHAT_NAME, txtGroupName.text.toString())
        setResult(RESULT_OK, result)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.didDeselectAll()
    }
}
