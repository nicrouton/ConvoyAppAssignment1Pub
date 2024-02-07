package edu.temple.convoy

import android.app.VoiceInteractor.Request
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {

    var loginVerified = false
    val urlString = "https://kamorris.com/lab/convoy/account.php"
    lateinit var sessionKey: String
    //Hydro 12345

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var isFirstTime = true
        if(!isFirstTime){
            launchHomePage()
        }

        findViewById<Button>(R.id.createAccountButton).setOnClickListener{
            createAccountSetup()
        }
        findViewById<Button>(R.id.loginButton).setOnClickListener {
            loginSetup()
        }

    }

    fun createAccountSetup(){
        setContentView(R.layout.create_account_screen)
        val username = findViewById<EditText>(R.id.usernameEditText)
        val password = findViewById<EditText>(R.id.passwordEditText)
        val firstname = findViewById<EditText>(R.id.firstnameEditText)
        val lastname = findViewById<EditText>(R.id.lastnameEditText)

        findViewById<Button>(R.id.submitButton).setOnClickListener {
            if(username.text.isEmpty() || password.text.isEmpty() ||
                firstname.text.isEmpty() || lastname.text.isEmpty()){
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show()
            } else {
                var queue = Volley.newRequestQueue(this@MainActivity)
                var stringRequest = object : StringRequest(
                    Method.POST,
                    urlString,
                    Response.Listener { response ->
                        val jsonData = JSONObject(response)
                        Log.d("", response)
                        if(jsonData.getString("status") == "SUCCESS"){
                            sessionKey = jsonData.getString("session_key")
                            launchHomePage()
                        } else{
                            Log.d("API",jsonData.getString("message"))
                        }
                    },
                    Response.ErrorListener { error ->
                        Toast.makeText(this, "There was an error making the login request", Toast.LENGTH_LONG).show()
                    }) {
                        override fun getParams(): Map<String, String> {
                            val params = HashMap<String, String>()
                            params.put("action", "REGISTER")
                            params.put("username",username.text.toString())
                            params.put("firstname",firstname.text.toString())
                            params.put("lastname",lastname.text.toString())
                            params.put("password", password.text.toString())
                            return params
                        }
                    }
                queue.add(stringRequest)
            }
        }
    }

    fun loginSetup() {
        setContentView(R.layout.login_screen)
        val username = findViewById<EditText>(R.id.usernameEditText)
        val password = findViewById<EditText>(R.id.passwordEditText)
        findViewById<Button>(R.id.submitButton).setOnClickListener {
            if (username.text.isEmpty() || password.text.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show()
            } else {
                var queue = Volley.newRequestQueue(this@MainActivity)
                var stringRequest = object : StringRequest(
                    Method.POST,
                    urlString,
                    Response.Listener { response ->
                        val jsonData = JSONObject(response)
                        Log.d("", response)
                        if(jsonData.getString("status") == "SUCCESS"){
                            sessionKey = jsonData.getString("session_key")
                            launchHomePage()
                        } else{
                            Log.d("API",jsonData.getString("message"))
                        }
                    },
                    Response.ErrorListener { error ->
                        Toast.makeText(
                            this,
                            "There was an error making the login request",
                            Toast.LENGTH_LONG
                        ).show()
                    }) {
                    override fun getParams(): Map<String, String> {
                        val params = HashMap<String, String>()
                        params.put("action", "LOGIN")
                        params.put("username", username.text.toString())
                        params.put("password", password.text.toString())
                        return params
                    }
                }
                queue.add(stringRequest)
            }
        }
    }

    fun launchHomePage(){
        startActivity(Intent(this, HomePage::class.java))
    }
}