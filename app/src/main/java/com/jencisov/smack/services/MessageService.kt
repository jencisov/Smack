package com.jencisov.smack.services

import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.jencisov.smack.App
import com.jencisov.smack.model.Channel
import com.jencisov.smack.model.Message
import com.jencisov.smack.utils.URL_GET_CHANNELS
import com.jencisov.smack.utils.URL_GET_MESSAGES
import org.json.JSONException

object MessageService {

    val channels = ArrayList<Channel>()
    val messages = ArrayList<Message>()

    fun getChannels(complete: (Boolean) -> Unit) {
        val channelRequest = object : JsonArrayRequest(Method.GET, URL_GET_CHANNELS, null,
                Response.Listener { response ->
                    try {
                        for (x in 0 until response.length()) {
                            val channel = response.getJSONObject(x)
                            val name = channel.getString("name")
                            val description = channel.getString("description")
                            val id = channel.getString("_id")

                            val newChannel = Channel(name, description, id)
                            this.channels.add(newChannel)
                        }

                        complete(true)
                    } catch (e: JSONException) {
                        Log.d("JSON", "EXC: ${e.localizedMessage}")
                        complete(false)
                    }
                },
                Response.ErrorListener { error ->
                    Log.d("ERROR", "Could not retrieve channels")
                    complete(false)
                }) {
            override fun getBodyContentType() = "application/json; charset=utf-8"
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.isLoggedIn}")
                return headers
            }
        }

        App.prefs.requestQueue.add(channelRequest)
    }

    fun getMessages(channelId: String, complete: (Boolean) -> Unit) {
        val url = "$URL_GET_MESSAGES$channelId"

        val messagesRequest = object : JsonArrayRequest(Method.GET, url, null,
                Response.Listener { response ->
                    clearMessages()

                    try {
                        for (x in 0 until response.length()) {
                            val message = response.getJSONObject(x)
                            val messageBody = message.getString("messageBody")
                            val channelId = message.getString("channelId")
                            val id = message.getString("_id")
                            val userName = message.getString("userName")
                            val userAvatar = message.getString("userAvatar")
                            val userAvatarColor = message.getString("userAvatarColor")
                            val timeStamp = message.getString("timeStamp")

                            val newMessage = Message(messageBody, channelId, id, userName,
                                    userAvatar, userAvatarColor, timeStamp)
                            this.messages.add(newMessage)
                        }

                        complete(true)
                    } catch (e: JSONException) {
                        Log.d("JSON", "EXC: ${e.localizedMessage}")
                        complete(false)
                    }
                },
                Response.ErrorListener { error ->
                    Log.d("ERROR", "Could not retrieve messages")
                    complete(false)
                }) {
            override fun getBodyContentType() = "application/json; charset=utf-8"
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.isLoggedIn}")
                return headers
            }
        }

        App.prefs.requestQueue.add(messagesRequest)
    }

    fun clearMessages() {
        messages.clear()
    }

    fun clearChannels() {
        channels.clear()
    }

}