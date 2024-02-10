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
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.net.URL

val USER_NAME = "USERNAME"
val sessionIDFileName = "SessionID"
val usernameFileName = "Username"
val convoyIDFileName = "ConvoyID"

class MainActivity : AppCompatActivity() {

    var loginVerified = false
    val urlString = "https://kamorris.com/lab/convoy/account.php"
    lateinit var sessionKey: String
    private lateinit var file: File
    val fileName = "SessionID"
    var user = ""

    lateinit var usernameEditText: EditText
    lateinit var passwordEditText: EditText
    lateinit var firstnameEditText: EditText
    lateinit var lastnameEditText: EditText
    //Hydro 12345
    //NR 12345

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        file = File(filesDir, fileName)
        var isFirstTime = Utils().loadPropertyFromFile(this, sessionIDFileName).first
        val loadUser = Utils().loadPropertyFromFile(this, usernameFileName)
        if(loadUser.first){
            user = loadUser.second
        }
        if(isFirstTime){
            launchHomePage()
        }
        loginSetup()
    }

    private fun createAccountSetup(){
        setContentView(R.layout.create_account_screen)
        findViewById<Button>(R.id.loginButton).setOnClickListener {
            setContentView(R.layout.activity_main)
            loginSetup()
        }
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText= findViewById(R.id.passwordEditText)
        firstnameEditText = findViewById(R.id.firstnameEditText)
        lastnameEditText = findViewById(R.id.lastnameEditText)

        findViewById<Button>(R.id.createAccountButton).setOnClickListener {
            if(usernameEditText.text.isEmpty() || passwordEditText.text.isEmpty() ||
                firstnameEditText.text.isEmpty() || lastnameEditText.text.isEmpty()){
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show()
            } else {
                user = usernameEditText.text.toString()
                val params = HashMap<String, String>()
                params.put("action", "REGISTER")
                params.put("username",user)
                params.put("firstname",firstnameEditText.text.toString())
                params.put("lastname",lastnameEditText.text.toString())
                params.put("password", passwordEditText.text.toString())
                Utils().getDataFromAPI(urlString,this,params){
                    val jsonData = JSONObject(it)
                    Log.d("", it)
                    if(jsonData.getString("status") == "SUCCESS"){
                        sessionKey = jsonData.getString("session_key")
                        Utils().savePropertyToFile(sessionKey, file)
                        Utils().savePropertyToFile(user, File(filesDir, usernameFileName))
                        launchHomePage()
                    } else{
                        Log.d("API",jsonData.getString("message"))
                    }
                }
            }

        }
    }

    fun loginSetup() {
        findViewById<Button>(R.id.createAccountButton).setOnClickListener{
            createAccountSetup()
        }
        usernameEditText = findViewById(R.id.usernameEditText)
        val password = findViewById<EditText>(R.id.passwordEditText)
        findViewById<Button>(R.id.loginButton).setOnClickListener {
            if (usernameEditText.text.isEmpty() || password.text.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show()
            } else {
                user = usernameEditText.text.toString()
                val params = HashMap<String, String>()
                params.put("action", "LOGIN")
                params.put("username",user)
                params.put("password", password.text.toString())
                Utils().getDataFromAPI(urlString,this,params){
                    val jsonData = JSONObject(it)
                    Log.d("", it)
                    if(jsonData.getString("status") == "SUCCESS"){
                        sessionKey = jsonData.getString("session_key")
                        Utils().savePropertyToFile(sessionKey, file)
                        Utils().savePropertyToFile(user, File(filesDir, usernameFileName))
                        launchHomePage()
                    } else{
                        Log.d("API",jsonData.getString("message"))
                    }
                }
            }
        }
    }

    fun launchHomePage(){
        startForegroundService(
            Intent(this, LocationListenerService::class.java)
        )

        startActivity(
            Intent(this, HomePage::class.java)
                .putExtra(USER_NAME, user)
        )
    }

}