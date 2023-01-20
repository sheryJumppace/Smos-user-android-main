package com.smox.smoxuser.ui.activity.chat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.users.model.QBUser
import com.smox.smoxuser.R
import com.smox.smoxuser.ui.activity.BaseActivity
import com.smox.smoxuser.ui.adapter.QBUsersAdapter
import com.smox.smoxuser.utils.chat.ChatHelper
import com.smox.smoxuser.utils.qb.QbUsersHolder
import com.smox.smoxuser.utils.shortToast
import kotlinx.android.synthetic.main.activity_chat_info.*

private const val EXTRA_DIALOG = "extra_dialog"

class ChatInfoActivity : BaseActivity() {

    private lateinit var usersListView: ListView
    private lateinit var qbDialog: QBChatDialog

    companion object {
        fun start(activity: Activity, qbDialog: QBChatDialog) {
            val intent = Intent(activity, ChatInfoActivity::class.java)
            intent.putExtra(EXTRA_DIALOG, qbDialog)
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_info)

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            //setHomeAsUpIndicator(ContextCompat.getDrawable(this@ChatInfoActivity, Constants.backButton))
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        usersListView = findViewById(R.id.list_chat_info_users)
        qbDialog = intent.getSerializableExtra(EXTRA_DIALOG) as QBChatDialog
        getDialog()
    }

    private fun getDialog() {
        val dialogID = qbDialog.dialogId
        ChatHelper.getDialogById(dialogID, object : QBEntityCallback<QBChatDialog> {
            override fun onSuccess(qbChatDialog: QBChatDialog, bundle: Bundle?) {
                qbDialog = qbChatDialog
                buildUserList()
            }

            override fun onError(e: QBResponseException) {
                shortToast(e.message)
                finish()
            }
        })
    }

    private fun buildUserList() {
        val userIds = qbDialog.occupants
        val users = QbUsersHolder.getUsersByIds(userIds)
        val adapter = QBUsersAdapter(this, users as MutableList<QBUser>)
        usersListView.adapter = adapter
    }
}