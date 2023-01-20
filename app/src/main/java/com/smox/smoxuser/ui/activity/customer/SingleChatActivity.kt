package com.smox.smoxuser.ui.activity.customer

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.google.gson.JsonObject
import com.smox.smoxuser.R
import com.smox.smoxuser.databinding.ActivitySingleChatBinding
import com.smox.smoxuser.manager.Constants.API.BARBER_ID
import com.smox.smoxuser.manager.Constants.API.BARBER_NAME
import com.smox.smoxuser.manager.Constants.API.CHATS_TABLE
import com.smox.smoxuser.manager.Constants.API.CHAT_ROOM
import com.smox.smoxuser.manager.Constants.API.CHAT_ROOM_ID
import com.smox.smoxuser.manager.Constants.API.CHAT_ROOM_TABLE
import com.smox.smoxuser.manager.Constants.API.COL_LAST_MESSAGE
import com.smox.smoxuser.manager.Constants.API.COL_LAST_MESSAGE_TIME
import com.smox.smoxuser.manager.Constants.API.COL_SENDER_ID
import com.smox.smoxuser.manager.Constants.API.MESSAGE_COUNT
import com.smox.smoxuser.manager.Constants.API.MESSAGE_TYPE_IMAGE
import com.smox.smoxuser.manager.Constants.API.MESSAGE_TYPE_TEXT
import com.smox.smoxuser.manager.Constants.API.USER_STATUS_TABLE
import com.smox.smoxuser.model.Chats
import com.smox.smoxuser.model.SimpleOkResponse2
import com.smox.smoxuser.retrofit.ApiRepository
import com.smox.smoxuser.ui.activity.BaseImagePickerActivity
import com.smox.smoxuser.ui.adapter.MessageAdapter
import com.smox.smoxuser.utils.ImageUploadUtils
import com.smox.smoxuser.utils.listeners.UploadImages
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SingleChatActivity : BaseImagePickerActivity(), UploadImages {
    private var chatRoom = ""
    private var chatRoomId = ""
    private var barberId = ""
    lateinit var binding: ActivitySingleChatBinding
    lateinit var dbRef: DatabaseReference
    var chatList: ArrayList<Chats>? = null
    lateinit var messageAdapter: MessageAdapter
    lateinit var chatChildEvent: ChildEventListener
    private var messageUnseenCount = 0
    var isOnline = false
    var isTyping = true
    lateinit var typingRunnable: Runnable
    val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingleChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbRef = FirebaseDatabase.getInstance().reference.child(CHATS_TABLE)
        chatList = ArrayList()
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.etTypeHere.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                updateStatus("typing...")
                handler.removeCallbacks(typingRunnable)
                handler.postDelayed(typingRunnable, 2000)
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }
        })

        typingRunnable = Runnable {
            updateStatus("online")
        }
        binding.sendButton.setOnClickListener {
            if (binding.etTypeHere.text.toString().isNotEmpty()) {
                updateStatus("online")
                handler.removeCallbacks(typingRunnable)
                sendMessage(binding.etTypeHere.text.toString(), MESSAGE_TYPE_TEXT)
                binding.etTypeHere.setText("")
            }
        }
        binding.addImage.setOnClickListener {
            didOpenPhotoOption()
        }

        if (intent.getStringExtra(CHAT_ROOM) != null) {
            chatRoom = intent.getStringExtra(CHAT_ROOM).toString()
            chatRoomId = intent.getStringExtra(CHAT_ROOM_ID).toString()
            barberId = intent.getStringExtra(BARBER_ID).toString()
            binding.username.text = intent.getStringExtra(BARBER_NAME).toString()
            messageUnseenCount = intent.getIntExtra(MESSAGE_COUNT, 0)
        }

        getUserStatus()
        setChatAdapter()
        getNewMessageEvent()
        moreOption()
    }

    private fun setChatAdapter() {
        binding.rvChat.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        binding.rvChat.layoutManager = linearLayoutManager
        messageAdapter =
            MessageAdapter(this@SingleChatActivity, chatList!!, app.currentUser.id.toString())
        binding.rvChat.adapter = messageAdapter
    }


    private fun getNewMessageEvent() {

        chatChildEvent = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chat: Chats? = snapshot.getValue(Chats::class.java)
                updateMessageSeen(snapshot, chat!!)
                messageAdapter.addItem(chat)
                binding.rvChat.scrollToPosition(chatList?.size!! - 1)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chat: Chats? = snapshot.getValue(Chats::class.java)
                val pos = chatList?.indexOfFirst { it.id == chat?.id }
                chatList?.get(pos!!)?.is_seen = chat?.is_seen
                messageAdapter.notifyItemChanged(pos!!)
                messageUnseenCount = 0
                updateLastMessageCount(messageUnseenCount.toString())
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
    }

    private fun sendMessage(message: String?, message_type: Int?) {
        val msgId = dbRef.child(chatRoom).push().key
        val data = HashMap<String, Any?>()
        data["id"] = msgId
        data["sender_id"] = sessionManager.userId.toString()
        data["receiver_id"] = barberId
        data["message"] = message
        data["message_type"] = message_type
        data["msg_time"] = System.currentTimeMillis()
        data["is_seen"] = isOnline
        data["sender_name"] = app.currentUser.firstName
        data["sender_profile"] = app.currentUser.imageUrl
        dbRef.child(chatRoom).child(msgId!!).setValue(data)

        messageUnseenCount++

        updateLastMessage(message,
            messageUnseenCount.toString(),
            System.currentTimeMillis().toString())

        val jsonObject = JsonObject()
        jsonObject.addProperty("sender_id", sessionManager.userId.toString())
        jsonObject.addProperty("receiver", barberId)
        jsonObject.addProperty("title", app.currentUser.firstName)
        jsonObject.addProperty("message", message)

        sendMessageNotification(jsonObject)

    }

    private fun sendMessageNotification(jsonObject: JsonObject) {

        Log.e(TAG, "sendMessageNotification: $jsonObject")
        ApiRepository(this).sendChatNotification(jsonObject).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<SimpleOkResponse2> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(res: SimpleOkResponse2) {

                }

                override fun onError(e: Throwable) {

                }

                override fun onComplete() {

                }

            })
    }

    private fun updateLastMessage(message: String?, msgCount: String, currentTimeMillis: String) {

        val database =
            FirebaseDatabase.getInstance().reference.child(CHAT_ROOM_TABLE).child(chatRoomId)
        database.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val hashMap = java.util.HashMap<String, Any>()
                hashMap[MESSAGE_COUNT] = msgCount
                hashMap[COL_LAST_MESSAGE] = message!!
                hashMap[COL_LAST_MESSAGE_TIME] = currentTimeMillis
                hashMap[COL_SENDER_ID] = sessionManager.userId.toString()
                FirebaseDatabase.getInstance().getReference(CHAT_ROOM_TABLE).child(chatRoomId)
                    .updateChildren(hashMap)
            }
        }
    }

    private fun updateLastMessageCount(messageCount: String) {
        val database =
            FirebaseDatabase.getInstance().reference.child(CHAT_ROOM_TABLE).child(chatRoomId)
        database.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val hashMap = java.util.HashMap<String, Any>()
                hashMap[MESSAGE_COUNT] = messageCount
                FirebaseDatabase.getInstance().getReference(CHAT_ROOM_TABLE).child(chatRoomId)
                    .updateChildren(hashMap)
            }
        }
    }

    /*private fun receiveAllMessage() {
        dbRef.child(chatRoom).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (task.result.exists()) {
                    chatList!!.clear()
                    for (snapshot in task.result.children) {
                        val chat: Chats? = snapshot.getValue(Chats::class.java)
                        chatList!!.add(chat!!)
                        updateMessageSeen(snapshot, chat)

                    }
                    updateLastMessage(MESSAGE_COUNT, "0")
                    messageAdapter.notifyDataSetChanged()
                }
            }
        }
    }*/

    fun updateMessageSeen(snapshot: DataSnapshot, chat: Chats) {
        if (chat.receiver_id == app.currentUser.id.toString()) {
            val hashMap = java.util.HashMap<String, Any>()
            hashMap["is_seen"] = true
            snapshot.ref.updateChildren(hashMap)
        }
    }

    override fun didSelectPhoto(uri: Uri) {
        super.didSelectPhoto(uri)
        progressHUD.show()
        val imageUploadUtils = ImageUploadUtils()
        imageUploadUtils.onUpload(this, uri.path!!, this)
    }

    private fun updateStatus(status: String) {
        val reference =
            FirebaseDatabase.getInstance().getReference(USER_STATUS_TABLE).child(chatRoom)
                .child(app.currentUser.id.toString())
        val hashMap = java.util.HashMap<String, Any>()
        hashMap["status"] = status
        reference.updateChildren(hashMap)
    }


    private fun blockUser() {

    }

    override fun onResume() {
        super.onResume()
        updateStatus("online") //receiveAllMessage()
        registerAllEvents()

    }

    override fun onPause() {
        super.onPause()
        updateStatus(System.currentTimeMillis().toString())
        unregisterAllEvents()
    }

    private fun unregisterAllEvents() {
        dbRef.child(chatRoom).removeEventListener(chatChildEvent)
    }

    private fun registerAllEvents() {
        dbRef.child(chatRoom).addChildEventListener(chatChildEvent)
    }

    companion object {
        const val TAG = "SingleChatActivity"
    }

    override fun upload(imageUrl: String) {
        progressHUD.dismiss()
        sendMessage(imageUrl, MESSAGE_TYPE_IMAGE)
    }

    override fun uploadError() {
        progressHUD.dismiss()
    }

    /**
     * Get user Typing..,Online,Offline status
     */
    private fun getUserStatus() {
        val database = FirebaseDatabase.getInstance().getReference(USER_STATUS_TABLE)
        database.child(chatRoom).child(barberId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val status = dataSnapshot.child("status").getValue(String::class.java)

                        if (status != "online" && status != "typing..." && status != "last seen recently") {
                            binding.onlineStatus.text = getSmsTodayYestFromMilli(status!!.toLong())
                        }
                        else {
                            binding.onlineStatus.text = status
                        }
                        isOnline = status == "online"
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }


    private fun moreOption() {
        binding.moreOption.setOnClickListener { view: View? ->
            val popup = PopupMenu(this, view!!, Gravity.START)
            popup.menuInflater.inflate(R.menu.activity_chat,
                popup.menu) //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener { item: MenuItem ->
                if (item.title == "Block") {
                    blockUser()
                }
                else if (item.title == "Report") {/*val bottomSheetAddressFragment = BottomSheetBlockDialog()
                    bottomSheetAddressFragment.setCancelable(true)
                    bottomSheetAddressFragment.show(
                        supportFragmentManager,
                        TAG
                    )*/
                }
                true
            }
            popup.show()
        }
    }

    fun getSmsTodayYestFromMilli(msgTimeMillis: Long): String {
        val messageTime = Calendar.getInstance()
        messageTime.timeInMillis = msgTimeMillis
        val now = Calendar.getInstance()
        val strTimeFormate = "h:mm aa"
        val strDateFormate = "EEE',' MMM d y',' h:mm aa"
        return if (now[Calendar.DATE] === messageTime[Calendar.DATE] && now[Calendar.MONTH] === messageTime[Calendar.MONTH] && now[Calendar.YEAR] === messageTime[Calendar.YEAR]) {
            "today at " + DateFormat.format(strTimeFormate, messageTime)
        }
        else if (now[Calendar.DATE] - messageTime[Calendar.DATE] === 1 && now[Calendar.MONTH] === messageTime[Calendar.MONTH] && now[Calendar.YEAR] === messageTime[Calendar.YEAR]) {
            "yesterday at " + DateFormat.format(strTimeFormate, messageTime)
        }
        else {
            "" + DateFormat.format(strDateFormate, messageTime)
        }
    }
}