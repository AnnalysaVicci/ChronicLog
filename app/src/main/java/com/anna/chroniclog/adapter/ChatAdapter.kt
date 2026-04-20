package com.anna.chroniclog.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anna.chroniclog.databinding.ItemChatReceivedBinding
import com.anna.chroniclog.databinding.ItemChatSentBinding
import com.anna.chroniclog.model.ChatMessage

class ChatAdapter(
    private val currentUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var messages = listOf<ChatMessage>()
    private val VIEW_TYPE_SENT = 1     // logged-in user
    private val VIEW_TYPE_RECEIVED = 2 // other members

    fun submitList(newList: List<ChatMessage>) {
        messages = newList
        notifyDataSetChanged()
    }

    inner class SentMessageViewHolder(val binding: ItemChatSentBinding) :
        RecyclerView.ViewHolder(binding.root)
    inner class ReceivedMessageViewHolder(val binding: ItemChatReceivedBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        // The logic remains the same: compare senderId to the active user's ID
        return if (messages[position].senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemChatSentBinding.inflate(inflater, parent, false)
            SentMessageViewHolder(binding)
        } else {
            val binding = ItemChatReceivedBinding.inflate(inflater, parent, false)
            ReceivedMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        when (holder) {
            is SentMessageViewHolder -> {
                holder.binding.tvMessageBody.text = message.text
            }
            is ReceivedMessageViewHolder -> {
                with(holder.binding) {
                    tvSenderName.text = message.senderName
                    tvMessageBody.text = message.text
                }
            }
        }
    }

    override fun getItemCount() = messages.size

    fun updateMessages(newMessages: List<ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }
}