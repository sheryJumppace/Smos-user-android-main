package com.smox.smoxuser.ui.fragment.chat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.quickblox.chat.QBChatService
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.request.QBPagedRequestBuilder
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import com.smox.smoxuser.R
import com.smox.smoxuser.data.ContactRepository
import com.smox.smoxuser.databinding.FragmentContactsBinding
import com.smox.smoxuser.model.SmoxUser
import com.smox.smoxuser.ui.activity.chat.ChatActivity
import com.smox.smoxuser.ui.activity.chat.EXTRA_DIALOG_ID
import com.smox.smoxuser.ui.adapter.ContactAdapter
import com.smox.smoxuser.ui.fragment.BaseFragment
import com.smox.smoxuser.utils.chat.ChatHelper
import com.smox.smoxuser.utils.managers.DialogsManager
import com.smox.smoxuser.utils.qb.QbDialogHolder
import com.smox.smoxuser.utils.qb.QbUsersHolder
import com.smox.smoxuser.utils.shortToast
import com.smox.smoxuser.viewmodel.ContactListViewModel
import com.smox.smoxuser.viewmodel.ContactListViewModelFactory
import kotlin.collections.ArrayList

class ContactsFragment : BaseFragment() {

    private val ARG_PARAM = "isFavorite"
    private var isFavorite: Boolean = false


    private lateinit var viewModel: ContactListViewModel

    fun newInstance(isFavorite: Boolean): ContactsFragment {
        val fragment = ContactsFragment()
        val args = Bundle()
        args.putBoolean(ARG_PARAM, isFavorite)
        fragment.arguments = args
        return fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isFavorite = it.getBoolean(ARG_PARAM)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentContactsBinding.inflate(inflater, container, false)

        val factory = ContactListViewModelFactory(ContactRepository.getInstance(), isFavorite)
        viewModel = ViewModelProviders.of(this, factory).get(ContactListViewModel::class.java)

        val adapter = ContactAdapter()
        binding.contactList.adapter = adapter
        adapter.setItemClickListener(clickListener = object: ContactAdapter.ItemClickListener{
            override fun onItemClick(view: View, user: SmoxUser) {
                checkValidUser(listOf(user.email))
            }

        })
        subscribeUi(adapter)
        return binding.root
    }

    private fun subscribeUi(adapter: ContactAdapter) {
        if(viewModel.contacts.value == null){
            viewModel.fetchList(activity!!)
        }
        viewModel.contacts.observe(viewLifecycleOwner, androidx.lifecycle.Observer { contacts ->
            if(contacts != null) adapter.submitList(contacts)
        })
    }
    private fun updateData() {
        with(viewModel) {
            fetchList(activity!!)
        }
    }

    private fun checkValidUser(contacts:List<String>){
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
                if(users.isNullOrEmpty()) return
                createDialog(users)
            }
        })

    }

    private fun createDialog(users: ArrayList<QBUser>) {
        QbUsersHolder.putUsers(users)
        if (isPrivateDialogExist(users)) {
            users.remove(ChatHelper.getCurrentUser())
            val existingPrivateDialog = QbDialogHolder.getPrivateDialogWithUser(users[0])
            if(existingPrivateDialog != null){
                openChatPage(existingPrivateDialog)
            }else{
                shortToast(R.string.dialogs_creation_error)
            }
        } else {
            progressHUD.show()
            ChatHelper.createDialogWithSelectedUsers(users, "",
                object : QBEntityCallback<QBChatDialog> {
                    override fun onSuccess(dialog: QBChatDialog, args: Bundle) {
                        progressHUD.dismiss()
                        DialogsManager().sendSystemMessageAboutCreatingDialog(QBChatService.getInstance().systemMessagesManager, dialog)
                        openChatPage(dialog)
                    }

                    @SuppressLint("ShowToast")
                    override fun onError(e: QBResponseException) {
                        progressHUD.dismiss()
                        shortToast(R.string.dialogs_creation_error)
                    }
                }
            )
        }
    }

    private fun isPrivateDialogExist(allSelectedUsers: java.util.ArrayList<QBUser>): Boolean {
        val selectedUsers = java.util.ArrayList<QBUser>()
        selectedUsers.addAll(allSelectedUsers)
        selectedUsers.remove(ChatHelper.getCurrentUser())
        return selectedUsers.size == 1 && QbDialogHolder.hasPrivateDialogWithUser(selectedUsers[0])
    }

    private fun openChatPage(dialog: QBChatDialog){
        val intent = Intent(activity, ChatActivity::class.java)
        intent.putExtra(EXTRA_DIALOG_ID, dialog)
        startActivity(intent)
        activity!!.overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit)
    }
}
