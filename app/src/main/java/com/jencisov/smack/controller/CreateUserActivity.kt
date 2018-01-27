package com.jencisov.smack.controller

import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import android.widget.Toast
import com.jencisov.smack.R
import com.jencisov.smack.model.User
import com.jencisov.smack.services.AuthService
import com.jencisov.smack.utils.BROADCAST_USER_DATA_CHANGE
import kotlinx.android.synthetic.main.activity_create_user.*
import java.util.*

class CreateUserActivity : AppCompatActivity() {

    var userAvatar = "profileDefault"
    var avatarColor = "[0.5, 0.5, 0.5, 1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
    }

    fun generateUserAvatar(view: View) {
        val random = Random()
        val color = random.nextInt(2)
        val avatar = random.nextInt(28)

        if (color == 0) {
            userAvatar = "light$avatar"
        } else {
            userAvatar = "dark$avatar"
        }

        val resourceId = resources.getIdentifier(userAvatar, "drawable", packageName)
        createAvatarIv.setImageResource(resourceId)
    }

    fun generateColorBtnClicked(view: View) {
        val random = Random()
        val r = random.nextInt(255)
        val g = random.nextInt(255)
        val b = random.nextInt(255)

        createAvatarIv.setBackgroundColor(Color.rgb(r, g, b))

        val savedR = r.toDouble() / 255
        val savedG = g.toDouble() / 255
        val savedB = b.toDouble() / 255

        avatarColor = "[$savedR, $savedG, $savedB, 1]"
    }

    fun createUserClicked(view: View) {
        enableProgressBar()

        val userName = createUserNameEt.text.toString().trim()
        val email = createUserEmailEt.text.toString().trim()
        val password = createUserPasswordEt.text.toString().trim()

        if (userName.isEmpty() or email.isEmpty() or password.isEmpty()) {
            Toast.makeText(this, "Make sure user name, email and password are filled in", Toast.LENGTH_SHORT).show()
            disableProgressBar()
            return
        }

        AuthService.registerUser(this, email, password) { registerComplete ->
            if (registerComplete) {
                AuthService.loginUser(this, email, password) { loginComplete ->
                    if (loginComplete) {
                        AuthService.createUser(this, User(userName, email, userAvatar, avatarColor)) { createComplete ->
                            if (createComplete) {

                                val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                                LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange)

                                disableProgressBar()
                                finish()
                            } else {
                                errorToast()
                            }
                        }
                    } else {
                        errorToast()
                    }
                }
            } else {
                errorToast()
            }
        }
    }

    private fun errorToast() {
        Toast.makeText(this, "Something went wront, please try again", Toast.LENGTH_SHORT).show()
        disableProgressBar()
    }

    private fun enableProgressBar() {
        createUserPb.visibility = View.VISIBLE
        createUserBtn.isEnabled = false
        createAvatarIv.isEnabled = false
        backgroundColorBtn.isEnabled = false
    }

    private fun disableProgressBar() {
        createUserPb.visibility = View.GONE
        createUserBtn.isEnabled = true
        createAvatarIv.isEnabled = true
        backgroundColorBtn.isEnabled = true
    }

}