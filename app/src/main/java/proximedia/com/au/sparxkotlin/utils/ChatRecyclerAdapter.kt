package proximedia.com.au.sparxkotlin.utils

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import proximedia.com.au.sparxkotlin.R
import proximedia.com.au.sparxkotlin.models.Chat
import kotlinx.android.synthetic.main.item_message_received.view.*
import proximedia.com.au.sparxkotlin.common.SessionManager
import kotlin.collections.ArrayList

/**
 * Created by pc on 27/10/2017.
 */

    class ChatRecyclerAdapter(var chatList: ArrayList<Chat>) : RecyclerView.Adapter<ChatRecyclerAdapter.ViewHolder>() {

    private val VIEW_TYPE_MESSAGE_SENT = 1
    private val VIEW_TYPE_MESSAGE_RECEIVED = 2

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRecyclerAdapter.ViewHolder {

        val v : View
        if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            v = LayoutInflater.from(parent.context).inflate(R.layout.item_message_received, parent, false)
        } else {
            v = LayoutInflater.from(parent.context).inflate(R.layout.item_message_sent, parent, false)
        }

        return ViewHolder(v)
    }

    override fun getItemViewType(position: Int): Int {

        val msg = chatList[position]
        if ( msg.uid.equals(SessionManager.currentUser?.uid)) {
            Log.d("ChatRecyclerAdapter", "userid= ${msg.uid} | ${SessionManager.currentUser?.uid} | VIEW_TYPE_MESSAGE_RECEIVED= ${VIEW_TYPE_MESSAGE_RECEIVED} ")
            return VIEW_TYPE_MESSAGE_RECEIVED
        } else {
            Log.d("ChatRecyclerAdapter", "userid= ${msg.uid} | ${SessionManager.currentUser?.uid} | VIEW_TYPE_MESSAGE_SENT= ${VIEW_TYPE_MESSAGE_SENT} ")
            return VIEW_TYPE_MESSAGE_SENT
        }

    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ChatRecyclerAdapter.ViewHolder, position: Int) {
        holder.bindItems(chatList[position])

    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return chatList.size
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val message_body = itemView.message_body
        val message_time = itemView.message_time

        fun bindItems(chat: Chat) {

            message_body.text = chat.message
            message_time.text = chat.timestamp.toString()

        }
    }
    }