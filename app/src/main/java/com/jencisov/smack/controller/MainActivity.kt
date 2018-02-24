package com.jencisov.smack.controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import com.jencisov.smack.App
import com.jencisov.smack.R
import com.jencisov.smack.adapter.MessageAdapter
import com.jencisov.smack.model.Channel
import com.jencisov.smack.model.Message
import com.jencisov.smack.services.AuthService
import com.jencisov.smack.services.MessageService
import com.jencisov.smack.services.UserDataService
import com.jencisov.smack.utils.BROADCAST_USER_DATA_CHANGE
import com.jencisov.smack.utils.SOCKET_URL
import com.jencisov.smack.utils.hideKeyboard
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {

    val socket = IO.socket(SOCKET_URL)
    lateinit var channelAdapter: ArrayAdapter<Channel>
    lateinit var messageAdapter: MessageAdapter
    var selectedChannel: Channel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        socket.connect()
        socket.on("channelCreated", onNewChannel)
        socket.on("messageCreated", onNewMessage)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        setupAdapters()
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(userDataChangeReceiver, IntentFilter(BROADCAST_USER_DATA_CHANGE))

        channel_list.setOnItemClickListener { _, _, i, _ ->
            selectedChannel = MessageService.channels[i]
            drawer_layout.closeDrawer(GravityCompat.START)
            updateWithChannel()
        }

        if (App.prefs.isLoggedIn) {
            AuthService.findUserByEmail(this) {}
        }
    }

    override fun onDestroy() {
        socket.disconnect()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun loginBtnNavClicked(view: View) {
        if (App.prefs.isLoggedIn) {
            UserDataService.logout()
            channelAdapter.notifyDataSetChanged()
            messageAdapter.notifyDataSetChanged()
            userNameNavHeader.text = ""
            userEmailNavHeader.text = ""
            userImageNavHeader.setImageResource(R.drawable.profiledefault)
            userImageNavHeader.setBackgroundColor(Color.TRANSPARENT)
            loginBtnNavHeader.text = "Login"
            mainChannelNameTv.text = "Please log in"
        } else {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent);
        }
    }

    fun addChannelClicked(view: View) {
        if (App.prefs.isLoggedIn) {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)

            builder.setView(dialogView)
                    .setPositiveButton("Add") { _, _ ->
                        val nameEt = dialogView.findViewById<EditText>(R.id.addChannelNameEt)
                        val descriptionEt = dialogView.findViewById<EditText>(R.id.addChannelDescriptionEt)
                        val channelName = nameEt.text.toString().trim()
                        val channelDescription = descriptionEt.text.toString().trim()

                        socket.emit("newChannel", channelName, channelDescription)
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                    }
                    .show()
        }
    }

    fun sendMessageBtnClicked(view: View) {
        if (App.prefs.isLoggedIn && messageEt.text.isNotEmpty() && selectedChannel != null) {
            val userId = UserDataService.id
            val channelId = selectedChannel!!.id

            socket.emit("newMessage", messageEt.text.toString(), userId, channelId,
                    UserDataService.name, UserDataService.avatarName, UserDataService.avatarColor)
            messageEt.text.clear()
            hideKeyboard()
        }
    }

    private fun setupAdapters() {
        channelAdapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, MessageService.channels)
        channel_list.adapter = channelAdapter;

        messageAdapter = MessageAdapter(this, MessageService.messages)
        messageRv.adapter = messageAdapter
    }

    private val userDataChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (App.prefs.isLoggedIn) {
                userNameNavHeader.text = UserDataService.name
                userEmailNavHeader.text = UserDataService.email
                val resourceId = resources.getIdentifier(UserDataService.avatarName, "drawable", packageName)
                userImageNavHeader.setImageResource(resourceId)
                userImageNavHeader.setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))
                loginBtnNavHeader.text = "Logout"

                MessageService.getChannels() { complete ->
                    if (complete) {
                        if (MessageService.channels.count() > 0) {
                            selectedChannel = MessageService.channels[0]
                            channelAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }

    fun updateWithChannel() {
        mainChannelNameTv.text = "#${selectedChannel?.name}"
        if (selectedChannel != null) {
            MessageService.getMessages(selectedChannel!!.id) { complete ->
                messageAdapter.notifyDataSetChanged()
                if (messageAdapter.itemCount > 0) {
                    messageRv.smoothScrollToPosition(messageAdapter.itemCount - 1)
                }
            }
        }
    }

    private val onNewChannel = Emitter.Listener { args ->
        if (App.prefs.isLoggedIn) {
            runOnUiThread {
                val channelName = args[0] as String
                val channelDescription = args[1] as String
                val channelId = args[2] as String

                val newChannel = Channel(channelName, channelDescription, channelId)
                MessageService.channels.add(newChannel)
                channelAdapter.notifyDataSetChanged()
            }
        }
    }

    private val onNewMessage = Emitter.Listener { args ->
        if (App.prefs.isLoggedIn) {
            runOnUiThread {
                val channelId = args[2] as String

                if (selectedChannel?.id == channelId) {
                    val messageBody = args[0] as String
                    val userName = args[3] as String
                    val userAvatar = args[4] as String
                    val userAvatarColor = args[5] as String
                    val id = args[6] as String
                    val timeStamp = args[7] as String

                    val newMessage = Message(messageBody, userName, channelId,
                            userAvatar, userAvatarColor, id, timeStamp)
                    MessageService.messages.add(newMessage)
                    messageAdapter.notifyDataSetChanged()
                    messageRv.smoothScrollToPosition(messageAdapter.itemCount - 1)
                }
            }
        }
    }

}