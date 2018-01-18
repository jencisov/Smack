package com.jencisov.smack.services

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.jencisov.smack.utils.URL_LOGIN
import com.jencisov.smack.utils.URL_REGISTER
import org.json.JSONException
import org.json.JSONObject

object AuthService {

    var isLoggedin = false
    var userEmail = ""
    var authToken = ""

    fun registerUser(context: Context, email: String, password: String, complete: (Boolean) -> Unit) {

        val url = URL_REGISTER

        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)

        val requestBody = jsonBody.toString()

        val registerRequest = object : StringRequest(Request.Method.POST, url,
                Response.Listener { _ -> complete(true) },
                Response.ErrorListener { error ->
                    Log.d("ERROR", "Could not register user: $error")
                    complete(false)
                }) {
            override fun getBodyContentType() = "application/json; charset=utf-8"
            override fun getBody() = requestBody.toByteArray()
        }

        Volley.newRequestQueue(context).add(registerRequest)
    }

    fun loginUser(context: Context, email: String, password: String, complete: (Boolean) -> Unit) {

        val url = URL_LOGIN

        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)

        val requestBody = jsonBody.toString()

        val loginRequest = object : JsonObjectRequest(Request.Method.POST, url, null,
                Response.Listener { response ->

                    try {
                        userEmail = response.getString("user")
                        authToken = response.getString("token")
                        isLoggedin = true

                        complete(true)
                    } catch (exception: JSONException) {
                        Log.d("JSON", "EXCEPTION: ${exception.localizedMessage}")
                        complete(false)
                    }
                },
                Response.ErrorListener { error ->
                    Log.d("ERROR", "Could not login user: $error")
                    complete(false)
                }) {
            override fun getBodyContentType() = "application/json; charset=utf-8"
            override fun getBody() = requestBody.toByteArray()
        }

        Volley.newRequestQueue(context).add(loginRequest)
    }

}