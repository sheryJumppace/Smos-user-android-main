//package com.smox.smoxuser.ui.fragment.chat

package com.smox.smoxuser.ui.fragment.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.ibcemobile.smoxstyler.adapter.ChatsListAdapter
import com.ibcemobile.smoxstyler.model.ChatUsers
import com.smox.smoxuser.databinding.FragmentRecentGroupBinding
import com.smox.smoxuser.App
import com.smox.smoxuser.manager.Constants.API.BARBER_ID
import com.smox.smoxuser.manager.Constants.API.BARBER_NAME
import com.smox.smoxuser.manager.Constants.API.CHAT_ROOM
import com.smox.smoxuser.manager.Constants.API.CHAT_ROOM_ID
import com.smox.smoxuser.manager.Constants.API.MESSAGE_COUNT
import com.smox.smoxuser.ui.activity.customer.SingleChatActivity
import com.smox.smoxuser.ui.fragment.BaseFragment

class RecentGroupFragment : BaseFragment(), ChatsListAdapter.MyItemClickListener {

    private val ARG_PARAM = "isGroup"
    private var isGroup: Boolean = false
    lateinit var dbRef: DatabaseReference
    var chatUserList: ArrayList<ChatUsers>? = null
    lateinit var binding: FragmentRecentGroupBinding
    lateinit var chatRoomEvent:ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isGroup = it.getBoolean(ARG_PARAM)
        }

        dbRef = FirebaseDatabase.getInstance().reference.child("chatRoom")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRecentGroupBinding.inflate(inflater, container, false)
        layoutInflater

        chatUserList = ArrayList()
        if (!isGroup) {
            receiveUsers()
        }

        return binding.root
    }

    private fun receiveUsers() {
        Log.e("TAG", "calling receiveUsers")
        chatRoomEvent=object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.e("", "firebase onDataChange: ")
                if (dataSnapshot.exists()) {
                    binding.txtError.visibility = View.GONE
                    chatUserList!!.clear()
                    for (snapshot in dataSnapshot.children) {
                        val barberId: String = snapshot.child("user_id").getValue(String::class.java)!!
                        if (barberId == sessionManager.userId.toString()) {
                            val chat: ChatUsers? = snapshot.getValue(ChatUsers::class.java)
                            chatUserList!!.add(chat!!)
                        }
                    }
                    val linearLayoutManager = LinearLayoutManager(
                        App.instance
                    )
                    binding.rvChat.layoutManager = linearLayoutManager

                    val chatUserAdapter = ChatsListAdapter(App.instance, this@RecentGroupFragment, sessionManager.userId.toString())
                    binding.rvChat.adapter = chatUserAdapter
                    chatUserAdapter.setData(chatUserList!!)
                    if (chatUserList!!.size > 0) {
                        binding.txtError.visibility = View.GONE
                        binding.rvChat.visibility = View.VISIBLE
                    } else {
                        binding.txtError.visibility = View.VISIBLE
                        binding.rvChat.visibility = View.GONE
                    }
                } else {
                    binding.txtError.visibility = View.VISIBLE
                    binding.rvChat.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("", "onCancelled: " + error.details)
            }

        }
    }

    override fun onResume() {
        super.onResume()
        dbRef.addValueEventListener(chatRoomEvent)

    }

    override fun onPause() {
        super.onPause()
        dbRef.removeEventListener(chatRoomEvent)
    }

    override fun clicked(users: ChatUsers) {
        val intent = Intent(requireActivity(), SingleChatActivity::class.java)
        intent.putExtra(CHAT_ROOM, users.chat_room_id)
        intent.putExtra(BARBER_ID, users.barber_id)
        intent.putExtra(BARBER_NAME, users.barber_name)
        intent.putExtra(CHAT_ROOM_ID, users.id)
        if (users.sender_id.equals(sessionManager.userId.toString()))
        intent.putExtra(MESSAGE_COUNT, users.message_count?.toInt())

        startActivity(intent)
    }

    fun newInstance(isGroup: Boolean): RecentGroupFragment {
        val fragment = RecentGroupFragment()
        val args = Bundle()
        args.putBoolean(ARG_PARAM, isGroup)
        fragment.arguments = args
        return fragment
    }


}