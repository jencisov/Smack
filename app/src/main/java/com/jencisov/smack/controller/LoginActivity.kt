package com.jencisov.smack.controller

import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.jencisov.smack.R
import com.jencisov.smack.services.AuthService
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun loginLoginBtnClicked(view: View) {
        hideKeyboard()
        enableProgressBar()

        val email = loginEmailEt.text.toString().trim()
        val password = loginPasswordEt.text.toString().trim()

        if (email.isEmpty() or password.isEmpty()) {
            Toast.makeText(this, "Please fill in both email and password", Toast.LENGTH_SHORT).show()
            return
        }

        AuthService.loginUser(this, email, password) { loginCompleted ->
            if (loginCompleted) {
                AuthService.findUserByEmail(this) { findCompleted ->
                    if (findCompleted) {
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
    }

    fun loginCreateUserBtnClicked(view: View) {
        val createUserIntent = Intent(this, CreateUserActivity::class.java)
        startActivity(createUserIntent)
        finish()
    }

    private fun errorToast() {
        Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show()
        disableProgressBar()
    }

    private fun enableProgressBar() {
        loginPb.visibility = View.VISIBLE
        loginLoginBtn.isEnabled = true
    }

    private fun disableProgressBar() {
        loginPb.visibility = View.GONE
        loginLoginBtn.isEnabled = false
    }

    fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

}