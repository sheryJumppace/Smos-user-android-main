package com.smox.smoxuser.ui.fragment.chat

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.AdapterView
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBDialogType

import com.smox.smoxuser.databinding.FragmentDialogsBinding
import com.smox.smoxuser.ui.adapter.DialogsAdapter
import com.smox.smoxuser.utils.qb.QbDialogHolder


class DialogsFragment : Fragment() {


     private val ARG_PARAM = "isGroup"
     private var isGroup: Boolean = false
     private lateinit var dialogsAdapter: DialogsAdapter

    fun newInstance(isGroup: Boolean): DialogsFragment {
        val fragment = DialogsFragment()
        val args = Bundle()
        args.putBoolean(ARG_PARAM, isGroup)
        fragment.arguments = args
        return fragment
    }

    private var onDialogClick: DialogClickListener? = null

    fun setDialogClickListener(clickListener: DialogClickListener) {
        onDialogClick = clickListener
    }
    interface DialogClickListener {
        fun onDialogClick(dialog: QBChatDialog)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isGroup = it.getBoolean(ARG_PARAM)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentDialogsBinding.inflate(inflater, container, false)
        val dialogsListView = binding.listDialogsChats
        dialogsAdapter = DialogsAdapter(context!!, getDialogs())
        dialogsListView.adapter = dialogsAdapter
        dialogsListView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, id ->
                val selectedDialog = parent.getItemAtPosition(position) as QBChatDialog
                onDialogClick?.onDialogClick(selectedDialog)
            }
        return binding.root
    }

    private fun getDialogs():List<QBChatDialog>{
        val allDialogs = ArrayList(QbDialogHolder.dialogsMap.values)
        return if(isGroup) allDialogs.filter { it -> it.type == QBDialogType.GROUP }
               else allDialogs
    }
    fun updateDialogsAdapter() {
        dialogsAdapter.updateList(getDialogs())
    }
}
