package edu.temple.convoy

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.io.File

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

        requestUserPermission()

        val channel = NotificationChannel(
            "Convoy",
            "Convoy Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        if(loadUser.first){
            user = loadUser.second
        }
        if(isFirstTime){
            launchHomePage()
        }
        loginSetup()
    }

    private fun requestUserPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.FOREGROUND_SERVICE_LOCATION
                ),
            0
        )
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
        startActivity(
            Intent(this, HomePage::class.java)
                .putExtra(USER_NAME, user)
        )
    }

}