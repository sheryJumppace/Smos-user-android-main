package com.smox.smoxuser.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.smox.smoxuser.model.Chats
import com.smox.smoxuser.R
import com.smox.smoxuser.manager.Constants.API.MESSAGE_TYPE_IMAGE
import com.smox.smoxuser.manager.SessionManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MessageAdapter(private val mContext: Context, private val mChat: ArrayList<Chats>, val userId:String) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = if (viewType == MSG_TYPE_RIGHT) {
            LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false)
        } else {
            LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false)
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = mChat[position]
        if (chat.message_type == MESSAGE_TYPE_IMAGE) {
            holder.rl_image.visibility = View.VISIBLE
            holder.rl_message.visibility = View.GONE
            holder.time_tv1.text = holder.convertTime(chat.msg_time)
            Glide.with(mContext).load(chat.message)
                .thumbnail(0.05f)
                .into(holder.image_upload)

        } else {
            holder.rl_image.visibility = View.GONE
            holder.rl_message.visibility = View.VISIBLE
            holder.show_message.text = chat.message
            if (chat.msg_time != null) {
                holder.time_tv.text = holder.convertTime(chat.msg_time)
            }
            if (chat.sender_profile != null) {
                Glide.with(mContext).load(chat.sender_profile)
                    .thumbnail(0.05f)
                    .into(holder.profile_image)
            }

        }

        if (chat.is_seen!!) {
            holder.imgSeen.setImageResource(R.drawable.ic_baseline_done_all_24)
        } else {
            holder.imgSeen.setImageResource(R.drawable.ic_baseline_gray_done_all_24)
        }


    }

    override fun getItemCount(): Int {
        return mChat.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (mChat[position].sender_id.equals(SessionManager.getInstance(mContext).userId.toString())) {
            MSG_TYPE_RIGHT
        } else {
            MSG_TYPE_LEFT
        }
    }

    fun addItem(chat: Chats?) {
        val size=mChat.size
        mChat.add(chat!!)
        notifyItemInserted(size)

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var show_message: TextView
        var profile_image: ImageView
        var image_upload: ImageView
        var time_tv: TextView
        var time_tv1: TextView
        var rl_image: RelativeLayout
        var rl_message: RelativeLayout
        var imgSeen: ImageView
        fun convertTime(time: Long?): String {
            val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
            return formatter.format(Date(time!!))
        }

        init {
            show_message = itemView.findViewById(R.id.show_message)
            profile_image = itemView.findViewById(R.id.profile_image)
            time_tv = itemView.findViewById(R.id.time_tv)
            time_tv1 = itemView.findViewById(R.id.time_tv1)
            image_upload = itemView.findViewById(R.id.image_upload)
            rl_image = itemView.findViewById(R.id.rl_image)
            rl_message = itemView.findViewById(R.id.rl_message)
            imgSeen = itemView.findViewById(R.id.imgSeen)
        }
    }

    companion object {
        const val MSG_TYPE_LEFT = 0
        const val MSG_TYPE_RIGHT = 1
    }
}