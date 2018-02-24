package com.jencisov.smack.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.jencisov.smack.R
import com.jencisov.smack.model.Message
import com.jencisov.smack.services.UserDataService
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(val context: Context, val messages: ArrayList<Message>)
    : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false)
        return ViewHolder(view);
    }

    override fun getItemCount() = messages.count()

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.bindMessage(context, messages[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userImage = itemView.findViewById<ImageView>(R.id.item_message_iv)
        private val timeStamp = itemView.findViewById<TextView>(R.id.item_message_date_tv)
        private val userName = itemView.findViewById<TextView>(R.id.item_message_name_tv)
        private val messageBody = itemView.findViewById<TextView>(R.id.item_message_text_tv)

        fun bindMessage(context: Context, message: Message) {
            val resourceId = context.resources.getIdentifier(message.userAvatar, "drawable", context.packageName)

            this.userImage.setImageResource(resourceId)
            this.userImage.setBackgroundColor(UserDataService.returnAvatarColor(message.userAvatarColor))
            this.userName.text = message.userName
            this.timeStamp.text = message.timeStamp
            this.messageBody.text = returnDateString(message.timeStamp)
        }

        fun returnDateString(isoString: String): String {
            val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormatter.timeZone = TimeZone.getTimeZone("UTC")

            var convertedDate = Date()
            try {
                convertedDate = isoFormatter.parse(isoString)
            } catch (e: ParseException) {
                Log.d("PARSE", "Cannot parse date")
            }

            val outDateString = SimpleDateFormat("E, h:mm a", Locale.getDefault())

            return outDateString.format(convertedDate)
        }
    }

}